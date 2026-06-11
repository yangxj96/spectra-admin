package com.devops00.spectra.core.ai;


import com.devops00.spectra.ai.base.AiToolMarker;
import com.devops00.spectra.core.service.user.UserService;
import com.devops00.spectra.ai.base.ToolExecutor;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
public class UserAiTool implements AiToolMarker {

    private final UserService userService;

    /// 获取所有用户信息
    ///
    /// @param token 当前请求token
    @Tool("获取所有用户信息")
    public String getAllUsers(@ToolMemoryId String token) {
        return ToolExecutor.execute(token, _ -> userService.list());
    }

}
