package com.yangxj96.spectra.service.auth.share;

import com.yangxj96.spectra.service.auth.javabean.mapstruct.AccountMapstruct;
import com.yangxj96.spectra.service.auth.properties.UserProperties;
import com.yangxj96.spectra.service.auth.service.AccountService;
import com.yangxj96.spectra.share.javabean.ShareAccount;
import com.yangxj96.spectra.share.service.ShareAccountService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 共享账号接口实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Service
public class ShareAccountServiceImpl implements ShareAccountService {

    @Resource
    private AccountService accountService;

    @Resource
    private AccountMapstruct mapstruct;

    @Resource
    private UserProperties userProperties;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean save(ShareAccount account) {
        var entity = mapstruct.shareToEntity(account);
        // 补充下默认密码
        entity.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
        return accountService.save(entity);
    }

    @Override
    public ShareAccount getDefaultAccountByUserId(Long uid) {
        var account = accountService.getDefaultAccountByUserId(uid);
        return mapstruct.entityToShare(account);
    }

    @Override
    public boolean updateById(ShareAccount account) {
        var entity = mapstruct.shareToEntity(account);
        return accountService.updateById(entity);
    }

    @Override
    public List<ShareAccount> getByUserId(Long id) {
        var accounts = accountService.getByUserId(id);
        return mapstruct.entityToShares(accounts);
    }

    @Override
    public void removeByUserId(Long id) {
        accountService.removeByUserId(id);
    }
}
