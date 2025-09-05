#include "MainWindow.h"
#include "DecryptionWindow.h"
#include "FileOperationsWindow.h"
#include "SettingsWindow.h"
#include <QApplication>
#include <QMessageBox>
#include <QDesktopServices>
#include <QUrl>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , m_tabWidget(nullptr)
    , m_decryptionWindow(nullptr)
    , m_fileOperationsWindow(nullptr)
    , m_settingsWindow(nullptr)
    , m_statusLabel(nullptr)
{
    setWindowTitle("Spade Ace - High-Performance Decryption Attack Tool");
    setMinimumSize(1000, 700);
    resize(1200, 800);
    
    setupUI();
    setupMenuBar();
    setupToolBar();
    setupStatusBar();
}

MainWindow::~MainWindow()
{
}

void MainWindow::setupUI()
{
    m_tabWidget = new QTabWidget(this);
    setCentralWidget(m_tabWidget);
    
    // Create tab windows
    m_decryptionWindow = new DecryptionWindow(this);
    m_fileOperationsWindow = new FileOperationsWindow(this);
    m_settingsWindow = new SettingsWindow(this);
    
    // Add tabs
    m_tabWidget->addTab(m_decryptionWindow, "🔐 Decryption Attack");
    m_tabWidget->addTab(m_fileOperationsWindow, "📁 File Operations");
    m_tabWidget->addTab(m_settingsWindow, "⚙️ Settings");
    
    // Connect tab change signal
    connect(m_tabWidget, &QTabWidget::currentChanged, this, &MainWindow::onTabChanged);
}

void MainWindow::setupMenuBar()
{
    QMenuBar *menuBar = this->menuBar();
    
    // File menu
    QMenu *fileMenu = menuBar->addMenu("&File");
    
    m_exitAction = new QAction("E&xit", this);
    m_exitAction->setShortcut(QKeySequence::Quit);
    m_exitAction->setStatusTip("Exit the application");
    connect(m_exitAction, &QAction::triggered, this, &QWidget::close);
    fileMenu->addAction(m_exitAction);
    
    // Help menu
    QMenu *helpMenu = menuBar->addMenu("&Help");
    
    m_helpAction = new QAction("&Help", this);
    m_helpAction->setShortcut(QKeySequence::HelpContents);
    m_helpAction->setStatusTip("Show help documentation");
    connect(m_helpAction, &QAction::triggered, this, &MainWindow::showHelp);
    helpMenu->addAction(m_helpAction);
    
    helpMenu->addSeparator();
    
    m_aboutAction = new QAction("&About", this);
    m_aboutAction->setStatusTip("Show information about this application");
    connect(m_aboutAction, &QAction::triggered, this, &MainWindow::showAbout);
    helpMenu->addAction(m_aboutAction);
}

void MainWindow::setupToolBar()
{
    QToolBar *mainToolBar = addToolBar("Main");
    mainToolBar->addAction(m_helpAction);
    mainToolBar->addSeparator();
    mainToolBar->addAction(m_exitAction);
}

void MainWindow::setupStatusBar()
{
    m_statusLabel = new QLabel("Ready");
    statusBar()->addWidget(m_statusLabel);
    statusBar()->showMessage("Ready", 2000);
}

void MainWindow::showAbout()
{
    QMessageBox::about(this, "About Spade Ace",
        "<h2>Spade Ace v1.0.0</h2>"
        "<p>High-Performance Decryption Attack Tool</p>"
        "<p>Built with Qt Framework</p>"
        "<p>Copyright © 2024 Binah-Arbitor</p>"
        "<p><a href='https://github.com/Binah-Arbitor/Spade-Ace'>GitHub Repository</a></p>");
}

void MainWindow::showHelp()
{
    QDesktopServices::openUrl(QUrl("https://github.com/Binah-Arbitor/Spade-Ace/wiki"));
}

void MainWindow::onTabChanged(int index)
{
    QString tabNames[] = {"Decryption Attack", "File Operations", "Settings"};
    if (index >= 0 && index < 3) {
        m_statusLabel->setText(QString("Current tab: %1").arg(tabNames[index]));
    }
}