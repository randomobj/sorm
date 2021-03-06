package com.gitee.randomobject.dao;

import com.gitee.randomobject.condition.SormCondition;
import com.gitee.randomobject.condition.MariaDBSormCondition;
import com.gitee.randomobject.helper.SQLHelper;
import com.gitee.randomobject.syntax.MariaDBSyntaxHandler;

import javax.sql.DataSource;

public class MariaDBDAO extends MySQLDAO{

    public MariaDBDAO(DataSource dataSource) {
        super(dataSource);
        fieldMapping.put("char", "char(4)");
        fieldMapping.put("integer", "integer(11)");
        fieldMapping.put("long", "INTEGER");
        fieldMapping.put("float", "float(4,2)");
        fieldMapping.put("double", "double(5,2)");
        syntaxHandler = new MariaDBSyntaxHandler();
        sqlHelper = new SQLHelper(syntaxHandler);
    }

    @Override
    public <T> SormCondition<T> query(Class<T> _class) {
        return new MariaDBSormCondition(_class, dataSource, this, syntaxHandler,sqlHelper);
    }
}
