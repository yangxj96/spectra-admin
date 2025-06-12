package com.yangxj96.spectra.security.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.base.BaseService;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.User;
import com.yangxj96.spectra.security.entity.from.UserPageFrom;
import com.yangxj96.spectra.security.entity.vo.UserPageVO;

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
