package io.github.yangxj96.spectra.common.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 分片工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/5 14:19
 */
public class ChunkUtils {

    public static Path getFileRoot(Path root, String fileMd5) {
        return root.resolve(fileMd5);
    }


    public static Path getChunkFile(Path root, String fileMd5, int chunkIndex) {
        return getFileRoot(root, fileMd5).resolve(String.valueOf(chunkIndex));
    }


    public static void ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }


    public static void mergeChunks(Path dest, Path chunksDir, int totalChunks) throws IOException {
        ensureDir(dest.getParent());
        try (var out = Files.newOutputStream(dest)) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunk = chunksDir.resolve(String.valueOf(i));
                Files.copy(chunk, out);
            }
        }
    }

}
