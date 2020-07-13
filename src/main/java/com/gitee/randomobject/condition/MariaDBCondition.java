package com.gitee.randomobject.condition;


import com.gitee.randomobject.dao.AbstractDAO;
import com.gitee.randomobject.helper.SQLHelper;
import com.gitee.randomobject.syntax.SyntaxHandler;

import javax.sql.DataSource;

public class MariaDBCondition extends AbstractCondition {

    public MariaDBCondition(Class _class,
                            DataSource dataSource,
                            AbstractDAO abstractDAO,
                            SyntaxHandler syntaxHandler,
                            SQLHelper sqlHelper) {
        super(_class, dataSource, abstractDAO, syntaxHandler, sqlHelper);
    }


}