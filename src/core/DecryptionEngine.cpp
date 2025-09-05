#include "DecryptionEngine.h"
#include "CryptoUtils.h"
#include <QFileInfo>
#include <QCryptographicHash>
#include <QRandomGenerator>
#include <QDebug>
#include <QDateTime>
#include <QFile>
#include <QTextStream>
#include <QThread>

DecryptionEngine::DecryptionEngine(QObject *parent)
    : QObject(parent)
    , m_progressTimer(new QTimer(this))
    , m_running(0)
    , m_workersFinished(0)
    , m_startTime(0)
{
    connect(m_progressTimer, &QTimer::timeout, this, &DecryptionEngine::updateProgress);
    m_progressTimer->setInterval(1000); // Update every second
}

DecryptionEngine::~DecryptionEngine()
{
    stopAttack();
}

void DecryptionEngine::startAttack(const AttackConfiguration &config)
{
    if (m_running.load()) {
        return; // Already running
    }
    
    m_config = config;
    m_running.store(1);
    m_workersFinished.store(0);
    m_startTime = QDateTime::currentMSecsSinceEpoch();
    
    // Initialize progress
    m_progress = AttackProgress();
    m_progress.status = "Starting attack...";
    
    // Generate password list based on attack type
    QStringList allPasswords;
    
    switch (config.attackType) {
        case AttackType::DICTIONARY_ATTACK:
            if (!config.dictionaryFile.isEmpty()) {
                allPasswords = loadDictionary(config.dictionaryFile);
            }
            break;
            
        case AttackType::BRUTE_FORCE:
        case AttackType::SMART_BRUTE_FORCE:
            // Generate brute force combinations
            allPasswords = generatePasswordCandidates(config, 0, 10000); // Start with 10k
            break;
            
        case AttackType::MASK_ATTACK:
            // Generate passwords based on mask pattern
            allPasswords = generatePasswordCandidates(config, 0, 5000);
            break;
            
        default:
            allPasswords = generatePasswordCandidates(config, 0, 1000);
            break;
    }
    
    m_progress.totalAttempts = allPasswords.size();
    
    setupWorkers(config);
    
    // Distribute passwords among workers
    int passwordsPerWorker = allPasswords.size() / config.threadCount;
    for (int i = 0; i < config.threadCount; ++i) {
        int start = i * passwordsPerWorker;
        int end = (i == config.threadCount - 1) ? allPasswords.size() : (i + 1) * passwordsPerWorker;
        
        QStringList workerPasswords = allPasswords.mid(start, end - start);
        
        DecryptionWorker *worker = new DecryptionWorker(config, workerPasswords, this);
        QThread *thread = new QThread(this);
        
        worker->moveToThread(thread);
        
        connect(thread, &QThread::started, worker, &DecryptionWorker::doWork);
        connect(worker, &DecryptionWorker::finished, this, &DecryptionEngine::onWorkerFinished);
        connect(worker, &DecryptionWorker::passwordFound, this, [this](const QString &password) {
            m_result.success = true;
            m_result.foundPassword = password;
            stopAttack();
        });
        
        m_workers.append(worker);
        m_threads.append(thread);
        
        thread->start();
    }
    
    m_progressTimer->start();
    m_progress.status = "Attack in progress...";
}

void DecryptionEngine::stopAttack()
{
    if (!m_running.load()) {
        return;
    }
    
    m_running.store(0);
    
    // Stop all workers
    for (DecryptionWorker *worker : m_workers) {
        worker->stop();
    }
    
    // Wait for threads to finish
    for (QThread *thread : m_threads) {
        thread->quit();
        thread->wait(3000); // Wait up to 3 seconds
        if (thread->isRunning()) {
            thread->terminate();
            thread->wait(1000);
        }
    }
    
    // Clean up
    qDeleteAll(m_workers);
    qDeleteAll(m_threads);
    m_workers.clear();
    m_threads.clear();
    
    m_progressTimer->stop();
    
    // Finalize result
    m_result.timeElapsed = QDateTime::currentMSecsSinceEpoch() - m_startTime;
    m_result.attemptsCount = m_progress.attemptsCount;
    
    if (!m_result.success) {
        m_result.errorMessage = "Attack stopped or no password found";
    }
    
    emit attackFinished(m_result);
}

bool DecryptionEngine::isRunning() const
{
    return m_running.load();
}

void DecryptionEngine::setupWorkers(const AttackConfiguration &config)
{
    Q_UNUSED(config)
    // Workers are created in startAttack method
}

void DecryptionEngine::updateProgress()
{
    if (!m_running.load()) {
        return;
    }
    
    qint64 currentTime = QDateTime::currentMSecsSinceEpoch();
    m_progress.elapsedTime = currentTime - m_startTime;
    
    // Calculate progress percentage
    if (m_progress.totalAttempts > 0) {
        m_progress.progressPercentage = (double)m_progress.attemptsCount / m_progress.totalAttempts * 100.0;
    }
    
    // Calculate speed
    if (m_progress.elapsedTime > 0) {
        m_progress.attemptsPerSecond = (double)m_progress.attemptsCount / (m_progress.elapsedTime / 1000.0);
    }
    
    // Estimate remaining time
    if (m_progress.attemptsPerSecond > 0) {
        qint64 remainingAttempts = m_progress.totalAttempts - m_progress.attemptsCount;
        m_progress.estimatedTimeRemaining = remainingAttempts / m_progress.attemptsPerSecond * 1000;
    }
    
    emit progressUpdated(m_progress);
}

void DecryptionEngine::onWorkerFinished()
{
    int finished = m_workersFinished.fetchAndAddAcquire(1) + 1;
    
    if (finished >= m_config.threadCount) {
        // All workers finished
        stopAttack();
    }
}

QStringList DecryptionEngine::generatePasswordCandidates(const AttackConfiguration &config, int start, int count)
{
    QStringList passwords;
    
    if (config.attackType == AttackType::SMART_BRUTE_FORCE) {
        // Add common passwords first
        QStringList commonPasswords = {
            "password", "123456", "123456789", "qwerty", "abc123", "password123",
            "admin", "letmein", "welcome", "monkey", "dragon", "1234567890"
        };
        
        for (const QString &pwd : commonPasswords) {
            if (pwd.length() <= config.maxPasswordLength) {
                passwords.append(pwd);
            }
        }
    }
    
    // Generate brute force combinations
    QString charset = config.characterSet;
    
    for (int len = 1; len <= config.maxPasswordLength && passwords.size() < count; ++len) {
        // Generate combinations of length 'len'
        for (int i = 0; i < qMin(1000, count - passwords.size()); ++i) {
            QString password;
            for (int j = 0; j < len; ++j) {
                int charIndex = QRandomGenerator::global()->bounded(charset.length());
                password.append(charset[charIndex]);
            }
            passwords.append(password);
        }
    }
    
    return passwords;
}

QStringList DecryptionEngine::loadDictionary(const QString &filePath)
{
    QStringList passwords;
    
    QFile file(filePath);
    if (file.open(QIODevice::ReadOnly | QIODevice::Text)) {
        QTextStream in(&file);
        while (!in.atEnd()) {
            QString line = in.readLine().trimmed();
            if (!line.isEmpty()) {
                passwords.append(line);
            }
        }
    }
    
    return passwords;
}

EncryptionAnalysis DecryptionEngine::analyzeFile(const QString &filePath)
{
    EncryptionAnalysis analysis;
    
    QFileInfo fileInfo(filePath);
    analysis.fileSize = fileInfo.size();
    
    // Calculate file hash
    QFile file(filePath);
    if (file.open(QIODevice::ReadOnly)) {
        QCryptographicHash hash(QCryptographicHash::Sha256);
        hash.addData(&file);
        analysis.fileHash = hash.result().toHex();
    }
    
    // Mock analysis - in real implementation, this would analyze file headers
    analysis.encryptionType = "Unknown";
    analysis.detectedAlgorithm = "AES-256";
    analysis.keySize = 256;
    analysis.mode = "CBC";
    analysis.padding = "PKCS7";
    analysis.hasIV = true;
    analysis.hasSalt = true;
    analysis.confidenceLevel = 0.75;
    
    analysis.analysisNotes.append("File appears to be encrypted");
    analysis.analysisNotes.append("Detected possible AES encryption");
    analysis.analysisNotes.append("File size: " + QString::number(analysis.fileSize) + " bytes");
    
    return analysis;
}

// DecryptionWorker implementation
DecryptionWorker::DecryptionWorker(const AttackConfiguration &config, 
                                   const QStringList &passwords,
                                   QObject *parent)
    : QObject(parent)
    , m_config(config)
    , m_passwords(passwords)
    , m_shouldStop(0)
    , m_attempts(0)
{
}

void DecryptionWorker::doWork()
{
    for (const QString &password : m_passwords) {
        if (m_shouldStop.load()) {
            break;
        }
        
        m_attempts++;
        
        if (testPassword(password)) {
            emit passwordFound(password);
            break;
        }
        
        // Emit progress every 100 attempts
        if (m_attempts % 100 == 0) {
            emit progressUpdate(m_attempts, password);
        }
    }
    
    emit finished();
}

void DecryptionWorker::stop()
{
    m_shouldStop.store(1);
}

bool DecryptionWorker::testPassword(const QString &password)
{
    // Mock password testing - in real implementation, this would try to decrypt the file
    Q_UNUSED(password)
    
    // Simulate some work
    QThread::msleep(1);
    
    // Random chance of success for testing (very low)
    return QRandomGenerator::global()->bounded(100000) == 0;
}

QByteArray DecryptionWorker::deriveKey(const QString &password, const QByteArray &salt)
{
    // Mock key derivation - in real implementation, this would use proper KDF
    Q_UNUSED(salt)
    
    QCryptographicHash hash(QCryptographicHash::Sha256);
    hash.addData(password.toUtf8());
    return hash.result();
}

bool DecryptionWorker::decryptFile(const QString &password)
{
    // Mock decryption - in real implementation, this would try to decrypt the actual file
    Q_UNUSED(password)
    return false;
}

#include "DecryptionEngine.moc"