package com.yangxj96.spectra.security.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.User;
import com.yangxj96.spectra.security.entity.from.UserPageFrom;
import com.yangxj96.spectra.security.entity.vo.UserPageVO;
import com.yangxj96.spectra.security.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * @author 杨新杰
 * @since 2025/6/11 14:52
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
    @GetMapping("/page")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        return bindService.page(page, params);
    }

}
