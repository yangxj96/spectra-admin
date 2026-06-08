package com.devops00.spectra.ai.starter.utils;


import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;

/**
 * Ai工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 11:04
 */
public class AiUtils {

    /// 生成用户消息
    ///
    /// @param msg 用户问题
    public static Msg generateUserMsg(String msg) {
        return Msg
                .builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(msg).build())
                .build();
    }

}
