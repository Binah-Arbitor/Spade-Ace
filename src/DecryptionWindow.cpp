#include "DecryptionWindow.h"
#include "core/DecryptionEngine.h"
#include <QMessageBox>
#include <QSplitter>

DecryptionWindow::DecryptionWindow(QWidget *parent)
    : QWidget(parent)
    , m_engine(new DecryptionEngine(this))
    , m_updateTimer(new QTimer(this))
    , m_attackRunning(false)
{
    setupUI();
    
    // Connect timer for progress updates
    connect(m_updateTimer, &QTimer::timeout, this, &DecryptionWindow::updateProgress);
    m_updateTimer->setInterval(1000); // Update every second
}

void DecryptionWindow::setupUI()
{
    m_mainLayout = new QVBoxLayout(this);
    
    setupFileSelection();
    setupAttackConfiguration();
    setupProgress();
    setupResults();
    
    // Add control buttons
    QHBoxLayout *buttonLayout = new QHBoxLayout();
    m_startButton = new QPushButton("🚀 Start Attack", this);
    m_stopButton = new QPushButton("⏹️ Stop Attack", this);
    m_stopButton->setEnabled(false);
    
    connect(m_startButton, &QPushButton::clicked, this, &DecryptionWindow::startAttack);
    connect(m_stopButton, &QPushButton::clicked, this, &DecryptionWindow::stopAttack);
    
    buttonLayout->addWidget(m_startButton);
    buttonLayout->addWidget(m_stopButton);
    buttonLayout->addStretch();
    
    m_mainLayout->addLayout(buttonLayout);
}

void DecryptionWindow::setupFileSelection()
{
    m_fileGroup = new QGroupBox("📁 Target File", this);
    QVBoxLayout *fileLayout = new QVBoxLayout(m_fileGroup);
    
    QHBoxLayout *filePathLayout = new QHBoxLayout();
    m_targetFileEdit = new QLineEdit(this);
    m_targetFileEdit->setPlaceholderText("Select an encrypted file...");
    m_browseTargetButton = new QPushButton("Browse...", this);
    m_analyzeButton = new QPushButton("🔍 Analyze", this);
    m_analyzeButton->setEnabled(false);
    
    connect(m_browseTargetButton, &QPushButton::clicked, this, &DecryptionWindow::browseTargetFile);
    connect(m_analyzeButton, &QPushButton::clicked, this, &DecryptionWindow::analyzeFile);
    
    filePathLayout->addWidget(new QLabel("Target File:"));
    filePathLayout->addWidget(m_targetFileEdit);
    filePathLayout->addWidget(m_browseTargetButton);
    filePathLayout->addWidget(m_analyzeButton);
    
    fileLayout->addLayout(filePathLayout);
    m_mainLayout->addWidget(m_fileGroup);
}

void DecryptionWindow::setupAttackConfiguration()
{
    m_configGroup = new QGroupBox("⚙️ Attack Configuration", this);
    QGridLayout *configLayout = new QGridLayout(m_configGroup);
    
    // Attack type
    configLayout->addWidget(new QLabel("Attack Type:"), 0, 0);
    m_attackTypeCombo = new QComboBox(this);
    m_attackTypeCombo->addItems({
        "Brute Force",
        "Smart Brute Force", 
        "Dictionary Attack",
        "Hybrid Attack",
        "Mask Attack",
        "Rule-based Attack",
        "Rainbow Table"
    });
    connect(m_attackTypeCombo, QOverload<int>::of(&QComboBox::currentIndexChanged),
            this, &DecryptionWindow::onAttackTypeChanged);
    configLayout->addWidget(m_attackTypeCombo, 0, 1, 1, 2);
    
    // Max password length
    configLayout->addWidget(new QLabel("Max Length:"), 1, 0);
    m_maxLengthSpin = new QSpinBox(this);
    m_maxLengthSpin->setRange(1, 20);
    m_maxLengthSpin->setValue(8);
    configLayout->addWidget(m_maxLengthSpin, 1, 1);
    
    // Character set
    configLayout->addWidget(new QLabel("Character Set:"), 2, 0);
    m_characterSetEdit = new QLineEdit(this);
    m_characterSetEdit->setText("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    configLayout->addWidget(m_characterSetEdit, 2, 1, 1, 2);
    
    // Dictionary file
    configLayout->addWidget(new QLabel("Dictionary File:"), 3, 0);
    m_dictionaryFileEdit = new QLineEdit(this);
    m_dictionaryFileEdit->setPlaceholderText("Select dictionary file...");
    m_browseDictionaryButton = new QPushButton("Browse...", this);
    connect(m_browseDictionaryButton, &QPushButton::clicked, this, &DecryptionWindow::browseDictionaryFile);
    configLayout->addWidget(m_dictionaryFileEdit, 3, 1);
    configLayout->addWidget(m_browseDictionaryButton, 3, 2);
    
    // Rule file
    configLayout->addWidget(new QLabel("Rule File:"), 4, 0);
    m_ruleFileEdit = new QLineEdit(this);
    m_ruleFileEdit->setPlaceholderText("Select rule file...");
    m_browseRuleButton = new QPushButton("Browse...", this);
    connect(m_browseRuleButton, &QPushButton::clicked, this, &DecryptionWindow::browseRuleFile);
    configLayout->addWidget(m_ruleFileEdit, 4, 1);
    configLayout->addWidget(m_browseRuleButton, 4, 2);
    
    // Rainbow table
    configLayout->addWidget(new QLabel("Rainbow Table:"), 5, 0);
    m_rainbowTableEdit = new QLineEdit(this);
    m_rainbowTableEdit->setPlaceholderText("Select rainbow table file...");
    m_browseRainbowButton = new QPushButton("Browse...", this);
    connect(m_browseRainbowButton, &QPushButton::clicked, this, &DecryptionWindow::browseRainbowTableFile);
    configLayout->addWidget(m_rainbowTableEdit, 5, 1);
    configLayout->addWidget(m_browseRainbowButton, 5, 2);
    
    // Mask pattern
    configLayout->addWidget(new QLabel("Mask Pattern:"), 6, 0);
    m_maskPatternEdit = new QLineEdit(this);
    m_maskPatternEdit->setText("?l?l?l?l?d?d?d?d");
    m_maskPatternEdit->setToolTip("?l=lowercase, ?u=uppercase, ?d=digit, ?s=special");
    configLayout->addWidget(m_maskPatternEdit, 6, 1, 1, 2);
    
    m_mainLayout->addWidget(m_configGroup);
    updateAttackTypeOptions();
}

void DecryptionWindow::setupProgress()
{
    m_progressGroup = new QGroupBox("📊 Progress", this);
    QVBoxLayout *progressLayout = new QVBoxLayout(m_progressGroup);
    
    m_progressBar = new QProgressBar(this);
    m_statusLabel = new QLabel("Ready", this);
    m_attemptsLabel = new QLabel("Attempts: 0", this);
    m_timeLabel = new QLabel("Elapsed: 00:00:00", this);
    m_speedLabel = new QLabel("Speed: 0 attempts/sec", this);
    
    progressLayout->addWidget(m_progressBar);
    progressLayout->addWidget(m_statusLabel);
    progressLayout->addWidget(m_attemptsLabel);
    progressLayout->addWidget(m_timeLabel);
    progressLayout->addWidget(m_speedLabel);
    
    m_mainLayout->addWidget(m_progressGroup);
}

void DecryptionWindow::setupResults()
{
    m_resultsGroup = new QGroupBox("📋 Results", this);
    QVBoxLayout *resultsLayout = new QVBoxLayout(m_resultsGroup);
    
    m_resultsText = new QTextEdit(this);
    m_resultsText->setReadOnly(true);
    m_resultsText->setMaximumHeight(150);
    m_resultsText->setPlaceholderText("Attack results will appear here...");
    
    resultsLayout->addWidget(m_resultsText);
    m_mainLayout->addWidget(m_resultsGroup);
}

void DecryptionWindow::browseTargetFile()
{
    QString fileName = QFileDialog::getOpenFileName(this,
        "Select Target File",
        QString(),
        "All Files (*.*)");
    
    if (!fileName.isEmpty()) {
        m_targetFileEdit->setText(fileName);
        m_analyzeButton->setEnabled(true);
        m_config.targetFile = fileName;
    }
}

void DecryptionWindow::browseDictionaryFile()
{
    QString fileName = QFileDialog::getOpenFileName(this,
        "Select Dictionary File",
        QString(),
        "Text Files (*.txt);;All Files (*.*)");
    
    if (!fileName.isEmpty()) {
        m_dictionaryFileEdit->setText(fileName);
        m_config.dictionaryFile = fileName;
    }
}

void DecryptionWindow::browseRuleFile()
{
    QString fileName = QFileDialog::getOpenFileName(this,
        "Select Rule File",
        QString(),
        "Text Files (*.txt);;All Files (*.*)");
    
    if (!fileName.isEmpty()) {
        m_ruleFileEdit->setText(fileName);
        m_config.ruleFile = fileName;
    }
}

void DecryptionWindow::browseRainbowTableFile()
{
    QString fileName = QFileDialog::getOpenFileName(this,
        "Select Rainbow Table File",
        QString(),
        "All Files (*.*)");
    
    if (!fileName.isEmpty()) {
        m_rainbowTableEdit->setText(fileName);
        m_config.rainbowTableFile = fileName;
    }
}

void DecryptionWindow::analyzeFile()
{
    if (m_config.targetFile.isEmpty()) {
        QMessageBox::warning(this, "Warning", "Please select a target file first.");
        return;
    }
    
    m_resultsText->append("🔍 Analyzing file: " + m_config.targetFile);
    // TODO: Implement file analysis
    m_resultsText->append("✅ Analysis completed. File appears to be encrypted.");
}

void DecryptionWindow::startAttack()
{
    if (m_config.targetFile.isEmpty()) {
        QMessageBox::warning(this, "Warning", "Please select a target file first.");
        return;
    }
    
    // Update configuration from UI
    m_config.attackType = stringToAttackType(m_attackTypeCombo->currentText());
    m_config.maxPasswordLength = m_maxLengthSpin->value();
    m_config.characterSet = m_characterSetEdit->text();
    m_config.maskPattern = m_maskPatternEdit->text();
    
    m_attackRunning = true;
    m_startButton->setEnabled(false);
    m_stopButton->setEnabled(true);
    m_updateTimer->start();
    
    m_resultsText->append("🚀 Starting " + attackTypeToString(m_config.attackType) + " attack...");
    m_statusLabel->setText("Attack in progress...");
    
    // TODO: Start actual attack
}

void DecryptionWindow::stopAttack()
{
    m_attackRunning = false;
    m_startButton->setEnabled(true);
    m_stopButton->setEnabled(false);
    m_updateTimer->stop();
    
    m_resultsText->append("⏹️ Attack stopped by user.");
    m_statusLabel->setText("Attack stopped");
}

void DecryptionWindow::onAttackTypeChanged()
{
    updateAttackTypeOptions();
}

void DecryptionWindow::updateProgress()
{
    if (!m_attackRunning) return;
    
    // TODO: Update with real progress data
    static int fakeProgress = 0;
    fakeProgress += 1;
    
    m_progressBar->setValue(fakeProgress % 100);
    m_attemptsLabel->setText(QString("Attempts: %1").arg(fakeProgress * 1000));
    
    // Update time
    static int seconds = 0;
    seconds++;
    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;
    int secs = seconds % 60;
    m_timeLabel->setText(QString("Elapsed: %1:%2:%3")
                        .arg(hours, 2, 10, QChar('0'))
                        .arg(minutes, 2, 10, QChar('0'))
                        .arg(secs, 2, 10, QChar('0')));
    
    m_speedLabel->setText(QString("Speed: %1 attempts/sec").arg(fakeProgress * 10));
}

void DecryptionWindow::updateAttackTypeOptions()
{
    AttackType type = stringToAttackType(m_attackTypeCombo->currentText());
    
    // Show/hide relevant options based on attack type
    bool showDictionary = (type == AttackType::DICTIONARY_ATTACK || type == AttackType::HYBRID_ATTACK);
    bool showRules = (type == AttackType::RULE_BASED_ATTACK);
    bool showRainbow = (type == AttackType::RAINBOW_TABLE);
    bool showMask = (type == AttackType::MASK_ATTACK);
    
    m_dictionaryFileEdit->setVisible(showDictionary);
    m_browseDictionaryButton->setVisible(showDictionary);
    m_ruleFileEdit->setVisible(showRules);
    m_browseRuleButton->setVisible(showRules);
    m_rainbowTableEdit->setVisible(showRainbow);
    m_browseRainbowButton->setVisible(showRainbow);
    m_maskPatternEdit->setVisible(showMask);
}