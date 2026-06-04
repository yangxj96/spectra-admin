// src/utils/crypto-utils.ts

/**
 * Base64 转 Uint8Array
 */
export function base64ToBytes(base64: string): Uint8Array {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes;
}

/**
 * HEX 转 Uint8Array
 */
function hexToBytes(hex: string): Uint8Array {
    const bytes = new Uint8Array(hex.length / 2);
    for (let i = 0; i < hex.length; i += 2) {
        bytes[i / 2] = parseInt(hex.slice(i, i + 2), 16);
    }
    return bytes;
}

/**
 * Uint8Array 转 ArrayBuffer
 */
function toBufferSource(data: Uint8Array): ArrayBuffer {
    return new Uint8Array(data).buffer;
}

/**
 * 导入 RSA 私钥（PKCS#8 格式，Base64 编码）
 */
export async function importRsaPrivateKey(base64Key: string): Promise<CryptoKey> {
    const keyData = base64ToBytes(base64Key);
    return await crypto.subtle.importKey(
        "pkcs8",
        toBufferSource(keyData),
        {
            name: "RSA-OAEP",
            hash: "SHA-256"
        },
        false,
        ["decrypt"]
    );
}

/**
 * 导入 RSA 公钥（X.509 格式，Base64 编码）
 */
export async function importRsaPublicKey(base64Key: string): Promise<CryptoKey> {
    const keyData = base64ToBytes(base64Key);
    return await crypto.subtle.importKey(
        "spki",
        toBufferSource(keyData),
        {
            name: "RSASSA-PKCS1-v1_5",
            hash: "SHA-256"
        },
        false,
        ["verify"]
    );
}

/**
 * RSA-PSS 验签
 */
export async function verifySignature(
    signatureBase64: string,
    dataString: string,
    publicKeyBase64: string
): Promise<boolean> {
    const publicKey = await importRsaPublicKey(publicKeyBase64);
    const signature = base64ToBytes(signatureBase64);
    const encoder = new TextEncoder();
    const data = encoder.encode(dataString);

    return await crypto.subtle.verify(
        {
            name: "RSASSA-PKCS1-v1_5",
            hash: "SHA-256",
            saltLength: 32
        },
        publicKey,
        toBufferSource(signature),
        data
    );
}

/**
 * 使用 RSA-OAEP(SHA-256) 解密 AES 密钥
 */
export async function decryptAesKey(encryptedKeyBase64: string, privateKeyBase64: string): Promise<Uint8Array> {
    const privateKey = await importRsaPrivateKey(privateKeyBase64);
    const encryptedKey = base64ToBytes(encryptedKeyBase64);

    const decryptedKey = await crypto.subtle.decrypt(
        {
            name: "RSA-OAEP",
            hash: "SHA-256"
        } as RsaOaepParams,
        privateKey,
        toBufferSource(encryptedKey)
    );

    return new Uint8Array(decryptedKey);
}

/**
 * 导入 AES-GCM 密钥
 */
export async function importAesKey(keyBytes: Uint8Array): Promise<CryptoKey> {
    return await crypto.subtle.importKey(
        "raw",
        toBufferSource(keyBytes),
        {
            name: "AES-GCM",
            length: 256
        },
        false,
        ["decrypt"]
    );
}

/**
 * 使用 AES-GCM 解密数据
 */
export async function decryptAesGcm(encryptedDataBase64: string, keyBytes: Uint8Array, ivHex: string): Promise<string> {
    const aesKey = await importAesKey(keyBytes);
    const encryptedData = base64ToBytes(encryptedDataBase64);
    const iv = hexToBytes(ivHex);

    const decryptedData = await crypto.subtle.decrypt(
        {
            name: "AES-GCM",
            iv: toBufferSource(iv)
        },
        aesKey,
        toBufferSource(encryptedData)
    );

    const decoder = new TextDecoder();
    return decoder.decode(decryptedData);
}

/**
 * 一键解密
 */
export async function decrypt(
    key: string,
    data: string,
    ivHex: string,
    privateKeyBase64: string
): Promise<string> {
    const aesKeyBytes = await decryptAesKey(key, privateKeyBase64);
    return await decryptAesGcm(data, aesKeyBytes, ivHex);
}
