package com.yangxj96.spectra.security.service.impl;

import com.yangxj96.spectra.core.base.BaseServiceImpl;
import com.yangxj96.spectra.security.entity.dto.User;
import com.yangxj96.spectra.security.mapper.UserMapper;
import com.yangxj96.spectra.security.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    protected UserServiceImpl(UserMapper bindMapper) {
        super(bindMapper);
    }
}
