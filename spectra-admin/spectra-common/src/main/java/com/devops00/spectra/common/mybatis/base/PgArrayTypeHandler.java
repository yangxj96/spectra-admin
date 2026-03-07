package com.devops00.spectra.common.mybatis.base;


import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/// PG数据库数组映射
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 00:02
@MappedJdbcTypes(JdbcType.ARRAY)
public abstract class PgArrayTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private final String pgType;

    protected PgArrayTypeHandler(String pgType) {
        this.pgType = pgType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {

        Connection conn = ps.getConnection();
        Array array = conn.createArrayOf(pgType, parameter.toArray());
        ps.setArray(i, array);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getArray(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getArray(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getArray(columnIndex));
    }

    @SuppressWarnings("unchecked")
    private List<T> convert(Array array) throws SQLException {

        if (array == null) {
            return null;
        }

        Object[] objArray = (Object[]) array.getArray();

        return (List<T>) Arrays.asList(objArray);
    }

}
