package io.github.yangxj96.spectra.license.utils;

import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 硬件工具类
 */
@Slf4j
public class HardwareIdUtil {

    private HardwareIdUtil() {
    }

    public static String generateHWID() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        StringBuilder sb = new StringBuilder();

        // CPU ID
        CentralProcessor cpu = hal.getProcessor();
        sb.append(cpu.getProcessorIdentifier().getProcessorID());

        // MAC Address (first non-loopback)
        try {
            List<NetworkIF> nets = hal.getNetworkIFs();
            String mac = nets.stream()
                    .filter(n -> n.getMacaddr() != null && !n.getMacaddr().isEmpty())
                    .map(NetworkIF::getMacaddr)
                    .findFirst()
                    .orElse("00:00:00:00:00:00");
            sb.append(mac);
        } catch (Exception e) {
            sb.append("NO_MAC");
            log.atError().log("NO_MAC", e);
        }

        // 主机名
        try {
            sb.append(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            sb.append("UNKNOWN_HOST");
            log.atError().log("UNKNOWN_HOST", e);
        }

        return sha256(sb.toString());
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.atError().log("ERROR_HASH", e);
            return "ERROR_HASH";
        }
    }

}
