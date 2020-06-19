package com.gitee.randomobject.dao;

import javax.sql.DataSource;

public class MariaDBDAO extends MySQLDAO{

    public MariaDBDAO(DataSource dataSource) {
        super(dataSource);
    }


}
