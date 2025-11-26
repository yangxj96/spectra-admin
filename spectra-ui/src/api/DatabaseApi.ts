/**
 *
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/11/26 14:07
 */

import http from "@/plugin/request/index";

export default {
    getDatabaseMonitors() {
        return http.get<string>("/api/actuator/prometheus");
    }
};