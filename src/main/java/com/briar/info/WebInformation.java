package com.briar.info;

import com.briar.constant.OperatorSystem;
import com.briar.hook.CommonHook;
import com.briar.util.DataSourceUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * （使用单例模式）
 * 目标web应用相关信息：
 * - 系统信息（Linux、Windows、Mac）
 * - 架构（x86、ARM）
 * - jdk版本（主版本、次版本）
 * - 服务器信息（Tomcat、JBoss、Jetty、Wildfly、Resin、WebLogic、WebSphere）、版本
 * - 框架（SpringBoot or Struts）
 * - 数据库信息（MySQL、Oracle、MSSQL、Redis、SQLite3、PostgreSQL、DB2、HSQLDB (WebGoat 使用的嵌入数据库)，可能连接多个数据库）
 */
public class WebInformation {

    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(WebInformation.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //主版本
    public String majorJDKVersion;

    //次版本
    public String minorJDKVersion;

    //架构信息
    public String arch;

    //系统信息
    public String os;

    //服务器信息
    public String webServer;

    //框架
    public String framework;

    //数据库信息
    public List<String> sqlInformation;

    //全局Hook信息
    public Set<CommonHook> hookContext;

    //每个线程独有
    public ThreadLocal<Context> context;

    private WebInformation(){
        context=new ThreadLocal<Context>();
        hookContext  = new CopyOnWriteArraySet<CommonHook>();
        sqlInformation=new CopyOnWriteArrayList<String>();
    };
    private static WebInformation instance=new WebInformation();
    public static WebInformation getInstance(){
        return instance;
    }

    public static void clearContext(){
        System.out.println("清除context");
        WebInformation.getInstance().context.remove();
    }


    /**
     * Agent启动前采集系统相关信息
     * 包括：
     * - 系统信息（Linux、Windows、Mac）
     * - 架构（x86、ARM）
     * - jdk版本（主版本、次版本）
     * - 配置文件
     */
    public void init(){
        if (WebInformation.class.getClassLoader()==null){
            logger.info("开始采集当前jvm信息");
        }
        //JDK的标准版本 ，例如：1.8 , 1.7 , 1.6
        majorJDKVersion = System.getProperty("java.specification.version");

        //JDK的详细版本号，例如：1.8.0_91 ， 1.7.0_79，1.6.0
        minorJDKVersion = System.getProperty("java.version");
        String[] s = minorJDKVersion.split("_");
        if (s.length==2){
            minorJDKVersion = s[1];
        }else {
            minorJDKVersion = "0";
        }
        arch = System.getProperty("os.arch");

        os = System.getProperty("os.name");
        if (os.contains("Linux")){
            os = OperatorSystem.LINUX;
        } else if (os.contains("Mac")) {
            os = OperatorSystem.MAC;
        } else if (os.contains("Windows")) {
            os = OperatorSystem.WINDOWS;
        }
        if (WebInformation.class.getClassLoader()==null){
            logger.info("采集完毕！当前系统为："+ os +" 当前jdk版本为："+majorJDKVersion);
        }
        //加载配置文件
        ConfigScanner.scan();
    }


    public static void saveAttackInfo(AttackInfo attackInfo){
        if (attackInfo==null){
            return;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = (Connection) DataSourceUtil.getDataSource().getConnection();

            String sql = "INSERT INTO attackinfo(attack_type, context_id, is_block, severity, attack_time, payload, stack) VALUES (?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, attackInfo.getAttackType());

            // 插入 Context 对象并获取生成的 ID
            int contextId = saveContext(connection, attackInfo.getContext());
            statement.setInt(2, contextId);

            statement.setBoolean(3, attackInfo.isBlock());
            statement.setString(4, attackInfo.getSeverity());
            statement.setLong(5, attackInfo.getAttackTime());
            statement.setString(6, attackInfo.getPayload());
            statement.setString(7, String.join(",", attackInfo.getStack()));

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                attackInfo.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
            closeStatement(statement);
        }
    }

    private static int saveContext(Connection connection, Context context) {
        PreparedStatement statement = null;

        Gson gson = new Gson();

        try {
            String sql = "INSERT INTO context(method, protocol, source_ip, url, uri, hostname, " +
                    "port, content_type, length, header, url_args, body,  " +
                    "destination_ip, query_string) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, context.getMethod());
            statement.setString(2, context.getProtocol());
            statement.setString(3, context.getSourceIp());
            statement.setString(4, context.getUrl());
            statement.setString(5, context.getUri());
            statement.setString(6, context.getHostname());
            statement.setString(7, context.getPort());
            statement.setString(8, context.getContentType());
            statement.setInt(9, context.getLength());
            statement.setString(10, gson.toJson(context.getHeader()));
            statement.setString(11, gson.toJson(context.getUrlArgs()));
            statement.setString(12, context.getBody());
            statement.setString(13, context.getDestinationIp());
            statement.setString(14, context.getQueryString());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                context.setId(id);
                return id;
            } else {
                throw new SQLException("Insert context failed, no generated keys obtained.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeStatement(statement);
        }
    }

    private static void closeConnection(Connection connection){
        if (connection!=null){
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void closeStatement(PreparedStatement preparedStatement){
        if (preparedStatement!=null){
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
