#ifndef DECRYPTIONWINDOW_H
#define DECRYPTIONWINDOW_H

#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QGridLayout>
#include <QGroupBox>
#include <QLabel>
#include <QLineEdit>
#include <QPushButton>
#include <QComboBox>
#include <QSpinBox>
#include <QProgressBar>
#include <QTextEdit>
#include <QFileDialog>
#include <QTimer>
#include "data/Models.h"

class DecryptionEngine;

class DecryptionWindow : public QWidget
{
    Q_OBJECT

public:
    explicit DecryptionWindow(QWidget *parent = nullptr);

private slots:
    void browseTargetFile();
    void browseDictionaryFile();
    void browseRuleFile();
    void browseRainbowTableFile();
    void analyzeFile();
    void startAttack();
    void stopAttack();
    void onAttackTypeChanged();
    void updateProgress();

private:
    void setupUI();
    void setupFileSelection();
    void setupAttackConfiguration();
    void setupProgress();
    void setupResults();
    void updateAttackTypeOptions();
    
    // UI Components
    QVBoxLayout *m_mainLayout;
    
    // File Selection
    QGroupBox *m_fileGroup;
    QLineEdit *m_targetFileEdit;
    QPushButton *m_browseTargetButton;
    QPushButton *m_analyzeButton;
    
    // Attack Configuration
    QGroupBox *m_configGroup;
    QComboBox *m_attackTypeCombo;
    QSpinBox *m_maxLengthSpin;
    QLineEdit *m_characterSetEdit;
    QLineEdit *m_dictionaryFileEdit;
    QPushButton *m_browseDictionaryButton;
    QLineEdit *m_ruleFileEdit;
    QPushButton *m_browseRuleButton;
    QLineEdit *m_rainbowTableEdit;
    QPushButton *m_browseRainbowButton;
    QLineEdit *m_maskPatternEdit;
    
    // Progress
    QGroupBox *m_progressGroup;
    QProgressBar *m_progressBar;
    QLabel *m_statusLabel;
    QLabel *m_attemptsLabel;
    QLabel *m_timeLabel;
    QLabel *m_speedLabel;
    
    // Controls
    QPushButton *m_startButton;
    QPushButton *m_stopButton;
    
    // Results
    QGroupBox *m_resultsGroup;
    QTextEdit *m_resultsText;
    
    // Data
    AttackConfiguration m_config;
    DecryptionEngine *m_engine;
    QTimer *m_updateTimer;
    bool m_attackRunning;
};

#endif // DECRYPTIONWINDOW_H