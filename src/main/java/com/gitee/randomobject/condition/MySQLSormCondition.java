package com.gitee.randomobject.condition;


import com.gitee.randomobject.dao.AbstractSormDao;
import com.gitee.randomobject.helper.SQLHelper;
import com.gitee.randomobject.syntax.SyntaxHandler;

import javax.sql.DataSource;

public class MySQLSormCondition extends AbstractSormCondition {

    public MySQLSormCondition(Class _class,
                              DataSource dataSource,
                              AbstractSormDao abstractDAO,
                              SyntaxHandler syntaxHandler,
                              SQLHelper sqlHelper) {
        super(_class, dataSource, abstractDAO, syntaxHandler, sqlHelper);
    }


}
