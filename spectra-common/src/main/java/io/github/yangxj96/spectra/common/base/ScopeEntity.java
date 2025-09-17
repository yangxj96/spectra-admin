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

package io.github.yangxj96.spectra.common.base;

/**
 * 范围实体的要求,
 * 这个实体有组织机构ID这个字段,则查询的时候需要进行权限范围的控制
 */
public interface ScopeEntity {

    /**
     * 获取组织机构ID
     *
     * @return 组织机构ID
     */
    Long getOrganizationId();

    /**
     * 设置组织机构ID
     *
     * @param oid 组织机构ID
     */
    void setOrganizationId(Long oid);


}
