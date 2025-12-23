package io.github.yangxj96.spectra.core.configure.datascope;


import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import io.github.yangxj96.spectra.common.exception.DataScopeViolationException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据拦截器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 14:04
 */
@Slf4j
@Component
public class DataScopeInnerInterceptor implements InnerInterceptor {

    private final Set<String> parseFailCache = ConcurrentHashMap.newKeySet();

    /**
     * 拦截SELECT
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

        final var ctx = DataScopeHolder.get();

        if (ctx == null || ctx.isIgnore() || ctx.getScope() == DataScopeType.ALL) {
            return;
        }

        final var originalSql = boundSql.getSql();

        if (parseFailCache.contains(originalSql)) {
            return;
        }

        try {
            var newSql = processSql(boundSql.getSql(), ctx);
            if (!originalSql.equals(newSql)) {
                MetaObject metaObject = SystemMetaObject.forObject(boundSql);
                metaObject.setValue("sql", newSql);
            }
        } catch (Exception e) {
            parseFailCache.add(originalSql);
            log.error("DataScope SQL 处理失败, mapper={}, sql={}",
                    ms.getId(), originalSql, e);
            // 生产环境建议：失败即放行，避免全站炸
        }
    }


    /**
     * 拦截UPDATE和DELETE
     */
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        DataScopeContext ctx = DataScopeHolder.get();
        if (ctx == null || ctx.isIgnore() || ctx.getScope() == DataScopeType.ALL) {
            return;
        }

        BoundSql boundSql = sh.getBoundSql();
        String originalSql = boundSql.getSql();

        try {
            String newSql = processUpdateDeleteSql(originalSql, ctx);
            if (!originalSql.equals(newSql)) {
                MetaObject metaObject = SystemMetaObject.forObject(boundSql);
                metaObject.setValue("sql", newSql);
            }
        } catch (DataScopeViolationException e) {
            log.warn("DataScope blocked SQL: {}", originalSql);
            throw e;
        } catch (Exception e) {
            log.error("DataScope UPDATE/DELETE SQL 处理失败: {}", originalSql, e);
            // 生产环境：失败放行
        }
    }

    /**
     * 处理UPDATE和DELETE
     *
     * @param sql SQL
     * @param ctx 数据范围上下文
     * @return 处理后的SQL
     * @throws Exception {@link Exception} e
     */
    private String processUpdateDeleteSql(String sql, DataScopeContext ctx) throws Exception {

        Statement stmt = CCJSqlParserUtil.parse(sql);

        if (stmt instanceof Update update) {
            processUpdate(update, ctx);
            return update.toString();
        }

        if (stmt instanceof Delete delete) {
            processDelete(delete, ctx);
            return delete.toString();
        }

        return null;
    }

    /**
     * 处理UPDATE
     *
     * @param update UPDATE
     * @param ctx    数据范围上下文
     */
    private void processUpdate(Update update, DataScopeContext ctx) {

        Table table = update.getTable();
        String scopeField = resolveScopeField(table, ctx);
        if (scopeField == null) {
            return;
        }

        Expression dataScopeExpr = buildScopeExpr(resolveScopeField(table, ctx), ctx);
        if (dataScopeExpr == null) {
            return;
        }

        Expression where = update.getWhere();

        if (where == null) {
            // 🔥 批量 UPDATE 兜底
            throw new DataScopeViolationException("UPDATE without WHERE is forbidden under data scope");
        }

        update.setWhere(new AndExpression(where, dataScopeExpr));
    }

    /**
     * 处理DELETE
     *
     * @param delete DELETE
     * @param ctx    数据范围上下文
     */
    private void processDelete(Delete delete, DataScopeContext ctx) {

        Table table = delete.getTable();
        String scopeField = resolveScopeField(table, ctx);
        if (scopeField == null) {
            return;
        }

        Expression dataScopeExpr = buildScopeExpr(resolveScopeField(table, ctx), ctx);
        if (dataScopeExpr == null) {
            return;
        }

        Expression where = delete.getWhere();
        if (where == null) {
            throw new DataScopeViolationException("DELETE without WHERE is forbidden under data scope");
        }
        delete.setWhere(new AndExpression(where, dataScopeExpr));
    }

    /**
     * 解决范围字段
     *
     * @param table table
     * @param ctx   数据范围上下文
     * @return 解析后
     */
    private String resolveScopeField(Table table, DataScopeContext ctx) {

        if (StringUtils.hasText(ctx.getScopeField())) {
            return ctx.getScopeField();
        }

        String alias = table.getAlias() != null
                ? table.getAlias().getName()
                : table.getName();

        return alias + ".org_id";
    }

    /**
     * SQL 处理入口
     *
     * @param sql 需要处理的SQL
     * @param ctx 数据范围上下文
     * @return 处理完成的SQL
     * @throws Exception {@link Exception} 错误
     */
    private String processSql(String sql, DataScopeContext ctx) throws Exception {
        Statement stmt = CCJSqlParserUtil.parse(sql);
        // 非SELECT SQL不处理
        if (!(stmt instanceof Select select)) {
            return sql;
        }

        if (select.getPlainSelect() != null) {
            processPlainSelect(select.getPlainSelect(), ctx);
        } else if (select.getSetOperationList() != null) {
            processSetOperationList(select.getSetOperationList(), ctx);
        }
        return select.toString();
    }

    /**
     * 处理UNION / UNION ALL
     *
     * @param set {@link SetOperationList} 带UNION / UNION ALL的复杂SQL
     * @param ctx 数据范围上下文
     */
    private void processSetOperationList(SetOperationList set, DataScopeContext ctx) {
        for (var s : set.getSelects()) {
            if (s.getPlainSelect() != null) {
                processPlainSelect(s.getPlainSelect(), ctx);
            }
        }
    }


    /**
     * 处理普通 SELECT
     *
     * @param ps  {@link PlainSelect} 简单的SELECT SQL
     * @param ctx 数据范围上下文
     */
    private void processPlainSelect(PlainSelect ps, DataScopeContext ctx) {

        // 处理 WITH (CTE)
        if (ps.getWithItemsList() != null) {
            for (var with : ps.getWithItemsList()) {
                Select s = with.getSelect();
                if (s.getPlainSelect() != null) {
                    processPlainSelect(s.getPlainSelect(), ctx);
                } else if (s.getSetOperationList() != null) {
                    processSetOperationList(s.getSetOperationList(), ctx);
                }
            }
        }

        // 处理子查询
        processFromItem(ps.getFromItem(), ctx);

        // 是否是count语句
        boolean isCount = isCountSelect(ps);

        // 处理join部分,暂时改为不处理,避免误伤
        //if (ps.getJoins() != null) {
        //    for (Join join : ps.getJoins()) {
        //        processFromItem(join.getRightItem(), ctx);
        //    }
        //}

        // WHERE 注入
        var dataScopeExpr = buildScopeExpr(resolveScopeField(ps, ctx), ctx);
        if (dataScopeExpr == null) {
            return;
        }

        // count SQL：不处理 order by（重要）
        if (isCount) {
            ps.setOrderByElements(null);
        }

        // 合并 WHERE
        var where = ps.getWhere();
        ps.setWhere(where == null ? dataScopeExpr : new AndExpression(where, dataScopeExpr));
    }

    /**
     * FromItem & 子查询
     *
     * @param item {@link FromItem}
     * @param ctx  数据范围上下文
     */
    private void processFromItem(FromItem item, DataScopeContext ctx) {
        if (item instanceof ParenthesedSelect ps) {
            var sub = ps.getSelect();
            if (sub.getPlainSelect() != null) {
                processPlainSelect(sub.getPlainSelect(), ctx);
            } else if (sub.getSetOperationList() != null) {
                processSetOperationList(sub.getSetOperationList(), ctx);
            }
        }
    }


    private Expression buildScopeExpr(String field, DataScopeContext ctx) {
        return switch (ctx.getScope()) {
            case SELF -> new EqualsTo(
                    new Column(field),
                    new LongValue(ctx.getUserId())
            );
            case DEPT, DEPT_AND_CHILD, CUSTOM -> {
                if (CollUtils.isEmpty(ctx.getTargetIds())) {
                    yield new EqualsTo(new LongValue(1), new LongValue(0));
                }
                yield new InExpression(
                        new Column(field),
                        new ExpressionList<>(
                                ctx.getTargetIds().stream()
                                        .limit(1000)
                                        .map(LongValue::new)
                                        .collect(Collectors.toList())
                        )
                );
            }
            default -> null;
        };
    }


    /**
     * 检查 select item 是否是 COUNT(*) / COUNT(x)
     *
     * @param ps {@link PlainSelect} 简单SELECT
     * @return 是否
     */
    private boolean isCountSelect(PlainSelect ps) {
        if (ps.getSelectItems() == null || ps.getSelectItems().size() != 1) {
            return false;
        }

        SelectItem<?> item = ps.getSelectItems().getFirst();
        Expression expr = item.getExpression();

        if (expr instanceof Function func) {
            return "COUNT".equalsIgnoreCase(func.getName());
        }
        return false;
    }

    /**
     * 字段 & 表别名解析（非常关键）
     * 推荐策略：字段优先，其次表别名
     *
     * @param ps  PlainSelect
     * @param ctx DataScopeContext
     * @return String
     */
    private String resolveScopeField(PlainSelect ps, DataScopeContext ctx) {
        // 注解显式指定（如 o.org_id）
        if (StringUtils.hasText(ctx.getScopeField())) {
            return ctx.getScopeField();
        }
        // 没指定 → 不加 DataScope
        return null;
    }

    /**
     * 解析用户字段
     *
     * @param ps 简单SQL
     * @return 用户字段
     */
    private String resolveUserField(PlainSelect ps) {
        FromItem from = ps.getFromItem();
        if (from instanceof Table table) {
            String alias = table.getAlias() != null
                    ? table.getAlias().getName()
                    : table.getName();
            return alias + ".created_by";
        }
        return null;
    }

}
