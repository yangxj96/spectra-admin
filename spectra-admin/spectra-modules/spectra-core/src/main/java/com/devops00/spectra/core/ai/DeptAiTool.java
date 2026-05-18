package com.devops00.spectra.core.ai;


import com.devops00.spectra.core.javabean.system.vo.DepartmentTreeVo;
import com.devops00.spectra.core.service.system.DepartmentService;
import io.agentscope.core.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

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
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DeptAiTool {

    private final DepartmentService departmentService;

    @Tool(name = "get_dept_tree", description = "获取部门树形结构数据")
    public List<DepartmentTreeVo> tree() throws IllegalAccessException {
        log.info("进行工具调用");
        return departmentService.tree();
    }

}
