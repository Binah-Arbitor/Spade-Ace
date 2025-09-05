#include "CryptoUtils.h"
#include <QCryptographicHash>
#include <QRandomGenerator>
#include <QFile>
#include <QDebug>

QByteArray CryptoUtils::sha256Simple(const QString &password, const QByteArray &salt)
{
    QCryptographicHash hash(QCryptographicHash::Sha256);
    hash.addData(password.toUtf8());
    if (!salt.isEmpty()) {
        hash.addData(salt);
    }
    return hash.result();
}

QByteArray CryptoUtils::pbkdf2(const QString &password, const QByteArray &salt, int iterations)
{
    // Simplified PBKDF2 implementation
    // In a real implementation, you would use a proper cryptographic library
    QByteArray result = password.toUtf8() + salt;
    
    for (int i = 0; i < iterations; ++i) {
        QCryptographicHash hash(QCryptographicHash::Sha256);
        hash.addData(result);
        result = hash.result();
    }
    
    return result;
}

QByteArray CryptoUtils::scrypt(const QString &password, const QByteArray &salt, int N, int r, int p)
{
    // Mock Scrypt implementation
    // In a real implementation, you would use a proper scrypt library
    Q_UNUSED(N)
    Q_UNUSED(r)
    Q_UNUSED(p)
    
    return pbkdf2(password, salt, 10000);
}

QByteArray CryptoUtils::aesEncrypt(const QByteArray &data, const QByteArray &key, const QByteArray &iv)
{
    // Mock AES encryption
    // In a real implementation, you would use OpenSSL or similar
    Q_UNUSED(iv)
    
    QByteArray result = data;
    for (int i = 0; i < data.size(); ++i) {
        result[i] = result[i] ^ key[i % key.size()];
    }
    
    return result;
}

QByteArray CryptoUtils::aesDecrypt(const QByteArray &data, const QByteArray &key, const QByteArray &iv)
{
    // Mock AES decryption (same as encryption with XOR)
    return aesEncrypt(data, key, iv);
}

QByteArray CryptoUtils::readFileHeader(const QString &filePath, int bytes)
{
    QFile file(filePath);
    if (!file.open(QIODevice::ReadOnly)) {
        return QByteArray();
    }
    
    return file.read(bytes);
}

bool CryptoUtils::isEncryptedFile(const QString &filePath)
{
    QByteArray header = readFileHeader(filePath, 16);
    if (header.isEmpty()) {
        return false;
    }
    
    // Check for common file signatures
    // If file doesn't start with known signatures, it might be encrypted
    
    // Common file signatures
    QList<QByteArray> knownSignatures = {
        QByteArray::fromHex("504B0304"), // ZIP
        QByteArray::fromHex("504B0506"), // ZIP empty
        QByteArray::fromHex("504B0708"), // ZIP spanned
        QByteArray::fromHex("25504446"), // PDF
        QByteArray::fromHex("89504E47"), // PNG
        QByteArray::fromHex("FFD8FFE0"), // JPEG
        QByteArray::fromHex("FFD8FFE1"), // JPEG
        QByteArray::fromHex("474946383761"), // GIF87a
        QByteArray::fromHex("474946383961"), // GIF89a
    };
    
    for (const QByteArray &signature : knownSignatures) {
        if (header.startsWith(signature)) {
            return false; // Known file format, probably not encrypted
        }
    }
    
    // Check entropy (simplified)
    int uniqueBytes = 0;
    QList<int> byteCount(256, 0);
    
    for (int i = 0; i < header.size(); ++i) {
        unsigned char byte = static_cast<unsigned char>(header[i]);
        if (byteCount[byte] == 0) {
            uniqueBytes++;
        }
        byteCount[byte]++;
    }
    
    // If file has high entropy (many unique bytes), it might be encrypted
    double entropy = (double)uniqueBytes / 256.0;
    return entropy > 0.6; // Threshold for considering file encrypted
}

QString CryptoUtils::detectEncryptionType(const QString &filePath)
{
    QByteArray header = readFileHeader(filePath, 32);
    if (header.isEmpty()) {
        return "Unknown";
    }
    
    // Check for specific encryption headers/signatures
    if (header.contains("Salted__")) {
        return "OpenSSL";
    }
    
    if (header.startsWith(QByteArray::fromHex("504B0304"))) {
        return "ZIP (possibly encrypted)";
    }
    
    // Check file extension
    QString extension = filePath.split('.').last().toLower();
    if (extension == "aes" || extension == "enc") {
        return "AES encrypted file";
    }
    
    if (extension == "gpg" || extension == "pgp") {
        return "GPG/PGP encrypted";
    }
    
    if (extension == "7z") {
        return "7-Zip (possibly encrypted)";
    }
    
    if (extension == "rar") {
        return "RAR (possibly encrypted)";
    }
    
    return "Unknown encryption";
}

QByteArray CryptoUtils::generateSalt(int length)
{
    QByteArray salt;
    salt.resize(length);
    
    for (int i = 0; i < length; ++i) {
        salt[i] = static_cast<char>(QRandomGenerator::global()->bounded(256));
    }
    
    return salt;
}

QByteArray CryptoUtils::generateIV(int length)
{
    return generateSalt(length);
}

QString CryptoUtils::bytesToHex(const QByteArray &bytes)
{
    return bytes.toHex();
}

QByteArray CryptoUtils::hexToBytes(const QString &hex)
{
    return QByteArray::fromHex(hex.toUtf8());
}