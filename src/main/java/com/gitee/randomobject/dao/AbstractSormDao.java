package com.gitee.randomobject.dao;


import com.alibaba.fastjson.JSON;
import com.gitee.randomobject.condition.SormCondition;
import com.gitee.randomobject.domain.Entity;
import com.gitee.randomobject.domain.Property;
import com.gitee.randomobject.helper.SQLHelper;
import com.gitee.randomobject.syntax.Syntax;
import com.gitee.randomobject.syntax.SyntaxHandler;
import com.gitee.randomobject.util.ReflectionUtil;
import com.gitee.randomobject.util.SormConfig;
import com.gitee.randomobject.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractSormDao implements SormDao {

    Logger logger = LoggerFactory.getLogger(AbstractSormDao.class);

    /**
     * 字段映射
     */
    protected Map<String, String> fieldMapping = new HashMap<>();
    protected DataSource dataSource;
    protected Connection connection;

    /**
     * 差异语法
     */
    protected SyntaxHandler syntaxHandler;
    /**
     * SQL语句
     */
    protected SQLHelper sqlHelper;

    /**
     * 是否开启事务
     */
    public boolean startTransactional = false;

    public AbstractSormDao(DataSource dataSource) {
        this.dataSource = dataSource;
        fieldMapping.put("string", "varchar(255)");
        fieldMapping.put("boolean", "boolean");
        fieldMapping.put("byte", "tinyint");
        fieldMapping.put("char", "char");
        fieldMapping.put("short", "smallint");
        fieldMapping.put("int", "integer");
        fieldMapping.put("integer", "integer");
        fieldMapping.put("long", "bigint");
        fieldMapping.put("float", "float");
        fieldMapping.put("double", "double");
        fieldMapping.put("date", "datetime");
        fieldMapping.put("time", "time");
        fieldMapping.put("timestamp", "timestamp");
    }

    @Override
    public boolean exist(Object instance) {
        try {
            //有id则一定存在数据库记录
            if (ReflectionUtil.hasId(instance)) {
                return true;
            }
            //有唯一性约束则根据唯一性约束查询
            SormCondition sormCondition = getUniqueCondition(instance);
            if (sormCondition == null) {
                return false;
            }
            return sormCondition.count() > 0;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> T fetch(Class<T> _class, long id) {
        String property = ReflectionUtil.entityMap.get(_class.getName()).id.name;
        List<T> ts = fetchList(_class, property, id);
        if (ValidateUtil.isNotEmpty(ts)) {
            return ts.get(0);
        } else {
            logger.debug("[不存在此id的数据记录]id:{}", id);
            return null;
        }
    }

    @Override
    public <T> List<T> fetchList(Class<T> _class, String property, Object value) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = null;
            int count = -1;
            if (value == null) {
                String fetchNullSQL = sqlHelper.fetchNull(_class, property);
                logger.debug("[根据属性:{},value:{},获取对象]执行sql:{}", property, value, fetchNullSQL);
                ps = connection.prepareStatement(fetchNullSQL);
                count = (int) query(_class).addNullQuery(property).count();
            } else {
                String fetchSQL = sqlHelper.fetch(_class, property);
                logger.debug("[根据属性:{},value:{},获取对象]执行sql:{}", property, value, fetchSQL.replace("?", value.toString()));
                ps = connection.prepareStatement(fetchSQL);
                switch (value.getClass().getSimpleName().toLowerCase()) {
                    case "int": {
                        ps.setInt(1, (int) value);
                    }
                    break;
                    case "integer": {
                        ps.setObject(1, (Integer) value);
                    }
                    break;
                    case "long": {
                        if (value.getClass().isPrimitive()) {
                            ps.setLong(1, (long) value);
                        } else {
                            ps.setObject(1, value);
                        }
                    }
                    break;
                    case "boolean": {
                        if (value.getClass().isPrimitive()) {
                            ps.setBoolean(1, (boolean) value);
                        } else {
                            ps.setObject(1, value);
                        }
                    }
                    break;
                    case "string": {
                        ps.setString(1, value.toString());
                    }
                    break;
                    default: {
                        ps.setObject(1, value);
                    }
                }
                count = (int) query(_class).addQuery(property, value).count();
            }
            ResultSet resultSet = ps.executeQuery();
            List<T> instanceList = ReflectionUtil.mappingResultSetToJSONArray(resultSet, count).toJavaList(_class);
            ps.close();
            connection.close();
            return instanceList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public abstract <T> SormCondition<T> query(Class<T> _class);

    @Override
    public long save(Object instance) {
        if (instance == null) {
            return 0;
        }
        try {
            Connection connection = getConnection();
            Class _class = instance.getClass();
            PreparedStatement ps = null;
            long effect = 0;

            Entity entity = ReflectionUtil.entityMap.get(_class.getName());//拿到实体类信息
            SormCondition sormCondition = getUniqueCondition(instance);//是否存在unique

            if (ReflectionUtil.hasId(instance)) { //存在id
                //设置id值
                entity.id.field.setLong(instance, entity.id.field.getLong(instance));
                //根据id更新
                String updateById = sqlHelper.updateById(_class);
                ps = connection.prepareStatement(updateById);
                logger.debug("[根据id更新]执行SQL:{}", ReflectionUtil.setValueWithUpdateById(ps, instance, updateById));
                effect = ps.executeUpdate();

            } else if (sormCondition != null) {//仅存在unique

                if (null != sormCondition) {//根据唯一性约束字段得到数据库中此实例的id值

                    List<Long> ids = sormCondition.getValueList(Long.class, entity.id.name);
                    if (ids.size() > 0) {//存在unique的字段的数据，就是更新
                        String updateByUniqueKey = sqlHelper.updateByUniqueKey(_class);
                        ps = connection.prepareStatement(updateByUniqueKey);
                        logger.debug("[根据unique更新]执行SQL:{}", ReflectionUtil.setValueWithUpdateByUniqueKey(ps, instance, updateByUniqueKey));
                        effect = ps.executeUpdate();

                    } else {//插入
                        effect = insert(connection, _class, instance, entity);
                    }
                }
            } else {//普通插入操作

                effect = insert(connection, _class, instance, entity);
            }
            if (ps != null) {
                ps.close();
            }
            if (!startTransactional) {
                connection.close();
            }
            return effect;
        } catch (Exception e) {
            e.printStackTrace();
            //出现异常，事务就回滚，前提是需要开启事务
            //connection.rollback();
            return -1;
        }
    }


    private long insert(Connection connection, Class _class, Object instance, Entity entity) {
        PreparedStatement ps = null;
        long effect = 0;
        try {
            String insertIgnore = sqlHelper.insertIgnore(_class, syntaxHandler.getSyntax(Syntax.InsertIgnore));
            ps = connection.prepareStatement(insertIgnore, PreparedStatement.RETURN_GENERATED_KEYS);
            logger.debug("[执行插入操作]执行SQL:{}", ReflectionUtil.setValueWithInsertIgnore(ps, instance, insertIgnore));
            effect = ps.executeUpdate();
            if (effect > 0) {
                //获取主键
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    //默认获取的主键类型是long
                    long id = rs.getLong(1);
                    entity.id.field.setLong(instance, id);
                }
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return effect;
    }


    @Override
    public long save(List instanceList) throws SQLException {
        if (instanceList == null || instanceList.size() == 0) {
            return 0;
        }
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            String updateById = sqlHelper.updateById(instanceList.get(0).getClass());
            PreparedStatement updateByIdPs = connection.prepareStatement(updateById);
            String insertIgnore = sqlHelper.insertIgnore(instanceList.get(0).getClass(), syntaxHandler.getSyntax(Syntax.InsertIgnore));
            PreparedStatement insertIgnorePs = connection.prepareStatement(insertIgnore);
            //根据每个实体类具体情况插入
            for (Object instance : instanceList) {
                try {
                    if (exist(instance)) {
                        //根据id更新
                        logger.debug("[根据id更新]执行SQL:{}", ReflectionUtil.setValueWithUpdateById(updateByIdPs, instance, updateById));
                        updateByIdPs.addBatch();
                    } else {
                        //执行insertIgnore
                        logger.debug("[执行插入操作]执行SQL:{}", ReflectionUtil.setValueWithInsertIgnore(insertIgnorePs, instance, insertIgnore));
                        insertIgnorePs.addBatch();
                    }
                } catch (Exception e) {
                    logger.warn("[插入单个记录失败]{}", JSON.toJSONString(instance));
                    e.printStackTrace();
                }
            }
            //执行Batch并将所有结果添加
            long effect = 0;
            PreparedStatement[] preparedStatements = {updateByIdPs, insertIgnorePs};
            for (int i = 0; i < preparedStatements.length; i++) {
                if (preparedStatements[i] == null) {
                    continue;
                }
                //执行
                int[] results = preparedStatements[i].executeBatch();

                for (long result : results) {
                    effect += result;
                }
                preparedStatements[i].close();
            }
            //未开启事务，提交
            if (!startTransactional) {
                connection.commit();
                connection.close();
            }
            return effect;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public long save(Object[] instances) throws SQLException {
        return save(Arrays.asList(instances));
    }

    @Override
    public long delete(Class _class, long id) {
        String field = ReflectionUtil.entityMap.get(_class.getName()).id.name;
        return delete(_class, field, id);
    }

    @Override
    public long delete(Class _class, String field, Object value) {
        try {
            Connection connection = getConnection();
            String deleteSQL = sqlHelper.delete(_class, field);
            PreparedStatement ps = connection.prepareStatement(deleteSQL);
            ps.setObject(1, value);
            logger.debug("[根据属性{}=>{}删除]执行SQL:{}", field, value, deleteSQL.replace("?", value.toString()));
            long effect = ps.executeUpdate();
            ps.close();
            if (!startTransactional) {
                connection.close();
            }
            return effect;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public long clear(Class _class) {
        try {
            Connection connection = getConnection();
            String deleteSQL = sqlHelper.clear(_class);
            logger.debug("[清空{}表]执行SQL:{}", _class.getSimpleName(), deleteSQL);
            PreparedStatement ps = connection.prepareStatement(deleteSQL);
            long effect = ps.executeUpdate();
            ps.close();
            if (!startTransactional) {
                connection.close();
            }
            return effect;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 开启事务
     */
    @Override
    public void startTransaction() {
        startTransactional = true;
        try {
            connection = getConnection();
        } catch (SQLException e) {
            //获取数据库连接失败时，需要关闭当前对象的数据库连接，必须保证当前事务在当前对象的数据库连接中
            e.printStackTrace();
            try {
                connection.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 事务回滚
     */
    @Override
    public void rollback() {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 事务提交
     */
    @Override
    public void commit() {
        if (connection == null) {
            return;
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束事务
     */
    @Override
    public void endTransaction() {
        startTransactional = false;
        if (connection == null) {
            logger.warn("数据库事务连接为空!不做任何操作!");
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }

    /**
     * 建表
     */
    @Override
    public void create(Class _class) {
        Entity entity = ReflectionUtil.entityMap.get(_class.getName());
        startTransaction();
        try {
            updateColumnType();
            createTable(entity);
            createIndex(entity);
            createUniqueKey(entity);
            if (SormConfig.openForeignKey) {
                createForeignKey();
            }
            commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        endTransaction();
    }

    /**
     * 删表
     */
    @Override
    public void drop(Class _class) {
        String sql = sqlHelper.dropTable(_class);
        logger.debug("[删除表]执行SQL:{}", sql);
        Connection connection = null;
        try {
            connection = getConnection();
            connection.prepareStatement(sql).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删表
     */
    @Override
    public void rebuild(Class _class) {
        drop(_class);
        create(_class);
    }

    /**
     * 仅供Condition类调用
     */
    public Connection getConnection() throws SQLException {
        //开启事务时使用同一Connection,不开启事务时从连接池中获取
        if (startTransactional) {
            synchronized (this) {
                if (connection == null) {
                    connection = dataSource.getConnection();
                    connection.setAutoCommit(false);
                }
            }
            return connection;
        } else {
            return dataSource.getConnection();
        }
    }

    /**
     * 获取实例的唯一性约束查询条件
     */
    private <T> SormCondition<T> getUniqueCondition(Object instance) throws IllegalAccessException {
        Property[] properties = ReflectionUtil.entityMap.get(instance.getClass().getName()).uniqueKeyProperties;
        if (properties == null || properties.length == 0) {
            return null;
        }
        SormCondition sormCondition = query(instance.getClass());
        for (Property property : properties) {
            sormCondition.addQuery(property.name, property.field.get(instance));
        }
        return sormCondition;
    }

    /**
     * 获取数据库信息
     */
    protected abstract Entity[] getDatabaseInfo() throws SQLException;

    /**
     * 创建新表
     */
    protected abstract void createTable(Entity entity) throws SQLException;

    /**
     * 创建索引
     */
    protected void createIndex(Entity entity) throws SQLException {
        if (null == entity.indexProperties || entity.indexProperties.length == 0) {
            return;
        }
        String indexName = entity.tableName + "_index";
        if (isIndexExists(entity.tableName, indexName)) {
            return;
        }
        StringBuilder indexBuilder = new StringBuilder("create index " + syntaxHandler.getSyntax(Syntax.Escape, indexName) + " on " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + " (");
        for (Property property : entity.indexProperties) {
            indexBuilder.append(syntaxHandler.getSyntax(Syntax.Escape, property.column) + ",");
        }
        indexBuilder.deleteCharAt(indexBuilder.length() - 1);
        indexBuilder.append(");");
        String indexSQL = indexBuilder.toString().replaceAll("\\s+", " ");
        logger.debug("[添加索引]表:{},执行SQL:{}", entity.tableName, indexSQL);
        connection.prepareStatement(indexSQL).executeUpdate();
    }

    /**
     * 创建唯一索引
     */
    protected void createUniqueKey(Entity entity) throws SQLException {
        if (null == entity.uniqueKeyProperties || entity.uniqueKeyProperties.length == 0) {
            return;
        }
        String uniqueKeyIndexName = entity.tableName + "_unique_index";
        if (isIndexExists(entity.tableName, uniqueKeyIndexName)) {
            return;
        }
        StringBuilder uniqueKeyBuilder = new StringBuilder("create unique index " + syntaxHandler.getSyntax(Syntax.Escape, uniqueKeyIndexName) + " on " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + " (");
        for (Property property : entity.uniqueKeyProperties) {
            uniqueKeyBuilder.append(syntaxHandler.getSyntax(Syntax.Escape, property.column) + ",");
        }
        uniqueKeyBuilder.deleteCharAt(uniqueKeyBuilder.length() - 1);
        uniqueKeyBuilder.append(");");
        String uniqueKeySQL = uniqueKeyBuilder.toString().replaceAll("\\s+", " ");
        logger.debug("[添加唯一性约束]表:{},执行SQL:{}", entity.tableName, uniqueKeySQL);
        connection.prepareStatement(uniqueKeySQL).executeUpdate();
    }

    /**
     * 创建外键约束
     */
    protected abstract void createForeignKey() throws SQLException;

    /**
     * 索引是否存在
     */
    protected abstract boolean isIndexExists(String tableName, String indexName) throws SQLException;

    /**
     * 外键是否存在
     */
    protected abstract boolean isConstraintExists(String tableName, String constraintName) throws SQLException;

    /**
     * 删除索引
     */
    protected abstract void dropIndex(String tableName, String indexName) throws SQLException;

    /**
     * 自动创建表
     */
    public void autoBuildDatabase() throws SQLException {
        if (!SormConfig.autoCreateTable) {
            return;
        }
        updateColumnType();
        //对比实体类信息与数据库信息
        Entity[] dbEntityList = getDatabaseInfo();
        logger.debug("[获取数据库信息]数据库表个数:{}", dbEntityList.length);

        startTransaction();
        Collection<Entity> entityList = ReflectionUtil.entityMap.values();
        for (Entity entity : entityList) {
            boolean entityExist = false;
            for (Entity dbEntity : dbEntityList) {
                if (entity.tableName.equals(dbEntity.tableName)) {
                    //对比字段
                    compareEntityDatabase(entity, dbEntity);
                    entityExist = true;
                    break;
                }
            }
            if (!entityExist) {
                //新增数据库表
                createTable(entity);
                createIndex(entity);
                createUniqueKey(entity);
            }
        }
        if (SormConfig.openForeignKey) {
            createForeignKey();
        }
        commit();
        endTransaction();
    }

    /**
     * 对比实体类和数据表并创建新列
     */
    private void compareEntityDatabase(Entity entity, Entity dbEntity) throws SQLException {
        Property[] entityProperties = entity.properties;
        Property[] dbEntityProperties = dbEntity.properties;
        boolean hasIndexProperty = false;
        boolean hasUniqueProperty = false;
        boolean hasForeignKeyProperty = false;
        for (Property entityProperty : entityProperties) {
            boolean columnExist = false;
            for (Property dbEntityProperty : dbEntityProperties) {
                if (dbEntityProperty.column.equals(entityProperty.column)) {
                    columnExist = true;
                    break;
                }
            }
            if (!columnExist) {
                addProperty(entity, entityProperty);
                if (entityProperty.index) {
                    hasIndexProperty = true;
                }
                if (entityProperty.unique) {
                    hasUniqueProperty = true;
                }
                if (null != entityProperty.foreignKey) {
                    hasForeignKeyProperty = true;
                }
            }
        }
        //如果新增属性中有索引属性,则重新建立联合索引
        if (hasIndexProperty) {
            dropIndex(entity.tableName, entity.tableName + "_index");
            createIndex(entity);
        }
        //如果新增属性中有唯一约束,则重新建立联合唯一约束
        if (hasUniqueProperty) {
            dropIndex(entity.tableName, entity.tableName + "_unique_index");
            createUniqueKey(entity);
        }
        if (hasForeignKeyProperty) {
            createForeignKey();
        }
    }

    /**
     * 表添加属性
     */
    private void addProperty(Entity entity, Property property) throws SQLException {
        StringBuilder addColumnBuilder = new StringBuilder();
        addColumnBuilder.append("alter table " + syntaxHandler.getSyntax(Syntax.Escape, entity.tableName) + " add " + syntaxHandler.getSyntax(Syntax.Escape, property.column) + " " + property.columnType + " ");
        if (null != property.defaultValue) {
            addColumnBuilder.append(" default " + property.defaultValue);
        }
        if (null != property.comment) {
            addColumnBuilder.append(" " + syntaxHandler.getSyntax(Syntax.Comment, property.comment));
        }
        addColumnBuilder.append(";");
        String sql = addColumnBuilder.toString().replaceAll("\\s+", " ");
        logger.debug("[添加新列]表:{},列名:{},执行SQL:{}", entity.tableName, property.column + "(" + property.columnType + ")", sql);
        connection.prepareStatement(sql).executeUpdate();
    }

    /**
     * 更新属性列类型
     */
    private void updateColumnType() {
        Collection<Entity> entityList = ReflectionUtil.entityMap.values();
        for (Entity entity : entityList) {
            Property[] properties = entity.properties;
            for (Property property : properties) {
                property.columnType = property.customType;
                if (property.columnType == null) {
                    property.columnType = fieldMapping.get(property.type);
                }
            }
        }
    }
}
