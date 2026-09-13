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

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/**
 * 文件引用事务端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface FileReferenceService {

    /**
     * 创建或保存文件引用数据，并返回接口约定的对象或回执。
     *
     * @param command 待登记的文件引用命令，包含文件标识、引用对象类型和引用对象 ID。
     * @return 登记文件与业务对象的引用关系并返回引用视图；注册失败时抛出业务异常，不返回 null。
     */
    FileReferenceView register(FileReferenceCommand command);

    /**
     * 删除、撤销或归档文件引用数据；重复调用按接口约定保持幂等。
     *
     * @param key 目标缓存或引用记录的键，用于定位存储数据。
     */
    void remove(FileReferenceKey key);

    /**
     * 删除、撤销或归档文件引用数据；重复调用按接口约定保持幂等。
     *
     * @param referenceId 被引用资源的唯一标识。
     */
    void removeById(UUID referenceId);

    /**
     * 删除、撤销或归档文件引用数据；重复调用按接口约定保持幂等。
     *
     * @param referenceType 被引用资源的类型编码，用于选择对应权限规则。
     * @param referenceId   被引用资源的唯一标识。
     */
    void removeByReference(String referenceType, UUID referenceId);
}
