#ifndef DECRYPTIONENGINE_H
#define DECRYPTIONENGINE_H

#include <QObject>
#include <QThread>
#include <QTimer>
#include <QString>
#include <QStringList>
#include <QMutex>
#include <QAtomicInt>
#include "data/Models.h"

class DecryptionWorker;

class DecryptionEngine : public QObject
{
    Q_OBJECT

public:
    explicit DecryptionEngine(QObject *parent = nullptr);
    ~DecryptionEngine();

    void startAttack(const AttackConfiguration &config);
    void stopAttack();
    bool isRunning() const;
    
    EncryptionAnalysis analyzeFile(const QString &filePath);

signals:
    void progressUpdated(const AttackProgress &progress);
    void attackFinished(const AttackResult &result);

private slots:
    void updateProgress();
    void onWorkerFinished();

private:
    void setupWorkers(const AttackConfiguration &config);
    QStringList generatePasswordCandidates(const AttackConfiguration &config, int start, int count);
    QStringList loadDictionary(const QString &filePath);
    
    QList<DecryptionWorker*> m_workers;
    QList<QThread*> m_threads;
    QTimer *m_progressTimer;
    QMutex m_mutex;
    
    AttackConfiguration m_config;
    AttackProgress m_progress;
    AttackResult m_result;
    
    QAtomicInt m_running;
    QAtomicInt m_workersFinished;
    qint64 m_startTime;
};

class DecryptionWorker : public QObject
{
    Q_OBJECT

public:
    explicit DecryptionWorker(const AttackConfiguration &config, 
                             const QStringList &passwords,
                             QObject *parent = nullptr);

public slots:
    void doWork();
    void stop();

signals:
    void progressUpdate(qint64 attempts, const QString &currentPassword);
    void passwordFound(const QString &password);
    void finished();

private:
    bool testPassword(const QString &password);
    QByteArray deriveKey(const QString &password, const QByteArray &salt = QByteArray());
    bool decryptFile(const QString &password);
    
    AttackConfiguration m_config;
    QStringList m_passwords;
    QAtomicInt m_shouldStop;
    qint64 m_attempts;
};

#endif // DECRYPTIONENGINE_H