/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretExportVO;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretImportPreviewVO;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

/** 使用一次性口令保护密钥导入导出的传输服务。 */
@Service
public class SecretTransferService {

    private static final byte[] MAGIC = "SPECTRA-SECRET-1".getBytes(StandardCharsets.US_ASCII);
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretManagementService managementService;
    private final ObjectMapper objectMapper;

    public SecretTransferService(SecretManagementService managementService,
                                 ObjectMapper objectMapper) {
        this.managementService = managementService;
        this.objectMapper = objectMapper;
    }

    /** 导出指定分类的当前 ACTIVE 版本，并生成不落库的一次性口令。 */
    public SecretExportVO exportCurrent(String category) {
        Map<String, Boolean> exportable = managementService.listDefinitions()
                .stream()
                .collect(Collectors.toMap(definition -> definition.getCode(),
                        definition -> Boolean.TRUE.equals(definition.getExportable())));
        List<SecretManagementService.ActiveSecret> current = managementService.currentActiveSecrets(category)
                .stream()
                .filter(secret -> Boolean.TRUE.equals(exportable.get(secret.code())))
                .toList();
        String passphrase = generatePassphrase();
        byte[] packageBytes = encryptPackage(current, passphrase);
        return new SecretExportVO("spectra-secrets-" + Instant.now().toString().replace(':', '-') + ".bin",
                Base64.getEncoder().encodeToString(packageBytes), passphrase, current.size());
    }

    /** 解密并校验导入包，只返回安全元数据预览。 */
    public SecretImportPreviewVO preview(byte[] packageBytes, String passphrase) {
        List<TransferEntry> entries = decryptPackage(packageBytes, passphrase);
        Set<String> registered = new HashSet<>(managementService.listDefinitions()
                .stream()
                .map(definition -> definition.getCode())
                .toList());
        Set<String> codes = new HashSet<>();
        List<String> conflicts = new ArrayList<>();
        List<SecretImportPreviewVO.Entry> preview = new ArrayList<>();
        for (TransferEntry entry : entries) {
            boolean known = registered.contains(entry.code());
            if (!known) {
                conflicts.add("未注册密钥编码: " + entry.code());
            }
            if (!codes.add(entry.code())) {
                conflicts.add("导入包包含重复密钥编码: " + entry.code());
            }
            preview.add(new SecretImportPreviewVO.Entry(entry.code(), entry.category(), entry.version(),
                    entry.fingerprint(), known));
        }
        return new SecretImportPreviewVO(conflicts.isEmpty(), entries.size(), List.copyOf(preview),
                List.copyOf(conflicts));
    }

    /** 导入包中的值作为新版本写入，所有新版本固定为 PENDING。 */
    @Transactional
    public int importPending(byte[] packageBytes, String passphrase) {
        List<TransferEntry> entries = decryptPackage(packageBytes, passphrase);
        SecretImportPreviewVO preview = preview(packageBytes, passphrase);
        if (!preview.valid()) {
            throw new DataSaveException("导入预校验未通过，请先处理冲突");
        }
        for (TransferEntry entry : entries) {
            managementService.createPending(entry.code(), entry.value(), "IMPORT");
        }
        return entries.size();
    }

    private byte[] encryptPackage(List<SecretManagementService.ActiveSecret> current, String passphrase) {
        try {
            var entries = current.stream()
                    .map(secret -> new TransferEntry(secret.code(), secret.category(), secret.version(),
                            secret.fingerprint(), secret.value()))
                    .toList();
            byte[] plaintext = objectMapper.writeValueAsString(new ExportPayload(1, entries))
                    .getBytes(StandardCharsets.UTF_8);
            byte[] salt = randomBytes(SALT_LENGTH);
            byte[] nonce = randomBytes(NONCE_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt), new GCMParameterSpec(TAG_LENGTH, nonce));
            cipher.updateAAD(MAGIC);
            byte[] ciphertext = cipher.doFinal(plaintext);
            var output = new ByteArrayOutputStream(MAGIC.length + SALT_LENGTH + NONCE_LENGTH + ciphertext.length);
            output.write(MAGIC);
            output.write(salt);
            output.write(nonce);
            output.write(ciphertext);
            return output.toByteArray();
        } catch (GeneralSecurityException | IOException | RuntimeException exception) {
            throw new DataSaveException("生成密钥导出包失败", exception);
        }
    }

    private List<TransferEntry> decryptPackage(byte[] packageBytes, String passphrase) {
        if (packageBytes == null
                || packageBytes.length <= MAGIC.length + SALT_LENGTH + NONCE_LENGTH
                || !StringUtils.hasText(passphrase)) {
            throw new DataSaveException("密钥导入包或导入口令无效");
        }
        try {
            if (!Arrays.equals(MAGIC, Arrays.copyOf(packageBytes, MAGIC.length))) {
                throw new DataSaveException("密钥导入包格式无效");
            }
            int offset = MAGIC.length;
            byte[] salt = Arrays.copyOfRange(packageBytes, offset, offset + SALT_LENGTH);
            offset += SALT_LENGTH;
            byte[] nonce = Arrays.copyOfRange(packageBytes, offset, offset + NONCE_LENGTH);
            offset += NONCE_LENGTH;
            byte[] ciphertext = Arrays.copyOfRange(packageBytes, offset, packageBytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt), new GCMParameterSpec(TAG_LENGTH, nonce));
            cipher.updateAAD(MAGIC);
            String json = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            ExportPayload payload = objectMapper.readValue(json, ExportPayload.class);
            if (payload.formatVersion() != 1 || payload.entries() == null) {
                throw new DataSaveException("密钥导入包版本不受支持");
            }
            return payload.entries();
        } catch (DataSaveException exception) {
            throw exception;
        } catch (GeneralSecurityException | RuntimeException exception) {
            throw new DataSaveException("密钥导入包校验失败", exception);
        }
    }

    private static SecretKeySpec deriveKey(String passphrase, byte[] salt) throws GeneralSecurityException {
        if (!StringUtils.hasText(passphrase) || passphrase.length() < 20) {
            throw new GeneralSecurityException("导入口令强度不足");
        }
        var spec = new PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
        byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(key, "AES");
    }

    private static String generatePassphrase() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(24));
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private record ExportPayload(int formatVersion, List<TransferEntry> entries) {
    }

    private record TransferEntry(String code, String category, int version, String fingerprint, String value) {
    }
}
