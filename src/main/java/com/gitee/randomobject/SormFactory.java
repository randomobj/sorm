package com.gitee.randomobject;

import com.gitee.randomobject.dao.AbstractDAO;
import com.gitee.randomobject.dao.DAO;
import com.gitee.randomobject.dao.MariaDBDAO;
import com.gitee.randomobject.dao.MySQLDAO;
import com.gitee.randomobject.util.ReflectionUtil;
import com.gitee.randomobject.util.SormConfig;
import com.gitee.randomobject.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SormFactory {
    Logger logger = LoggerFactory.getLogger(SormFactory.class);
    private final static HashMap<String, Class> driverMapping = new HashMap();

    //目前只支持mysql和mariadb
    static {
        driverMapping.put("jdbc:mysql", MySQLDAO.class);
        driverMapping.put("jdbc:mariadb", MariaDBDAO.class);
    }

    private SormFactory() {
    }

    private enum SormFactoryBuilder {
        INSTANCE;
        private SormFactory sormFactory;

        SormFactoryBuilder() {
            this.sormFactory = new SormFactory();
        }

        public SormFactory getSormFactory(){
            return sormFactory;
        }
    }

    public static SormFactory newInstance() {
        return  SormFactoryBuilder.INSTANCE.getSormFactory();
    }

    public SormFactory dataSource(DataSource dataSource) {
        SormConfig.dataSource = dataSource;
        return this;
    }

    public SormFactory packageName(String packageName) {
        SormConfig.packageNameMap.put(packageName, "");
        return this;
    }

    public SormFactory packageName(String packageName, String prefix) {
        SormConfig.packageNameMap.put(packageName, prefix + "_");
        return this;
    }


    public SormFactory ignoreClass(Class _class) {
        if (SormConfig.ignoreClassList == null) {
            SormConfig.ignoreClassList = new ArrayList<>();
        }
        SormConfig.ignoreClassList.add(_class);
        return this;
    }

    public SormFactory foreignKey(boolean openForeignKey) {
        SormConfig.openForeignKey = openForeignKey;
        return this;
    }

    public SormFactory autoCreateTable(boolean autoCreateTable) {
        SormConfig.autoCreateTable = autoCreateTable;
        return this;
    }

    public DAO build() {
        if (SormConfig.packageNameMap.isEmpty()) {
            throw new IllegalArgumentException("请设置要扫描的实体类包名!");
        }
        if (ValidateUtil.isNull(SormConfig.dataSource)) {
            throw new IllegalArgumentException("请设置数据库连接池属性!");
        }
        AbstractDAO dao = null;
        try {
            Connection connection = SormConfig.dataSource.getConnection();
            String url = connection.getMetaData().getURL();
            logger.info("[加载的数据源地址]{}", url);
            Set<String> keySet = driverMapping.keySet();
            for (String key : keySet) {
                if (url.contains(key)) {
                    //反射调用构造方法创建DAO的实例对象，此处利用java的多态，运行时的动态绑定。
                    dao = (AbstractDAO) driverMapping.get(key).getConstructor(DataSource.class).newInstance(SormConfig.dataSource);
                    break;
                }
            }
            if (dao == null) {
                throw new UnsupportedOperationException("当前数据源没有合适的适配器.数据源地址:" + url);
            }
            ReflectionUtil.getEntityInfo();
            //自动建表
            dao.autoBuildDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return dao;
    }

}
