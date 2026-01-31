package io.github.yangxj96.spectra.core.controller.system;


import io.github.yangxj96.spectra.core.javabean.system.entity.Region;
import io.github.yangxj96.spectra.core.service.system.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping
    public List<Region> query() {
        return regionService.list();
    }


}
