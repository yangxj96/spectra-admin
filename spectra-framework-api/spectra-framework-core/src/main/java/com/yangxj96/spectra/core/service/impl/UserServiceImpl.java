package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.javabean.entity.Account;
import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;
import com.yangxj96.spectra.core.mapper.UserMapper;
import com.yangxj96.spectra.core.service.AccountService;
import com.yangxj96.spectra.core.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private AccountService accountService;

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName())
                .orderByAsc(User::getCreatedAt);
        Page<User> users = this.page(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        IPage<UserPageVO> result = new Page<>();
        BeanUtils.copyProperties(users, result);
        ArrayList<UserPageVO> vos = new ArrayList<>();
        // 查询账号列表
        List<Account> accounts = accountService.listByIds(users.getRecords().stream().map(User::getId).toList());
        // 角色列表
        for (User user : users.getRecords()) {
            UserPageVO vo = new UserPageVO();
            BeanUtils.copyProperties(user, vo);
            vo.setState(accounts.stream().filter(i -> i.getId().equals(user.getId())).findFirst().orElse(new Account()).getEnable());
            vos.add(vo);
        }

        result.setRecords(vos);
        return result;
    }
}
