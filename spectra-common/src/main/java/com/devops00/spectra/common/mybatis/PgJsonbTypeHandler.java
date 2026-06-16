package com.devops00.spectra.common.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL JSONB 类型映射 (基于 Jackson 3)
 * 完美解决 "字段类型为 jsonb 但表达式类型为 character varying" 的写入断层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/16 17:00
 */
@MappedTypes({Map.class})
@MappedJdbcTypes({JdbcType.OTHER})
public class PgJsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private final ObjectMapper om;

    /**
     * 1. 显式保留无参构造器：防止特定场景下 MyBatis 原生反射报错
     */
    public PgJsonbTypeHandler() {
        this.om = new ObjectMapper(); // 默认兜底
    }

    /**
     * 2. 提供有参构造器：MyBatis-Plus 在通过配置感知注册时能把全局 om 顶进来
     */
    public PgJsonbTypeHandler(ObjectMapper om) {
        this.om = om != null ? om : new ObjectMapper();
    }

    /**
     * ✨ 核心魔法：将 Map 序列化为 JSON 字符串，并将其打包为 PG 特有的 jsonb 对象发送
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String jsonStr = om.writeValueAsString(parameter);

            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(jsonStr);

            ps.setObject(i, pgObject);
        } catch (Exception e) {
            throw new SQLException("PgJsonbTypeHandler 序列化参数失败", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonStr = rs.getString(columnName);
        return parseJson(jsonStr);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonStr = rs.getString(columnIndex);
        return parseJson(jsonStr);
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonStr = cs.getString(columnIndex);
        return parseJson(jsonStr);
    }

    /**
     * 将数据库取出的 JSON 字符串反序列化还原为 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String jsonStr) throws SQLException {
        if (jsonStr == null || jsonStr.isBlank()) {
            return null;
        }
        try {
            return om.readValue(jsonStr, Map.class);
        } catch (Exception e) {
            throw new SQLException("PgJsonbTypeHandler 反序列化失败, 原始文本: " + jsonStr, e);
        }
    }
}