#include "SettingsWindow.h"
#include <QThread>
#include <QSysInfo>
#include <QStorageInfo>
#include <QMessageBox>

SettingsWindow::SettingsWindow(QWidget *parent)
    : QWidget(parent)
{
    setupUI();
    updateSystemInfo();
}

void SettingsWindow::setupUI()
{
    m_mainLayout = new QVBoxLayout(this);
    
    setupPerformanceSettings();
    setupHardwareSettings();
    setupSystemInfo();
    
    // Control buttons
    QHBoxLayout *buttonLayout = new QHBoxLayout();
    m_resetButton = new QPushButton("🔄 Reset to Defaults", this);
    m_optimizeButton = new QPushButton("⚡ Optimize Memory", this);
    
    connect(m_resetButton, &QPushButton::clicked, this, &SettingsWindow::resetToDefaults);
    connect(m_optimizeButton, &QPushButton::clicked, this, &SettingsWindow::optimizeMemory);
    
    buttonLayout->addWidget(m_resetButton);
    buttonLayout->addWidget(m_optimizeButton);
    buttonLayout->addStretch();
    
    m_mainLayout->addLayout(buttonLayout);
}

void SettingsWindow::setupPerformanceSettings()
{
    m_performanceGroup = new QGroupBox("⚡ Performance Settings", this);
    QGridLayout *perfLayout = new QGridLayout(m_performanceGroup);
    
    // Optimization level
    perfLayout->addWidget(new QLabel("Optimization Level:"), 0, 0);
    m_optimizationCombo = new QComboBox(this);
    m_optimizationCombo->addItems({"Low", "Medium", "High", "Extreme"});
    m_optimizationCombo->setCurrentText("High");
    connect(m_optimizationCombo, QOverload<int>::of(&QComboBox::currentIndexChanged),
            this, &SettingsWindow::onOptimizationLevelChanged);
    perfLayout->addWidget(m_optimizationCombo, 0, 1);
    
    // Thread count
    int maxThreads = QThread::idealThreadCount();
    perfLayout->addWidget(new QLabel("Thread Count:"), 1, 0);
    
    QHBoxLayout *threadLayout = new QHBoxLayout();
    m_threadSpin = new QSpinBox(this);
    m_threadSpin->setRange(1, maxThreads * 2);
    m_threadSpin->setValue(maxThreads);
    
    m_threadSlider = new QSlider(Qt::Horizontal, this);
    m_threadSlider->setRange(1, maxThreads * 2);
    m_threadSlider->setValue(maxThreads);
    
    connect(m_threadSpin, QOverload<int>::of(&QSpinBox::valueChanged),
            this, &SettingsWindow::onThreadCountChanged);
    connect(m_threadSlider, &QSlider::valueChanged, this, &SettingsWindow::onThreadCountChanged);
    
    threadLayout->addWidget(m_threadSpin);
    threadLayout->addWidget(m_threadSlider);
    perfLayout->addLayout(threadLayout, 1, 1);
    
    // Chunk size
    perfLayout->addWidget(new QLabel("Chunk Size (KB):"), 2, 0);
    m_chunkSizeSpin = new QSpinBox(this);
    m_chunkSizeSpin->setRange(64, 16384);
    m_chunkSizeSpin->setValue(1024);
    m_chunkSizeSpin->setSuffix(" KB");
    perfLayout->addWidget(m_chunkSizeSpin, 2, 1);
    
    m_mainLayout->addWidget(m_performanceGroup);
}

void SettingsWindow::setupHardwareSettings()
{
    m_hardwareGroup = new QGroupBox("🖥️ Hardware Settings", this);
    QGridLayout *hwLayout = new QGridLayout(m_hardwareGroup);
    
    // Hardware acceleration
    hwLayout->addWidget(new QLabel("Hardware Acceleration:"), 0, 0);
    m_hardwareAccelCombo = new QComboBox(this);
    m_hardwareAccelCombo->addItems({"CPU Only", "GPU Assisted", "Hybrid Mode"});
    connect(m_hardwareAccelCombo, QOverload<int>::of(&QComboBox::currentIndexChanged),
            this, &SettingsWindow::onHardwareAccelerationChanged);
    hwLayout->addWidget(m_hardwareAccelCombo, 0, 1);
    
    // Key derivation method
    hwLayout->addWidget(new QLabel("Key Derivation:"), 1, 0);
    m_keyDerivationCombo = new QComboBox(this);
    m_keyDerivationCombo->addItems({"SHA-256 Simple", "PBKDF2", "Scrypt", "Argon2", "Bcrypt"});
    m_keyDerivationCombo->setCurrentText("SHA-256 Simple");
    hwLayout->addWidget(m_keyDerivationCombo, 1, 1);
    
    // Options
    hwLayout->addWidget(new QLabel("Options:"), 2, 0);
    QVBoxLayout *optionsLayout = new QVBoxLayout();
    
    m_gpuAccelCheck = new QCheckBox("Enable GPU Acceleration", this);
    m_smartPatternsCheck = new QCheckBox("Enable Smart Patterns", this);
    m_smartPatternsCheck->setChecked(true);
    m_commonPasswordsCheck = new QCheckBox("Try Common Passwords First", this);
    m_commonPasswordsCheck->setChecked(true);
    
    optionsLayout->addWidget(m_gpuAccelCheck);
    optionsLayout->addWidget(m_smartPatternsCheck);
    optionsLayout->addWidget(m_commonPasswordsCheck);
    
    hwLayout->addLayout(optionsLayout, 2, 1);
    
    m_mainLayout->addWidget(m_hardwareGroup);
}

void SettingsWindow::setupSystemInfo()
{
    m_systemGroup = new QGroupBox("💻 System Information", this);
    QVBoxLayout *sysLayout = new QVBoxLayout(m_systemGroup);
    
    m_systemInfoText = new QTextEdit(this);
    m_systemInfoText->setReadOnly(true);
    m_systemInfoText->setMaximumHeight(200);
    
    m_detectButton = new QPushButton("🔍 Detect Hardware", this);
    connect(m_detectButton, &QPushButton::clicked, this, &SettingsWindow::detectHardware);
    
    sysLayout->addWidget(m_systemInfoText);
    sysLayout->addWidget(m_detectButton);
    
    m_mainLayout->addWidget(m_systemGroup);
}

void SettingsWindow::updateSystemInfo()
{
    QString info;
    
    // System information
    info += "🖥️ <b>System Information</b><br>";
    info += QString("OS: %1<br>").arg(QSysInfo::prettyProductName());
    info += QString("Architecture: %1<br>").arg(QSysInfo::currentCpuArchitecture());
    info += QString("Kernel: %1<br>").arg(QSysInfo::kernelVersion());
    info += "<br>";
    
    // CPU information
    info += "🔧 <b>CPU Information</b><br>";
    info += QString("Ideal Thread Count: %1<br>").arg(QThread::idealThreadCount());
    info += QString("Current Thread Count: %1<br>").arg(m_threadSpin->value());
    info += "<br>";
    
    // Memory information
    info += "💾 <b>Memory Information</b><br>";
    info += QString("Chunk Size: %1 KB<br>").arg(m_chunkSizeSpin->value());
    info += "<br>";
    
    // Storage information
    info += "💿 <b>Storage Information</b><br>";
    QStorageInfo storage = QStorageInfo::root();
    qint64 totalSpace = storage.bytesTotal();
    qint64 freeSpace = storage.bytesAvailable();
    
    info += QString("Total Space: %1 GB<br>").arg(totalSpace / (1024 * 1024 * 1024));
    info += QString("Free Space: %1 GB<br>").arg(freeSpace / (1024 * 1024 * 1024));
    info += "<br>";
    
    // Configuration
    info += "⚙️ <b>Current Configuration</b><br>";
    info += QString("Optimization Level: %1<br>").arg(m_optimizationCombo->currentText());
    info += QString("Hardware Acceleration: %1<br>").arg(m_hardwareAccelCombo->currentText());
    info += QString("Key Derivation: %1<br>").arg(m_keyDerivationCombo->currentText());
    
    m_systemInfoText->setHtml(info);
}

void SettingsWindow::onOptimizationLevelChanged()
{
    updateSystemInfo();
}

void SettingsWindow::onHardwareAccelerationChanged()
{
    bool enableGpu = (m_hardwareAccelCombo->currentText() != "CPU Only");
    m_gpuAccelCheck->setEnabled(enableGpu);
    m_gpuAccelCheck->setChecked(enableGpu);
    updateSystemInfo();
}

void SettingsWindow::onThreadCountChanged()
{
    // Sync spinbox and slider
    if (sender() == m_threadSpin) {
        m_threadSlider->setValue(m_threadSpin->value());
    } else if (sender() == m_threadSlider) {
        m_threadSpin->setValue(m_threadSlider->value());
    }
    updateSystemInfo();
}

void SettingsWindow::resetToDefaults()
{
    int reply = QMessageBox::question(this, "Reset Settings",
        "Are you sure you want to reset all settings to defaults?",
        QMessageBox::Yes | QMessageBox::No);
    
    if (reply == QMessageBox::Yes) {
        m_optimizationCombo->setCurrentText("High");
        m_threadSpin->setValue(QThread::idealThreadCount());
        m_threadSlider->setValue(QThread::idealThreadCount());
        m_chunkSizeSpin->setValue(1024);
        m_hardwareAccelCombo->setCurrentText("CPU Only");
        m_keyDerivationCombo->setCurrentText("SHA-256 Simple");
        m_gpuAccelCheck->setChecked(false);
        m_smartPatternsCheck->setChecked(true);
        m_commonPasswordsCheck->setChecked(true);
        
        updateSystemInfo();
        QMessageBox::information(this, "Settings Reset", "All settings have been reset to defaults.");
    }
}

void SettingsWindow::optimizeMemory()
{
    QMessageBox::information(this, "Memory Optimization", 
        "Memory optimization completed. This would trigger garbage collection in the actual implementation.");
}

void SettingsWindow::detectHardware()
{
    QString detection = "🔍 <b>Hardware Detection Results</b><br><br>";
    
    detection += "CPU Cores: " + QString::number(QThread::idealThreadCount()) + "<br>";
    detection += "Architecture: " + QSysInfo::currentCpuArchitecture() + "<br>";
    detection += "OS: " + QSysInfo::prettyProductName() + "<br>";
    
    // Mock GPU detection
    detection += "<br>🎮 <b>GPU Detection</b><br>";
    detection += "GPU Vendor: Intel/AMD/NVIDIA (Mock)<br>";
    detection += "GPU Model: Integrated Graphics (Mock)<br>";
    detection += "GPU Memory: 2GB (Mock)<br>";
    detection += "OpenGL Support: Yes (Mock)<br>";
    
    QMessageBox::information(this, "Hardware Detection", detection);
}