package com.gitee.randomobject.util;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SormConfig {

    /**
     * 数据源
     */
    public static DataSource dataSource;
    /**
     * 待扫描包名
     */
    public static Map<String, String> packageNameMap = new HashMap<>();
    /**
     * 要忽略的类
     */
    public static List<Class> ignoreClassList;
    /**
     * 是否开启外键约束
     */
    public static boolean openForeignKey;
    /**
     * 是否启动时自动建表
     */
    public static boolean autoCreateTable = true;
}
