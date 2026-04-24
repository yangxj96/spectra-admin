export type DeviceApi = {
    /**
     * 获取设备ID
     *
     * @return {string} 设备ID
     */
    getDeviceId(): string;
    /**
     * 获取系统信息
     * @return {string} 系统信息
     */
    getSystem(): string;
};
