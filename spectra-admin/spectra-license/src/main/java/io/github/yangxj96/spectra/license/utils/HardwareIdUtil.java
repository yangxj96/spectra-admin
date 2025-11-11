/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.license.utils;

import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 硬件工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
public class HardwareIdUtil {

    private HardwareIdUtil() {
    }

    public static String generateHWID() {
        var si = new SystemInfo();
        var hal = si.getHardware();

        var sb = new StringBuilder();

        // CPU ID
        var cpu = hal.getProcessor();
        sb.append(cpu.getProcessorIdentifier().getProcessorID());

        // MAC Address (first non-loopback)
        try {
            var nets = hal.getNetworkIFs();
            var mac = nets.stream()
                    .filter(n -> n.getMacaddr() != null && !n.getMacaddr().isEmpty())
                    .map(NetworkIF::getMacaddr)
                    .findFirst()
                    .orElse("00:00:00:00:00:00");
            sb.append(mac);
        } catch (Exception e) {
            sb.append("NO_MAC");
            log.error("NO_MAC", e);
        }

        // 主机名
        try {
            sb.append(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            sb.append("UNKNOWN_HOST");
            log.error("UNKNOWN_HOST", e);
        }

        return sha256(sb.toString());
    }

    private static String sha256(String input) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var hash = md.digest(input.getBytes());
            var sb = new StringBuilder();
            for (var b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("ERROR_HASH", e);
            return "ERROR_HASH";
        }
    }

}
