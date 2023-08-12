package com.briar.constant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 关系型数据库信息（MySQL、Oracle、MSSQL、SQLite3、PostgreSQL、DB2、HSQLDB (WebGoat 使用的嵌入数据库)）
 * 非关系型数据库信息（Redis、MongoDB）
 */
public class SQLSever {
    //只读取，不需要线程安全
    public static final Map<String,List<String>> sqlMarkMap=new HashMap<String,List<String>>(){
        {
            put(SQLSever.MYSQL,new ArrayList(Arrays.asList(SQLSever.MYSQL_JDBC_OVER8_CLASS,SQLSever.MYSQL_JDBC_UNDER8_CLASS)));
            put(SQLSever.ORACLE,new ArrayList(Arrays.asList(SQLSever.ORACLE_CLASS)));
            put(SQLSever.MSSQL,new ArrayList(Arrays.asList(SQLSever.MSSQL_CLASS)));
            put(SQLSever.SQLITE3,new ArrayList(Arrays.asList(SQLSever.SQLITE1_CLASS,SQLSever.SQLITE2_CLASS)));
            put(SQLSever.POSTGRESQL,new ArrayList(Arrays.asList(SQLSever.POSTGRESQL1_CLASS,SQLSever.POSTGRESQL2_CLASS,SQLSever.POSTGRESQL3_CLASS)));
            put(SQLSever.DB2,new ArrayList(Arrays.asList(SQLSever.DB2_CLASS)));
            put(SQLSever.HSQLDB,new ArrayList(Arrays.asList(SQLSever.HSQLDB1_CLASS,SQLSever.HSQLDB2_CLASS)));
            put(SQLSever.REDIS,new ArrayList(Arrays.asList(SQLSever.REDIS1_CLASS,SQLSever.REDIS2_CLASS)));
            put(SQLSever.MONGODB,new ArrayList(Arrays.asList(SQLSever.MONGODB_CLASS)));
        }
    };
    public static final String MYSQL_JDBC_OVER8_CLASS = "com.mysql.cj.jdbc.ConnectionImpl";
    public static final String MYSQL_JDBC_UNDER8_CLASS = "com.mysql.jdbc.ConnectionImpl";
    public static final String ORACLE_CLASS="oracle.jdbc.driver.PhysicalConnection";
    public static final String MSSQL_CLASS="com.microsoft.sqlserver.jdbc.SQLServerConnection";
    public static final String SQLITE1_CLASS="org.sqlite.Conn";
    public static final String SQLITE2_CLASS="org.sqlite.jdbc4.JDBC4Connection";
    public static final String POSTGRESQL1_CLASS="org.postgresql.jdbc.PgConnection";
    public static final String POSTGRESQL2_CLASS="org.postgresql.jdbc3.Jdbc3Connection";
    public static final String POSTGRESQL3_CLASS="org.postgresql.jdbc4.Jdbc4Connection";
    public static final String DB2_CLASS="com.ibm.db2.jcc.am.Connection";
    public static final String HSQLDB1_CLASS="org.hsqldb.jdbc.JDBCConnection";
    public static final String HSQLDB2_CLASS="org.hsqldb.jdbc.jdbcConnection";
    public static final String MONGODB_CLASS="com.mongodb.MongoCollectionImpl";
    //使用了jedis作为redis的客户端
    public static final String REDIS1_CLASS="redis.clients.jedis.util.RedisInputStream";
    //使用了lettuce作为redis的客户端
    public static final String REDIS2_CLASS="io.lettuce.core.AbstractRedisClient";

    public static final String MYSQL="MySQL";
    public static final String ORACLE="Oracle";
    public static final String MSSQL="MSSQL";
    public static final String SQLITE3="SQLite3";
    public static final String POSTGRESQL="PostgreSQL";
    public static final String DB2="DB2";
    public static final String HSQLDB="HSQLDB";

    public static final String REDIS="Redis";
    public static final String MONGODB="MongoDB";
}
