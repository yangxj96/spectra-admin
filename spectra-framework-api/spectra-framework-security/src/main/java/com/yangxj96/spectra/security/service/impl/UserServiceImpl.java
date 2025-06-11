/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.core.base.BaseServiceImpl;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.Account;
import com.yangxj96.spectra.security.entity.dto.User;
import com.yangxj96.spectra.security.entity.from.UserPageFrom;
import com.yangxj96.spectra.security.entity.vo.UserPageVO;
import com.yangxj96.spectra.security.mapper.UserMapper;
import com.yangxj96.spectra.security.service.AccountService;
import com.yangxj96.spectra.security.service.RoleService;
import com.yangxj96.spectra.security.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private AccountService accountService;

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName())
                .orderByAsc(User::getCreatedAt)
        ;
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
