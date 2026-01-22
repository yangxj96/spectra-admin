export const FileUtils = {
    /**
     * 计算文件hash值
     * @param blob 文件
     */
    async hash(blob: Blob): Promise<string> {
        // 使用现代 API: Blob.arrayBuffer()
        const buffer = await blob.arrayBuffer();
        // 计算 SHA-256
        const hashBuffer = await crypto.subtle.digest("SHA-256", buffer);
        // 转为十六进制字符串
        const hashArray = new Uint8Array(hashBuffer);
        // 计算HASH
        return Array.from(hashArray, byte => byte.toString(16).padStart(2, "0")).join("");
    }
};
