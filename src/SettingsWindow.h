#ifndef SETTINGSWINDOW_H
#define SETTINGSWINDOW_H

#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QGridLayout>
#include <QGroupBox>
#include <QLabel>
#include <QSpinBox>
#include <QComboBox>
#include <QCheckBox>
#include <QPushButton>
#include <QSlider>
#include <QTextEdit>
#include "data/Models.h"

class SettingsWindow : public QWidget
{
    Q_OBJECT

public:
    explicit SettingsWindow(QWidget *parent = nullptr);

private slots:
    void onOptimizationLevelChanged();
    void onHardwareAccelerationChanged();
    void onThreadCountChanged();
    void resetToDefaults();
    void optimizeMemory();
    void detectHardware();

private:
    void setupUI();
    void setupPerformanceSettings();
    void setupHardwareSettings();
    void setupSystemInfo();
    void updateSystemInfo();
    
    QVBoxLayout *m_mainLayout;
    
    // Performance settings
    QGroupBox *m_performanceGroup;
    QComboBox *m_optimizationCombo;
    QSpinBox *m_threadSpin;
    QSlider *m_threadSlider;
    QSpinBox *m_chunkSizeSpin;
    
    // Hardware settings
    QGroupBox *m_hardwareGroup;
    QComboBox *m_hardwareAccelCombo;
    QComboBox *m_keyDerivationCombo;
    QCheckBox *m_gpuAccelCheck;
    QCheckBox *m_smartPatternsCheck;
    QCheckBox *m_commonPasswordsCheck;
    
    // System info
    QGroupBox *m_systemGroup;
    QTextEdit *m_systemInfoText;
    QPushButton *m_detectButton;
    
    // Control buttons
    QPushButton *m_resetButton;
    QPushButton *m_optimizeButton;
    
    AttackConfiguration m_config;
};

#endif // SETTINGSWINDOW_H