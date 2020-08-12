package com.gitee.randomobject.condition.subCondition;


import com.gitee.randomobject.condition.SormCondition;

import java.util.List;

/**
 * 关联查询类
 * @param <T>
 */
public interface SubCondition<T> {

    SubCondition leftJoin();

    SubCondition rightJoin();

    <T> SubCondition<T> joinTable(Class<T> _class, String primaryField, String joinTableField);

    <T> SubCondition<T> joinTable(Class<T> _class, String primaryField, String joinTableField, String compositeField);

    SubCondition addNullQuery(String field);

    SubCondition addNotNullQuery(String field);

    SubCondition addNotEmptyQuery(String field);

    SubCondition addInQuery(String field, Object[] values);

    SubCondition addInQuery(String field, List<? extends Object> values);

    SubCondition addNotInQuery(String field, Object[] values);

    SubCondition addNotInQuery(String field, List<? extends Object> values);

    SubCondition addQuery(String query);

    SubCondition addQuery(String property, Object value);

    SubCondition addQuery(String property, String operator, Object value);

    /**
     * 根据指定字段升序排列
     *
     * @param field 升序排列字段名
     */
    SubCondition orderBy(String field);

    /**
     * 根据指定字段降序排列
     *
     * @param field 降序排列字段名
     */
    SubCondition orderByDesc(String field);

    SubCondition doneSubCondition();

    SormCondition done();
}
