package com.gitee.randomobject.helper;


import com.gitee.randomobject.domain.Entity;
import com.gitee.randomobject.domain.Property;
import com.gitee.randomobject.syntax.Syntax;
import com.gitee.randomobject.syntax.SyntaxHandler;
import com.gitee.randomobject.util.ReflectionUtil;
import com.gitee.randomobject.util.StringUtil;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SQLHelper implements Serializable {

    private SyntaxHandler syntaxHandler;

    private ConcurrentHashMap<String, String> sqlCache = new ConcurrentHashMap<>();//缓存执行过的SQL语句

    public SQLHelper(SyntaxHandler syntaxHandler) {
        this.syntaxHandler = syntaxHandler;
    }

    /**
     * 返回fetch语句
     */
    public String fetch(Class _class, String property) {
        String key = "fetch_" + _class.getName() + "_" + property;
        if (!sqlCache.containsKey(key)) {
            String tableName = ReflectionUtil.entityMap.get(_class.getName()).tableName;
            String fetchSQL = "select " + columns(_class.getName(), "t") + " from " + syntaxHandler.getSyntax(Syntax.Escape, tableName) + " as t where t." + StringUtil.Camel2Underline(property) + " = ?";
            sqlCache.put(key, fetchSQL);
        }
        return sqlCache.get(key);
    }

    /**
     * 返回fetch语句
     */
    public String fetchNull(Class _class, String property) {
        String key = "fetch_" + _class.getName() + "_" + property;
        if (!sqlCache.containsKey(key)) {
            String tableName = ReflectionUtil.entityMap.get(_class.getName()).tableName;
            String fetchSQL = "select " + columns(_class.getName(), "t") + " from " + syntaxHandler.getSyntax(Syntax.Escape, tableName) + " as t where t." + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property)) + " is null";
            sqlCache.put(key, fetchSQL);
        }
        return sqlCache.get(key);
    }

    /**
     * 返回根据属性删除的语句
     */
    public String delete(Class _class, String property) {
        String key = "delete_" + _class.getName() + "_" + property;
        if (!sqlCache.containsKey(key)) {
            String tableName = ReflectionUtil.entityMap.get(_class.getName()).tableName;
            String fetchSQL = "delete from " + syntaxHandler.getSyntax(Syntax.Escape, tableName) + " where " + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property)) + " = ?";
            sqlCache.put(key, fetchSQL);
        }
        return sqlCache.get(key);
    }

    /**
     * 返回根据属性删除的语句
     */
    public String delete(Class _class) {
        String key = "delete_" + _class.getName();
        if (!sqlCache.containsKey(key)) {
            String tableName = ReflectionUtil.entityMap.get(_class.getName()).tableName;
            String fetchSQL = "delete from " + syntaxHandler.getSyntax(Syntax.Escape, tableName);
            sqlCache.put(key, fetchSQL);
        }
        return sqlCache.get(key);
    }

    /**
     * 返回insertIgnore语句
     */
    public String insertIgnore(Class _class, String insertIgnoreSQL) {
        String key = "insertIgnore_" + insertIgnoreSQL + "_" + _class.getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = ReflectionUtil.entityMap.get(_class.getName());
            builder.append(insertIgnoreSQL + " " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + "(");

            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                builder.append("" + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property.name)) + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") values(");
            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 返回根据UniqueKey更新的SQL语句
     */
    public String updateByUniqueKey(Class _class) {
        String key = "updateByUniqueKey_" + _class.getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = ReflectionUtil.entityMap.get(_class.getName());
            builder.append("update " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + " set ");

            for (Property property : entity.properties) {
                if (property.id || property.unique) {
                    continue;
                }
                builder.append("" + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property.name)) + "=?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where ");
            for (Property property : entity.properties) {
                if (property.unique) {
                    builder.append("" + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property.name)) + "=? and ");
                }
            }
            builder.delete(builder.length() - 5, builder.length());
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 返回根据id更新的语句
     */
    public String updateById(Class _class) {
        String key = "updateById_" + _class.getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = ReflectionUtil.entityMap.get(_class.getName());
            builder.append("update " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + " set ");

            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                builder.append("" + syntaxHandler.getSyntax(Syntax.Escape, StringUtil.Camel2Underline(property.name)) + "=?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where " + syntaxHandler.getSyntax(Syntax.Escape, entity.id.name) + " = ?");
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 删除表
     */
    public String dropTable(Class _class) {
        String key = "dropTable_" + _class.getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("drop table");
            builder.append(" " + syntaxHandler.getSyntax(Syntax.Escape, ReflectionUtil.entityMap.get(_class.getName()).tableName));
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 返回列名的SQL语句
     */
    public String columns(String className, String tableAlias) {
        String key = "columnTable_" + className + "_" + tableAlias;
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Property[] properties = ReflectionUtil.entityMap.get(className).properties;
            for (Property property : properties) {
                builder.append(tableAlias + "." + syntaxHandler.getSyntax(Syntax.Escape, property.column) + " as " + tableAlias + "_" + property.column + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 返回不包含指定列名的SQL语句
     */
    public String columnsNot(String field, String className, String tableAlias) {
        StringBuilder builder = new StringBuilder();
        Entity entity = ReflectionUtil.entityMap.get(className);
        List<String> notColumns = entity.notColumns;
        notColumns.add(field);
        Property[] properties = entity.properties;
        outside:
        for (Property property : properties) {
            if (notColumns.size() > 0) {
                //排除字段
                for (String notColumn : notColumns) {
                    if (property.column.equals(notColumn)) {
                        continue outside;
                    }
                }
            }
            builder.append(tableAlias + "." + syntaxHandler.getSyntax(Syntax.Escape, property.column) + " as " + tableAlias + "_" + property.column + ",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * 清空表
     *
     * @param aClass 被清空表对应的实体类
     * @return 生成清空表的SQL语句
     */
    public String clear(Class aClass) {
        //先判断是否存在需要清除的表的SQL
        String key = "cleanTable_" + aClass.getName();
        if (!sqlCache.containsKey(key)) {
            //通过反射工具类，拿到保存的实体类信息，进而拿到数据库中的表名
            String tableName = ReflectionUtil.entityMap.get(aClass.getName()).tableName;
            String cleanSql = "truncate " + syntaxHandler.getSyntax(Syntax.Escape, tableName);
            sqlCache.put(key, cleanSql);
        }
        return sqlCache.get(key);
    }
}
