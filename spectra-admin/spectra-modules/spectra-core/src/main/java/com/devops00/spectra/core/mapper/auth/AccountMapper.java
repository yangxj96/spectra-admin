package com.devops00.spectra.core.mapper.auth;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.javabean.auth.entity.Account;
import org.apache.ibatis.annotations.Mapper;

/// 账号Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/11 15:48
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
