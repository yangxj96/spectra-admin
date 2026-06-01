package com.devops00.spectra.framework.configure.mybatis.interceptor;


import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.UUID;

/**
 * MP执行的单表SQL拦截处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/5/9 17:35
 */
@Slf4j
@RequiredArgsConstructor
public class DataScopeInnerInterceptor implements MultiDataPermissionHandler {

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {

        UUID userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }

        // TODO 方向已定,还需完善

        // 过滤需要处理权限的表
        //if (!"sys_user".equalsIgnoreCase(table.getName())) {
        //    return null;
        //}

        // 获取当前用户的UUID部门列表
        //List<String> deptIds = Collections.singletonList(UUID.randomUUID().toString());

        // 动态处理别名
        Alias alias = table.getAlias();
        //Column column = (alias != null)
        //        ? new Column(alias.getName() + ".dept_id")
        //        : new Column("dept_id");

        if (alias != null) {
            log.debug("{}别名识别出来了:{}", LogPrefix.PERSISTENCE.p(), alias.getName());
        }

        Column column = new Column("1");

        //List<StringValue> expressions = deptIds.stream()
        //        .map(StringValue::new)
        //        .collect(Collectors.toList());
        //ExpressionList<StringValue> itemsList = new ExpressionList<>(expressions);

        return new EqualsTo(column, new DoubleValue(1));

    }


}
