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

package com.devops00.spectra.common.port.security;

/**
 * Web 加密请求 replay nonce 的安全管理端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecurityReplayNonceAdminPort {

    /** 返回 nonce 防重放运行态摘要，不返回 nonce、Redis Key 或安全值。 */
    Summary summary();

    /** 将指定 nonce 以摘要形式标记为已消费。 */
    Result invalidate(String nonce);

    /** 按请求时间戳将指定 nonce 以摘要形式标记为已消费。 */
    default Result invalidate(String nonce, long requestTimestamp) {
        return invalidate(nonce);
    }

    /** 推进当前加密请求窗口的 cutoff，并清理此前已记录的 nonce。 */
    Result invalidateAll();

    /** 判断请求时间戳是否位于当前全局失效 cutoff 及之前。 */
    boolean isBeforeOrAtCutoff(long timestamp);

    /**
     * 定义相关数据相关的跨模块调用契约。
     *
     * @param status            业务状态
     * @param cutoffEpochSecond 清理数据使用的截止 Unix 秒数
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    record Summary(String status, Long cutoffEpochSecond) {
    }

    /**
     * 定义结果相关的跨模块调用契约。
     *
     * @param operationType     操作类型
     * @param affectedCount     本次操作影响的对象数量
     * @param cutoffEpochSecond 清理数据使用的截止 Unix 秒数
     * @param status            业务状态
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    record Result(String operationType, long affectedCount, Long cutoffEpochSecond, String status) {
    }
}
