#include "FileOperationsWindow.h"
#include <QDir>
#include <QFileInfo>
#include <QHeaderView>
#include <QStandardPaths>
#include <QMessageBox>
#include <QFileDialog>

FileOperationsWindow::FileOperationsWindow(QWidget *parent)
    : QWidget(parent)
    , m_currentDirectory(QStandardPaths::writableLocation(QStandardPaths::HomeLocation))
{
    setupUI();
    populateFileList(m_currentDirectory);
}

void FileOperationsWindow::setupUI()
{
    m_mainLayout = new QVBoxLayout(this);
    
    // Navigation group
    m_navigationGroup = new QGroupBox("📂 Directory Navigation", this);
    QVBoxLayout *navLayout = new QVBoxLayout(m_navigationGroup);
    
    // Path and buttons
    QHBoxLayout *pathLayout = new QHBoxLayout();
    
    m_pathEdit = new QLineEdit(this);
    m_pathEdit->setText(m_currentDirectory);
    connect(m_pathEdit, &QLineEdit::returnPressed, this, [this]() {
        QString path = m_pathEdit->text();
        if (QDir(path).exists()) {
            m_currentDirectory = path;
            populateFileList(m_currentDirectory);
        }
    });
    
    m_browseButton = new QPushButton("Browse...", this);
    connect(m_browseButton, &QPushButton::clicked, this, &FileOperationsWindow::browseDirectory);
    
    m_upButton = new QPushButton("⬆️ Up", this);
    connect(m_upButton, &QPushButton::clicked, this, &FileOperationsWindow::navigateUp);
    
    m_homeButton = new QPushButton("🏠 Home", this);
    connect(m_homeButton, &QPushButton::clicked, this, &FileOperationsWindow::navigateHome);
    
    m_refreshButton = new QPushButton("🔄 Refresh", this);
    connect(m_refreshButton, &QPushButton::clicked, this, &FileOperationsWindow::refreshFileList);
    
    pathLayout->addWidget(new QLabel("Current Directory:"));
    pathLayout->addWidget(m_pathEdit);
    pathLayout->addWidget(m_browseButton);
    pathLayout->addWidget(m_upButton);
    pathLayout->addWidget(m_homeButton);
    pathLayout->addWidget(m_refreshButton);
    
    // Options
    QHBoxLayout *optionsLayout = new QHBoxLayout();
    m_showHiddenCheck = new QCheckBox("Show hidden files", this);
    connect(m_showHiddenCheck, &QCheckBox::toggled, this, &FileOperationsWindow::toggleHiddenFiles);
    optionsLayout->addWidget(m_showHiddenCheck);
    optionsLayout->addStretch();
    
    navLayout->addLayout(pathLayout);
    navLayout->addLayout(optionsLayout);
    
    m_mainLayout->addWidget(m_navigationGroup);
    
    // File tree
    m_fileTree = new QTreeWidget(this);
    m_fileTree->setHeaderLabels({"Name", "Size", "Type", "Modified"});
    m_fileTree->setRootIsDecorated(false);
    m_fileTree->setAlternatingRowColors(true);
    m_fileTree->setSortingEnabled(true);
    
    // Set column widths
    m_fileTree->header()->resizeSection(0, 300); // Name
    m_fileTree->header()->resizeSection(1, 100); // Size
    m_fileTree->header()->resizeSection(2, 150); // Type
    m_fileTree->header()->resizeSection(3, 150); // Modified
    
    connect(m_fileTree, &QTreeWidget::itemDoubleClicked, this, &FileOperationsWindow::onItemDoubleClicked);
    
    m_mainLayout->addWidget(m_fileTree);
    
    // Status label
    m_statusLabel = new QLabel("Ready", this);
    m_mainLayout->addWidget(m_statusLabel);
}

void FileOperationsWindow::browseDirectory()
{
    QString directory = QFileDialog::getExistingDirectory(this,
        "Select Directory", m_currentDirectory);
    
    if (!directory.isEmpty()) {
        m_currentDirectory = directory;
        m_pathEdit->setText(m_currentDirectory);
        populateFileList(m_currentDirectory);
    }
}

void FileOperationsWindow::navigateUp()
{
    QDir dir(m_currentDirectory);
    if (dir.cdUp()) {
        m_currentDirectory = dir.absolutePath();
        m_pathEdit->setText(m_currentDirectory);
        populateFileList(m_currentDirectory);
    }
}

void FileOperationsWindow::navigateHome()
{
    m_currentDirectory = QStandardPaths::writableLocation(QStandardPaths::HomeLocation);
    m_pathEdit->setText(m_currentDirectory);
    populateFileList(m_currentDirectory);
}

void FileOperationsWindow::refreshFileList()
{
    populateFileList(m_currentDirectory);
}

void FileOperationsWindow::onItemDoubleClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    
    if (!item) return;
    
    QString fileName = item->text(0);
    QString fullPath = m_currentDirectory + "/" + fileName;
    
    QFileInfo fileInfo(fullPath);
    if (fileInfo.isDir()) {
        m_currentDirectory = fileInfo.absoluteFilePath();
        m_pathEdit->setText(m_currentDirectory);
        populateFileList(m_currentDirectory);
    }
}

void FileOperationsWindow::toggleHiddenFiles(bool show)
{
    Q_UNUSED(show)
    populateFileList(m_currentDirectory);
}

void FileOperationsWindow::populateFileList(const QString &directory)
{
    m_fileTree->clear();
    
    QDir dir(directory);
    if (!dir.exists()) {
        m_statusLabel->setText("Directory does not exist: " + directory);
        return;
    }
    
    QDir::Filters filters = QDir::AllEntries | QDir::NoDotAndDotDot;
    if (m_showHiddenCheck->isChecked()) {
        filters |= QDir::Hidden;
    }
    
    QFileInfoList fileList = dir.entryInfoList(filters, QDir::DirsFirst | QDir::Name);
    
    int fileCount = 0;
    int dirCount = 0;
    
    for (const QFileInfo &fileInfo : fileList) {
        QTreeWidgetItem *item = new QTreeWidgetItem(m_fileTree);
        
        // Name with icon
        QString icon = getFileIcon(fileInfo.fileName(), fileInfo.isDir());
        item->setText(0, icon + " " + fileInfo.fileName());
        
        // Size
        if (fileInfo.isDir()) {
            item->setText(1, "");
            dirCount++;
        } else {
            item->setText(1, formatFileSize(fileInfo.size()));
            fileCount++;
        }
        
        // Type
        if (fileInfo.isDir()) {
            item->setText(2, "Directory");
        } else {
            QString suffix = fileInfo.suffix().toUpper();
            if (suffix.isEmpty()) {
                item->setText(2, "File");
            } else {
                item->setText(2, suffix + " File");
            }
        }
        
        // Modified date
        item->setText(3, fileInfo.lastModified().toString("yyyy-MM-dd hh:mm:ss"));
        
        // Set different colors for directories
        if (fileInfo.isDir()) {
            item->setForeground(0, QColor(100, 150, 255));
        }
    }
    
    m_statusLabel->setText(QString("%1 directories, %2 files").arg(dirCount).arg(fileCount));
}

QString FileOperationsWindow::getFileIcon(const QString &fileName, bool isDirectory)
{
    if (isDirectory) {
        return "📁";
    }
    
    QString extension = QFileInfo(fileName).suffix().toLower();
    
    if (extension == "txt" || extension == "log" || extension == "md") {
        return "📄";
    } else if (extension == "zip" || extension == "rar" || extension == "7z" || 
               extension == "tar" || extension == "gz") {
        return "📦";
    } else if (extension == "jpg" || extension == "jpeg" || extension == "png" || 
               extension == "gif" || extension == "bmp") {
        return "🖼️";
    } else if (extension == "mp4" || extension == "avi" || extension == "mov" || 
               extension == "mkv") {
        return "🎬";
    } else if (extension == "mp3" || extension == "wav" || extension == "flac" || 
               extension == "ogg") {
        return "🎵";
    } else if (extension == "pdf") {
        return "📕";
    } else if (extension == "exe" || extension == "msi") {
        return "⚙️";
    } else {
        return "📄";
    }
}

QString FileOperationsWindow::formatFileSize(qint64 bytes)
{
    if (bytes < 1024) {
        return QString::number(bytes) + " B";
    } else if (bytes < 1024 * 1024) {
        return QString::number(bytes / 1024) + " KB";
    } else if (bytes < 1024 * 1024 * 1024) {
        return QString::number(bytes / (1024 * 1024)) + " MB";
    } else {
        return QString::number(bytes / (1024 * 1024 * 1024)) + " GB";
    }
}