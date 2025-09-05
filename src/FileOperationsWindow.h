#ifndef FILEOPERATIONSWINDOW_H
#define FILEOPERATIONSWINDOW_H

#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QTreeWidget>
#include <QLineEdit>
#include <QPushButton>
#include <QLabel>
#include <QCheckBox>
#include <QGroupBox>
#include <QSplitter>

class FileOperationsWindow : public QWidget
{
    Q_OBJECT

public:
    explicit FileOperationsWindow(QWidget *parent = nullptr);

private slots:
    void browseDirectory();
    void navigateUp();
    void navigateHome();
    void refreshFileList();
    void onItemDoubleClicked(QTreeWidgetItem *item, int column);
    void toggleHiddenFiles(bool show);

private:
    void setupUI();
    void populateFileList(const QString &directory);
    QString getFileIcon(const QString &fileName, bool isDirectory);
    QString formatFileSize(qint64 bytes);
    
    QVBoxLayout *m_mainLayout;
    QGroupBox *m_navigationGroup;
    QLineEdit *m_pathEdit;
    QPushButton *m_browseButton;
    QPushButton *m_upButton;
    QPushButton *m_homeButton;
    QPushButton *m_refreshButton;
    QCheckBox *m_showHiddenCheck;
    
    QTreeWidget *m_fileTree;
    QLabel *m_statusLabel;
    
    QString m_currentDirectory;
};

#endif // FILEOPERATIONSWINDOW_H