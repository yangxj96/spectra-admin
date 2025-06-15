package com.yangxj96.spectra.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;

/**
 * 用户service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface UserService extends BaseService<User> {

    /**
     * 分页查询用户列表
     *
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 分页结果
     */
    IPage<UserPageVO> page(PageFrom page, UserPageFrom params);

}
