QT += core widgets

CONFIG += c++17

TARGET = SpadeAce
TEMPLATE = app

# Define source directory
SOURCEDIR = src

# Include paths
INCLUDEPATH += $$SOURCEDIR

# Source files
SOURCES += \
    $$SOURCEDIR/main.cpp \
    $$SOURCEDIR/MainWindow.cpp \
    $$SOURCEDIR/DecryptionWindow.cpp \
    $$SOURCEDIR/FileOperationsWindow.cpp \
    $$SOURCEDIR/SettingsWindow.cpp \
    $$SOURCEDIR/core/DecryptionEngine.cpp \
    $$SOURCEDIR/core/CryptoUtils.cpp \
    $$SOURCEDIR/data/Models.cpp

# Header files
HEADERS += \
    $$SOURCEDIR/MainWindow.h \
    $$SOURCEDIR/DecryptionWindow.h \
    $$SOURCEDIR/FileOperationsWindow.h \
    $$SOURCEDIR/SettingsWindow.h \
    $$SOURCEDIR/core/DecryptionEngine.h \
    $$SOURCEDIR/core/CryptoUtils.h \
    $$SOURCEDIR/data/Models.h

# Enable debugging symbols in debug mode
CONFIG(debug, debug|release) {
    DEFINES += DEBUG
    TARGET = $$TARGET"_debug"
}

# Optimize for release
CONFIG(release, debug|release) {
    DEFINES += QT_NO_DEBUG_OUTPUT
}

# Output directories
MOC_DIR = build/moc
OBJECTS_DIR = build/obj
RCC_DIR = build/rcc
UI_DIR = build/ui

# Link OpenSSL for encryption
win32 {
    LIBS += -lssl -lcrypto
}
unix {
    LIBS += -lssl -lcrypto
}

# Application metadata
VERSION = 1.0.0
QMAKE_TARGET_COMPANY = "Binah-Arbitor"
QMAKE_TARGET_PRODUCT = "Spade Ace"
QMAKE_TARGET_DESCRIPTION = "High-performance decryption attack tool"
QMAKE_TARGET_COPYRIGHT = "Copyright (c) 2024 Binah-Arbitor"