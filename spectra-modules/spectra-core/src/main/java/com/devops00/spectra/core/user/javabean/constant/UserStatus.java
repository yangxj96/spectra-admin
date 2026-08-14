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

package com.devops00.spectra.core.user.javabean.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
public enum UserStatus {

    /**
     * 正常账号，可以参与认证。
     */
    ACTIVE("ACTIVE"),

    /**
     * 由安全策略临时锁定，例如连续登录失败。
     */
    LOCKED("LOCKED"),

    /**
     * 管理员主动禁用，重新启用后也必须重新认证。
     */
    DISABLED("DISABLED"),

    /**
     * 已离职/退出组织；重新入职不得自动恢复旧授权。
     */
    DEPARTED("DEPARTED");

    @EnumValue
    @JsonValue
    private final String code;

    UserStatus(String code) {
        this.code = code;
    }

    /**
     * 判断是否允许创建新的认证 Session。
     *
     * @return 只有 ACTIVE 可以登录
     */
    public boolean loginAllowed() {
        return this == ACTIVE;
    }

    /**
     * 校验用户生命周期状态转换。
     * <p>
     * DEPARTED 重新入职是显式的恢复流程，调用方仍必须重新建立 RoleAssignment，
     * 本枚举不会恢复任何历史授权。
     *
     * @param target 目标状态
     * @throws IllegalArgumentException 状态转换不被允许
     */
    public void assertTransitionTo(UserStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("目标用户状态不能为空");
        }
        if (this == target) {
            return;
        }
        boolean allowed = switch (this) {
            case ACTIVE -> target == LOCKED || target == DISABLED || target == DEPARTED;
            case LOCKED -> target == ACTIVE || target == DISABLED || target == DEPARTED;
            case DISABLED -> target == ACTIVE || target == DEPARTED;
            case DEPARTED -> target == ACTIVE;
        };
        if (!allowed) {
            throw new IllegalArgumentException("不允许的用户状态转换: " + this + " -> " + target);
        }
    }

    /**
     * 生命周期变化会改变认证安全状态，必须撤销该用户全部 Session。
     *
     * @param target 目标状态
     * @return 是否需要撤销 Session
     */
    public boolean requiresSessionRevocation(UserStatus target) {
        assertTransitionTo(target);
        return this != target;
    }
}
