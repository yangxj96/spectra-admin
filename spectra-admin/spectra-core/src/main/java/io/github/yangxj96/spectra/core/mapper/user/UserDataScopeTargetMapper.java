package io.github.yangxj96.spectra.core.mapper.user;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yangxj96.spectra.core.javabean.user.entity.UserDataScopeTarget;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据范围目标表Mapper
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 11:36
 */
public interface UserDataScopeTargetMapper extends BaseMapper<UserDataScopeTarget> {

    /**
     * 根据用户ID获取数据范围目标信息
     *
     * @param userId 用户ID
     * @return 数据范围目标列表
     */
    List<UserDataScopeTarget> findByUserId(@Param("userId") Long userId);

}
