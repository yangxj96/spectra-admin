package com.yangxj96.spectra.core.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;
import com.yangxj96.spectra.core.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService bindService;

    /**
     * 分页查询用户列表
     *
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 分页结果
     */
    @ULog("分页查询用户列表")
    @GetMapping("/page")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        return bindService.page(page, params);
    }

}
