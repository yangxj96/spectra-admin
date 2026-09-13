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

package com.devops00.spectra.core.security.authentication.service;

import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户认证与通知联系方式服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface UserContactService {

    String PHONE = "PHONE";

    String EMAIL = "EMAIL";

    String ACTIVE = "ACTIVE";

    /**
     * 创建或更新用户已验证的联系方式。
     *
     * @param userId      目标用户的唯一标识，用于限定查询或变更范围。
     * @param contactType 联系方式类型，用于选择手机号或邮箱校验规则。
     * @param value       待存储、序列化或转换的业务值。
     * @return 返回用户联系方式；处理失败时抛出业务异常，不返回 null。
     */
    UserContact upsertVerified(UUID userId, String contactType, String value);

    /**
     * 查询用户当前有效联系方式。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合查询条件的用户联系方式列表；无匹配时返回空列表，不返回 null。
     */
    List<UserContact> listActiveByUserId(UUID userId);

    /**
     * 按联系方式类型和关键字查询当前有效联系方式候选。
     *
     * @param contactType 联系方式类型，只支持 PHONE 或 EMAIL。
     * @param keyword     联系方式关键字。
     * @param limit       返回数量上限，由实现进一步限制。
     * @return 返回有效联系方式列表；无匹配时返回空列表。
     */
    List<UserContact> searchActiveByType(String contactType, String keyword, int limit);

    /**
     * 批量查询用户当前有效联系方式。
     *
     * @param userIds 目标用户标识集合，用于批量处理。
     * @return 返回按用户标识分组的UUID映射；没有匹配用户时返回空 Map，不返回 null。
     */
    Map<UUID, List<UserContact>> listActiveByUserIds(List<UUID> userIds);

    /**
     * 撤销用户指定类型的当前有效联系方式。
     *
     * @param userId      目标用户的唯一标识，用于限定查询或变更范围。
     * @param contactType 联系方式类型，用于选择手机号或邮箱校验规则。
     */
    void revokeByUserIdAndType(UUID userId, String contactType);
}
