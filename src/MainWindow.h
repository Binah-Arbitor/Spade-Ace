#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QTabWidget>
#include <QMenuBar>
#include <QStatusBar>
#include <QToolBar>
#include <QAction>
#include <QLabel>

class DecryptionWindow;
class FileOperationsWindow;
class SettingsWindow;

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void showAbout();
    void showHelp();
    void onTabChanged(int index);

private:
    void setupUI();
    void setupMenuBar();
    void setupToolBar();
    void setupStatusBar();

    QTabWidget *m_tabWidget;
    DecryptionWindow *m_decryptionWindow;
    FileOperationsWindow *m_fileOperationsWindow;
    SettingsWindow *m_settingsWindow;
    
    QLabel *m_statusLabel;
    QAction *m_aboutAction;
    QAction *m_helpAction;
    QAction *m_exitAction;
};

#endif // MAINWINDOW_H