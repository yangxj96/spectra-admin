package com.yangxj96.spectra.core.user.javabean.vo;

import com.yangxj96.spectra.common.base.javabean.vo.Tree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限树形VO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityTreeVO implements Tree<AuthorityTreeVO>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long pid;

    private String name;

    private String code;

    private List<AuthorityTreeVO> children;

}
