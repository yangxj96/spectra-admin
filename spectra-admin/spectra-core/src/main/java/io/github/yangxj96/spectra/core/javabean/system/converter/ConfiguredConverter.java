package io.github.yangxj96.spectra.core.javabean.system.converter;


import io.github.yangxj96.spectra.core.javabean.system.entity.Configured;
import io.github.yangxj96.spectra.core.javabean.system.vo.ConfiguredVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 系统配置Mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/11/06
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfiguredConverter {


    /**
     * 数据库实体转VO
     *
     * @param configured 数据库实体
     * @return VO
     */
    ConfiguredVO toVO(Configured configured);

    /**
     * 数据库实体列表转VO列表
     *
     * @param configureds 数据库实体列表
     * @return VO列表
     */
    List<ConfiguredVO> toVOs(List<Configured> configureds);

}
