package com.devops00.spectra.core.controller.system;


import com.devops00.spectra.core.javabean.system.vo.RegionVO;
import com.devops00.spectra.core.service.system.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政区划相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/30 15:32
 */
@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /// 懒加载树
    ///
    /// @param level 层级
    /// @param id    父级ID
    /// @return 根据条件获取的下级的列表
    @GetMapping
    public List<RegionVO> lazyTree(Integer level, @RequestParam(value = "id", required = false) String id) {
        return regionService.lazyTree(level, id);
    }


}
