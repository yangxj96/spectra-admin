package com.yangxj96.spectra.common.base.javabean.vo;

import java.util.List;

/**
 * 所有树形结构 VO 的通用接口
 *
 * @param <T> 具体类型
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface Tree<T> {

    Long getId();

    void setId(Long id);

    Long getPid();

    void setPid(Long pid);

    List<T> getChildren();

    void setChildren(List<T> children);
}
