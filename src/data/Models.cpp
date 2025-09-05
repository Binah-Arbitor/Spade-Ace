#include "Models.h"

QString attackTypeToString(AttackType type) {
    switch (type) {
        case AttackType::BRUTE_FORCE: return "Brute Force";
        case AttackType::DICTIONARY_ATTACK: return "Dictionary Attack";
        case AttackType::RAINBOW_TABLE: return "Rainbow Table";
        case AttackType::HYBRID_ATTACK: return "Hybrid Attack";
        case AttackType::MASK_ATTACK: return "Mask Attack";
        case AttackType::RULE_BASED_ATTACK: return "Rule-based Attack";
        case AttackType::SMART_BRUTE_FORCE: return "Smart Brute Force";
        default: return "Unknown";
    }
}

QString optimizationLevelToString(OptimizationLevel level) {
    switch (level) {
        case OptimizationLevel::LOW: return "Low";
        case OptimizationLevel::MEDIUM: return "Medium";
        case OptimizationLevel::HIGH: return "High";
        case OptimizationLevel::EXTREME: return "Extreme";
        default: return "Unknown";
    }
}

QString hardwareAccelerationToString(HardwareAcceleration accel) {
    switch (accel) {
        case HardwareAcceleration::CPU_ONLY: return "CPU Only";
        case HardwareAcceleration::GPU_ASSISTED: return "GPU Assisted";
        case HardwareAcceleration::HYBRID_MODE: return "Hybrid Mode";
        default: return "Unknown";
    }
}

QString keyDerivationMethodToString(KeyDerivationMethod method) {
    switch (method) {
        case KeyDerivationMethod::SHA256_SIMPLE: return "SHA-256 Simple";
        case KeyDerivationMethod::PBKDF2: return "PBKDF2";
        case KeyDerivationMethod::SCRYPT: return "Scrypt";
        case KeyDerivationMethod::ARGON2: return "Argon2";
        case KeyDerivationMethod::BCRYPT: return "Bcrypt";
        default: return "Unknown";
    }
}

AttackType stringToAttackType(const QString& str) {
    if (str == "Brute Force") return AttackType::BRUTE_FORCE;
    if (str == "Dictionary Attack") return AttackType::DICTIONARY_ATTACK;
    if (str == "Rainbow Table") return AttackType::RAINBOW_TABLE;
    if (str == "Hybrid Attack") return AttackType::HYBRID_ATTACK;
    if (str == "Mask Attack") return AttackType::MASK_ATTACK;
    if (str == "Rule-based Attack") return AttackType::RULE_BASED_ATTACK;
    if (str == "Smart Brute Force") return AttackType::SMART_BRUTE_FORCE;
    return AttackType::BRUTE_FORCE;
}

OptimizationLevel stringToOptimizationLevel(const QString& str) {
    if (str == "Low") return OptimizationLevel::LOW;
    if (str == "Medium") return OptimizationLevel::MEDIUM;
    if (str == "High") return OptimizationLevel::HIGH;
    if (str == "Extreme") return OptimizationLevel::EXTREME;
    return OptimizationLevel::HIGH;
}

HardwareAcceleration stringToHardwareAcceleration(const QString& str) {
    if (str == "CPU Only") return HardwareAcceleration::CPU_ONLY;
    if (str == "GPU Assisted") return HardwareAcceleration::GPU_ASSISTED;
    if (str == "Hybrid Mode") return HardwareAcceleration::HYBRID_MODE;
    return HardwareAcceleration::CPU_ONLY;
}

KeyDerivationMethod stringToKeyDerivationMethod(const QString& str) {
    if (str == "SHA-256 Simple") return KeyDerivationMethod::SHA256_SIMPLE;
    if (str == "PBKDF2") return KeyDerivationMethod::PBKDF2;
    if (str == "Scrypt") return KeyDerivationMethod::SCRYPT;
    if (str == "Argon2") return KeyDerivationMethod::ARGON2;
    if (str == "Bcrypt") return KeyDerivationMethod::BCRYPT;
    return KeyDerivationMethod::SHA256_SIMPLE;
}