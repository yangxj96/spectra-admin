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

package io.github.yangxj96.spectra.core.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yangxj96.spectra.core.javabean.system.entity.Department;
import org.apache.ibatis.annotations.Param;


/// 组织机构Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-15
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据ID生成组织机构路径
     * <p>如:光谱平台/云南分公司/保山分公司/测试小组</p>
     *
     * @param id 组织机构ID
     * @return 组织机构路径
     */
    String generatePath(@Param("id") String id);
}
