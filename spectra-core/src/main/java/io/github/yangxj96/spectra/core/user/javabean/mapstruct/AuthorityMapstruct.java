package io.github.yangxj96.spectra.core.user.javabean.mapstruct;

import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 权限mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring")
public interface AuthorityMapstruct {

    /**
     * 实体转VO
     *
     * @param entity 实体对象
     * @return VO对象
     */
    AuthorityVO toVO(Authority entity);

    /**
     * 实体列表转VO列表
     *
     * @param coll 实体列表
     * @return VO列表
     */
    List<AuthorityVO> toVOS(List<Authority> coll);

}
