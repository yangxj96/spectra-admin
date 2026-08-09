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

package com.devops00.spectra.upload.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.from.FilePageFrom;
import com.devops00.spectra.upload.javabean.vo.FileInfoVO;

import java.util.UUID;

/**
 * 文件信息服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/4/2 11:35
 */
public interface FileInfoService extends BaseService<FileInfo> {

    /**
     * 根据hash值查询文件是否已经上穿过
     *
     * @param hash
     *            hash值
     * @return 文件信息，可能为null
     */
    FileInfo findByHash(String hash);

    /**
     * 增加引用计数
     */
    void incrRefCount(UUID id);

    /**
     * 分页查询文件列表
     *
     * @param page
     *            分页参数
     * @param params
     *            查询参数
     * @return 分页结果
     */
    IPage<FileInfoVO> page(PageFrom page, FilePageFrom params);

    /**
     * 根据ID删除文件(软删除)
     *
     * @param id
     *            文件ID
     */
    void deleteById(UUID id);
}
