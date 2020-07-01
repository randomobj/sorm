package com.gitee.randomobject.domain;


import com.gitee.randomobject.dao.AbstractDAO;
import com.gitee.randomobject.helper.SQLHelper;
import com.gitee.randomobject.syntax.SyntaxHandler;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询类辅助类
 */
public final class Query implements Serializable {

    /**
     * 关联类
     */
    public Class _class;
    /**
     * 关联类名
     */
    public String className;
    /**
     * 表名
     */
    public String tableName;
    /**
     * distinct
     */
    public String distinct = "";
    /**
     * 需要返回的列名
     */
    public StringBuilder addColumnBuilder = new StringBuilder();
    /**
     * 不需要返回的列名
     */
    public StringBuilder addNotColumnBuilder = new StringBuilder();
    /**
     * 聚合函数列名
     */
    public StringBuilder aggregateColumnBuilder = new StringBuilder();
    /**
     * 字段更新
     */
    public StringBuilder setBuilder = new StringBuilder();
    /**
     * 查询条件
     */
    public StringBuilder whereBuilder = new StringBuilder();
    /**
     * 分组查询
     */
    public StringBuilder groupByBuilder = new StringBuilder("group by ");
    /**
     * 排序
     */
    public StringBuilder orderByBuilder = new StringBuilder();
    /**
     * 分页
     */
    public String limit = "";
    /**
     * 参数索引
     */
    public int parameterIndex = 1;
    /**
     * 查询参数
     */
    public List parameterList = new ArrayList();
    /**
     * 更新参数
     */
    public List updateParameterList = new ArrayList();
    /**
     * 关联Entity
     */
    public transient Entity entity;
    /**
     * 状态
     */
    public boolean hasDone;
    /**
     * 关联子查询
     */
    public List<SubQuery> subQueryList = new ArrayList<>();
    /**
     * 数据源
     */
    public transient DataSource dataSource;
    /**
     * 存放关联的DAO对象
     */
    public transient AbstractDAO abstractDAO;
    /**
     * 差异语法
     */
    public transient SyntaxHandler syntaxHandler;
    /**
     * SQL帮助类
     */
    public transient SQLHelper sqlHelper;
}
