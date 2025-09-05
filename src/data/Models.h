#ifndef MODELS_H
#define MODELS_H

#include <QString>
#include <QStringList>
#include <QObject>

enum class AttackType {
    BRUTE_FORCE,
    DICTIONARY_ATTACK,
    RAINBOW_TABLE,
    HYBRID_ATTACK,
    MASK_ATTACK,
    RULE_BASED_ATTACK,
    SMART_BRUTE_FORCE
};

enum class OptimizationLevel {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME
};

enum class HardwareAcceleration {
    CPU_ONLY,
    GPU_ASSISTED,
    HYBRID_MODE
};

enum class KeyDerivationMethod {
    SHA256_SIMPLE,
    PBKDF2,
    SCRYPT,
    ARGON2,
    BCRYPT
};

struct AttackConfiguration {
    AttackType attackType = AttackType::BRUTE_FORCE;
    QString targetFile;
    int maxPasswordLength = 8;
    QString characterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    QString dictionaryFile;
    QString rainbowTableFile;
    QString maskPattern = "?l?l?l?l?d?d?d?d"; // ?l=lowercase, ?u=uppercase, ?d=digit, ?s=special
    QString ruleFile;
    int threadCount = 4;
    int chunkSize = 1024 * 1024; // 1MB chunks
    OptimizationLevel optimizationLevel = OptimizationLevel::HIGH;
    HardwareAcceleration hardwareAcceleration = HardwareAcceleration::CPU_ONLY;
    bool enableGpuAcceleration = false;
    KeyDerivationMethod keyDerivationMethod = KeyDerivationMethod::SHA256_SIMPLE;
    bool enableSmartPatterns = true;
    bool commonPasswordsFirst = true;
    bool skipWeakCombinations = false;
};

struct AttackProgress {
    qint64 attemptsCount = 0;
    qint64 totalAttempts = 0;
    double progressPercentage = 0.0;
    qint64 elapsedTime = 0;
    qint64 estimatedTimeRemaining = 0;
    QString currentPassword;
    double attemptsPerSecond = 0.0;
    QString status = "Preparing...";
};

struct AttackResult {
    bool success = false;
    QString foundPassword;
    qint64 timeElapsed = 0;
    qint64 attemptsCount = 0;
    QString errorMessage;
    QStringList candidatePasswords;
};

struct EncryptionAnalysis {
    QString encryptionType;
    QString detectedAlgorithm;
    int keySize = 0;
    QString mode;
    QString padding;
    bool hasIV = false;
    bool hasSalt = false;
    QStringList analysisNotes;
    double confidenceLevel = 0.0;
    qint64 fileSize = 0;
    QString fileHash;
};

// Helper functions for enum to string conversion
QString attackTypeToString(AttackType type);
QString optimizationLevelToString(OptimizationLevel level);
QString hardwareAccelerationToString(HardwareAcceleration accel);
QString keyDerivationMethodToString(KeyDerivationMethod method);

AttackType stringToAttackType(const QString& str);
OptimizationLevel stringToOptimizationLevel(const QString& str);
HardwareAcceleration stringToHardwareAcceleration(const QString& str);
KeyDerivationMethod stringToKeyDerivationMethod(const QString& str);

#endif // MODELS_H