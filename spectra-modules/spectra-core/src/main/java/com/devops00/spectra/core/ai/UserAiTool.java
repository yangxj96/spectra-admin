package com.devops00.spectra.core.ai;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.javabean.system.vo.DepartmentTreeVo;
import com.devops00.spectra.core.javabean.user.entity.User;
import com.devops00.spectra.core.service.user.UserService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 部门相关AI调用的tool
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/29 16:06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAiTool {

    private final UserService userService;

    private final ObjectMapper om;

    @Tool("获取所有用户信息")
    public String getAllUsers() {
        log.info("{}DeepSeek 成功触发了本地 [getAllUsers] 工具调用！", LogPrefix.AI.p());
        return om.writeValueAsString(userService.list());
    }

}
