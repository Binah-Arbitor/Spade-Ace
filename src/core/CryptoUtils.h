#ifndef CRYPTOUTILS_H
#define CRYPTOUTILS_H

#include <QString>
#include <QByteArray>

class CryptoUtils
{
public:
    // Key derivation functions
    static QByteArray sha256Simple(const QString &password, const QByteArray &salt = QByteArray());
    static QByteArray pbkdf2(const QString &password, const QByteArray &salt, int iterations = 10000);
    static QByteArray scrypt(const QString &password, const QByteArray &salt, int N = 16384, int r = 8, int p = 1);
    
    // Encryption/Decryption
    static QByteArray aesEncrypt(const QByteArray &data, const QByteArray &key, const QByteArray &iv = QByteArray());
    static QByteArray aesDecrypt(const QByteArray &data, const QByteArray &key, const QByteArray &iv = QByteArray());
    
    // File operations
    static QByteArray readFileHeader(const QString &filePath, int bytes = 1024);
    static bool isEncryptedFile(const QString &filePath);
    static QString detectEncryptionType(const QString &filePath);
    
    // Utility functions
    static QByteArray generateSalt(int length = 32);
    static QByteArray generateIV(int length = 16);
    static QString bytesToHex(const QByteArray &bytes);
    static QByteArray hexToBytes(const QString &hex);
    
private:
    CryptoUtils() = default; // Static class
};

#endif // CRYPTOUTILS_H