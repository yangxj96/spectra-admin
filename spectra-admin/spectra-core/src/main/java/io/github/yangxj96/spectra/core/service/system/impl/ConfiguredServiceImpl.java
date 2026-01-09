package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.javabean.system.converter.ConfiguredConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Configured;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredFrom;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredPageFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.ConfiguredVO;
import io.github.yangxj96.spectra.core.mapper.system.ConfiguredMapper;
import io.github.yangxj96.spectra.core.service.system.ConfiguredService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


/// 系统配置Service层默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-06
@Service
public class ConfiguredServiceImpl extends BaseServiceImpl<ConfiguredMapper, Configured> implements ConfiguredService {

    private final ConfiguredConverter configuredConverter;

    public ConfiguredServiceImpl(ConfiguredConverter configuredConverter) {
        this.configuredConverter = configuredConverter;
    }


    @Override
    @Transactional
    public void modify(ConfiguredFrom params) {
        var db = this.getById(params.getId());
        if (db == null) {
            throw new DataNotExistException("系统配置不存在");
        }
        db.setValue(params.getValue());
        db.setRemarks(params.getRemarks());
        this.updateById(db);
    }

    @Override
    public IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params) {
        // 条件构建
        var wrapper = new LambdaQueryWrapper<Configured>()
                .like(StrUtils.isNotBlank(params.getKey()), Configured::getKey, params.getKey());
        // 查询并转换相关内容
        var db = this.page(page.toPage(), wrapper);
        return configuredConverter.toVOPage(db);
    }

    @Override
    public Object json() {
        var listed = list();
        Map<String, Object> result = new HashMap<>();
        for (var cfg : listed) {
            String[] keys = cfg.getKey().split("\\.");
            Map<String, Object> current = result;
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (i == keys.length - 1) {
                    current.put(k, parseValue(cfg.getValue()));
                } else {
                    Object next = current.get(k);
                    if (next == null) {
                        Map<String, Object> child = new HashMap<>();
                        current.put(k, child);
                        current = child;
                    } else if (next instanceof Map) {
                        //noinspection unchecked
                        current = (Map<String, Object>) next;
                    } else {
                        throw new IllegalStateException("配置 key 冲突: " + cfg.getKey());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 转换值
     *
     * @param value 值
     * @return 转换后的结果
     */
    private Object parseValue(String value) {
        if (value == null) return null;

        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;

        if (value.matches("-?\\d+")) {
            return Long.parseLong(value);
        }

        if (value.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(value);
        }

        return value;
    }

}
