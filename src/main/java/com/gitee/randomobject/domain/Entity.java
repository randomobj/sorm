package com.gitee.randomobject.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体类信息
 */
public class Entity {
    /**
     * 实体类对象
     */
    public Class _class;
    /**
     * 实体类表名
     */
    public String tableName;
    /**
     * 实体类名
     */
    public String className;
    /**
     * 表注释
     */
    public String comment;
    /**
     * Id属性
     */
    public Property id;
    /**
     * 属性字段(排除ignore字段和实体包内字段)
     */
    public Property[] properties;
    /**
     * 唯一索引字段
     */
    public Property[] indexProperties;
    /**
     * 唯一约束字段
     */
    public Property[] uniqueKeyProperties;
    /**
     * 外键约束字段
     */
    public Property[] foreignKeyProperties;
    /**
     * Field数组(排除ignore字段和实体包内字段)
     */
    public Field[] fields;
    /**
     * Field数组(实体包类)
     */
    public Field[] compositFields;

    /**
     * 不包含的列名
     */
    public List<String> notColumns = new ArrayList<>();
}
