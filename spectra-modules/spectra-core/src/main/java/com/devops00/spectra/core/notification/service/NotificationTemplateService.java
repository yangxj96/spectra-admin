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

package com.devops00.spectra.core.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplatePreviewVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateGroupVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;

import java.util.List;
import java.util.UUID;

/**
 * 通知模板生命周期服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationTemplateService {

    /**
     * 查询或获取目标数据（{@code page}）。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 模板组、渠道、用途和状态等模板分页筛选条件。
     * @return 返回按分页条件查询的通知模板分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params);

    /**
     * 查询或获取目标数据（{@code groupPage}）。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 模板组、渠道、用途和状态等模板分组分页筛选条件。
     * @return 返回按分页条件查询的通知模板分组分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationTemplateGroupVO> groupPage(PageFrom page, NotificationTemplatePageFrom params);

    /**
     * 查询或获取目标数据（{@code detail}）。
     *
     * @param id 要读取详情的通知模板唯一标识。
     * @return 返回符合条件的通知模板详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    NotificationTemplateVO detail(UUID id);

    /**
     * 创建或构建目标数据（{@code create}）。
     *
     * @param params 模板组编码、名称、渠道、用途、标题/正文模板、参数规则和版本等创建字段。
     * @return 返回已保存的通知模板，包含模板标识、版本和当前状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    NotificationTemplateVO create(NotificationTemplateSaveFrom params);

    /**
     * 更新或推进目标状态（{@code update}）。
     *
     * @param params 已有模板标识及要更新的模板名称、内容、参数规则和版本字段。
     * @return 返回已保存的通知模板，包含模板标识、版本和当前状态；版本冲突或写入失败时抛出业务异常，不返回 null。
     */
    NotificationTemplateVO update(NotificationTemplateSaveFrom params);

    /**
     * 更新或推进目标状态（{@code publish}）。
     *
     * @param id     待发布通知模板的唯一标识。
     * @param params 要发布的模板版本号。
     */
    void publish(UUID id, NotificationTemplateActionFrom params);

    /**
     * 更新或推进目标状态（{@code disable}）。
     *
     * @param id     待停用通知模板的唯一标识。
     * @param params 要停用的模板版本号。
     */
    void disable(UUID id, NotificationTemplateActionFrom params);

    /**
     * 更新或推进目标状态（{@code enable}）。
     *
     * @param id     待启用通知模板的唯一标识。
     * @param params 要启用的模板版本号。
     */
    void enable(UUID id, NotificationTemplateActionFrom params);

    /**
     * 更新或推进目标状态（{@code archive}）。
     *
     * @param id     待归档通知模板的唯一标识。
     * @param params 要归档的模板版本号。
     */
    void archive(UUID id, NotificationTemplateActionFrom params);

    /**
     * 处理内部业务逻辑（{@code versions}）。
     *
     * @param id 要查询历史版本的通知模板唯一标识。
     * @return 返回该模板从旧到新的版本列表；尚无版本时返回空列表，不返回 null。
     */
    List<NotificationTemplateVO> versions(UUID id);

    /**
     * 处理内部业务逻辑（{@code preview}）。
     *
     * @param params 模板标识、渠道、用途、模板文本、参数规则及普通/敏感预览参数。
     * @return 返回模板预览的请求标识和渲染后的渠道内容；模板不存在、参数缺失或渲染失败时抛出异常，不返回 null。
     */
    NotificationTemplatePreviewVO preview(NotificationTemplatePreviewFrom params);
}
