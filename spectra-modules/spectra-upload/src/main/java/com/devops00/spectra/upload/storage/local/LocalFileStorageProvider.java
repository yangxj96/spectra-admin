/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage.local;

import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.properties.LocalProperties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.PartTarget;
import com.devops00.spectra.upload.storage.StorageHealth;
import com.devops00.spectra.upload.storage.StorageMultipart;
import com.devops00.spectra.upload.storage.StorageObject;
import com.devops00.spectra.upload.storage.StorageObjectMetadata;
import com.devops00.spectra.upload.storage.StoragePaths;
import com.devops00.spectra.upload.storage.StoredPart;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Streaming local filesystem provider. */
@Component
public class LocalFileStorageProvider implements FileStorageProvider {

    private final Path storageRoot;
    private final Path stagingRoot;

    public LocalFileStorageProvider(LocalProperties properties) {
        this.storageRoot = Path.of(properties.getStorageRoot()).toAbsolutePath().normalize();
        this.stagingRoot = Path.of(properties.getStagingRoot()).toAbsolutePath().normalize();
    }

    @Override
    public StorageProviderType type() {
        return StorageProviderType.LOCAL;
    }

    @Override
    public StorageMultipart createMultipart(UUID uploadId, String container, String key, int totalParts) {
        try {
            Files.createDirectories(stagingPath(uploadId));
            return new StorageMultipart(container, StoragePaths.resolveRelative(key).toString(), uploadId.toString());
        } catch (IOException e) {
            throw storageFailure("unable to create local staging directory", e);
        }
    }

    @Override
    public PartTarget createPartTarget(StorageMultipart multipart, int partNumber, long partSize, String partSha256,
                                       Instant expiresAt, int attempt) {
        return new PartTarget("PUT", "/file/uploads/" + multipart.providerUploadId() + "/parts/" + partNumber + "/content",
                java.util.Map.of("Content-Type", "application/octet-stream", "Content-Length", Long.toString(partSize)), expiresAt, attempt);
    }

    @Override
    public StoredPart putLocalPart(StorageMultipart multipart, int partNumber, InputStream content, long expectedSize,
                                   String expectedSha256) {
        Path destination = stagingPath(UUID.fromString(multipart.providerUploadId())).resolve(partName(partNumber));
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size;
            try (var digestInput = new java.security.DigestInputStream(content, digest);
                    OutputStream output = Files.newOutputStream(temporary)) {
                size = copyExactly(digestInput, output, expectedSize);
            }
            String actual = HexFormat.of().formatHex(digest.digest());
            if (size != expectedSize) {
                throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "part size does not match declaration");
            }
            if (!actual.equalsIgnoreCase(expectedSha256)) {
                throw new FileUploadException(FileErrorCode.FILE_PART_HASH_MISMATCH, "part hash does not match declaration");
            }
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return new StoredPart(partNumber, size, actual, actual);
        } catch (FileUploadException e) {
            deleteQuietly(temporary);
            throw e;
        } catch (Exception e) {
            deleteQuietly(temporary);
            throw storageFailure("unable to store local upload part", e);
        }
    }

    @Override
    public StoredPart confirmExternalPart(StorageMultipart multipart, int partNumber, long expectedSize,
                                          String expectedSha256, String providerEtag) {
        throw new FileUploadException(FileErrorCode.FILE_UPLOAD_CONFLICT, "local provider requires binary part upload");
    }

    @Override
    public void completeMultipart(StorageMultipart multipart, List<StoredPart> parts) {
        Path sourceDirectory = stagingPath(UUID.fromString(multipart.providerUploadId()));
        Path destination = StoragePaths.resolve(storageRoot, multipart.key());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            Path destinationParent = destination.getParent();
            if (destinationParent == null) {
                throw storageFailure("unable to resolve local upload destination",
                        new IOException("destination parent is missing"));
            }
            Files.createDirectories(destinationParent);
            try (OutputStream output = Files.newOutputStream(temporary)) {
                for (StoredPart part : parts.stream().sorted(java.util.Comparator.comparingInt(StoredPart::partNumber)).toList()) {
                    try (InputStream input = Files.newInputStream(sourceDirectory.resolve(partName(part.partNumber())))) {
                        input.transferTo(output);
                    }
                }
            }
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            deleteQuietly(temporary);
            throw storageFailure("unable to assemble local upload", e);
        }
    }

    @Override
    public void abortMultipart(StorageMultipart multipart) {
        try {
            deleteTree(stagingPath(UUID.fromString(multipart.providerUploadId())));
        } catch (IOException e) {
            throw storageFailure("unable to remove local staging directory", e);
        }
    }

    @Override
    public StorageObject open(String container, String key, Long rangeStart, Long rangeEnd) {
        Path path = StoragePaths.resolve(storageRoot, key);
        try {
            long size = Files.size(path);
            if (size == 0 && rangeStart == null && rangeEnd == null) {
                return new StorageObject(Files.newInputStream(path),
                        new StorageObjectMetadata(0, "application/octet-stream", null, null));
            }
            long start = rangeStart == null ? 0 : rangeStart;
            long end = rangeEnd == null ? size - 1 : Math.min(rangeEnd, size - 1);
            if (start < 0 || start >= size || end < start) {
                throw new FileUploadException(FileErrorCode.FILE_ASSET_NOT_READY, "requested range is invalid");
            }
            InputStream input = Files.newInputStream(path);
            input.skipNBytes(start);
            return new StorageObject(new BoundedInputStream(input, end - start + 1),
                    new StorageObjectMetadata(end - start + 1, "application/octet-stream", null, null));
        } catch (FileUploadException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUploadException(FileErrorCode.FILE_ASSET_NOT_READY, "local file is unavailable", e);
        }
    }

    @Override
    public void delete(String container, String key) {
        try {
            Files.deleteIfExists(StoragePaths.resolve(storageRoot, key));
        } catch (IOException e) {
            throw storageFailure("unable to delete local file", e);
        }
    }

    @Override
    public boolean exists(String container, String key) {
        return Files.isRegularFile(StoragePaths.resolve(storageRoot, key));
    }

    @Override
    public StorageHealth health() {
        try {
            Files.createDirectories(storageRoot);
            Files.createDirectories(stagingRoot);
            var available = Files.isDirectory(storageRoot) && Files.isDirectory(stagingRoot);
            return available
                    ? StorageHealth.available("LOCAL_STORAGE_REACHABLE")
                    : StorageHealth.unavailable("LOCAL_STORAGE_UNAVAILABLE");
        } catch (IOException e) {
            return StorageHealth.unavailable("LOCAL_STORAGE_UNAVAILABLE");
        }
    }

    private Path stagingPath(UUID uploadId) {
        return stagingRoot.resolve(uploadId.toString()).normalize();
    }

    private static String partName(int partNumber) {
        return "part-" + partNumber + ".bin";
    }

    private static long copyExactly(InputStream input, OutputStream output, long expectedSize) throws IOException {
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > expectedSize) {
                throw new FileUploadException(FileErrorCode.FILE_PART_INVALID, "part is larger than declared size");
            }
            output.write(buffer, 0, read);
        }
        return total;
    }

    private static FileUploadException storageFailure(String message, Exception cause) {
        return new FileUploadException(FileErrorCode.FILE_STORAGE_UNAVAILABLE, message, cause);
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The cleanup scheduler retries the owning task.
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static final class BoundedInputStream extends FilterInputStream {
        private long remaining;

        private BoundedInputStream(InputStream input, long remaining) {
            super(input);
            this.remaining = remaining;
        }

        @Override
        public int read() throws IOException {
            if (remaining == 0)
                return -1;
            int value = super.read();
            if (value >= 0)
                remaining--;
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (remaining == 0)
                return -1;
            int read = super.read(bytes, offset, (int) Math.min(length, remaining));
            if (read > 0)
                remaining -= read;
            return read;
        }
    }
}
