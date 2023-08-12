package com.briar.util;


import com.briar.Agent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.jar.JarEntry;

public class DataSourceUtil {
    private static DataSource dataSource;
    private static URLClassLoader urlClassLoader;

    private DataSourceUtil(){};

    static {
        try {
            //拿到jarPath
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(Agent.class.getName());
            Field jarPath1 = aClass.getDeclaredField("jarPath");
            jarPath1.setAccessible(true);
            String jarPath = (String) jarPath1.get(null);
            if (jarPath==null||"".equals(jarPath)){
                dataSource=null;
            }
            //加载配置文件
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath);
            JarEntry jarEntry = jarFile.getJarEntry("druid.properties");
            InputStream inputStream =jarFile.getInputStream(jarEntry);
            Properties properties = new Properties();
            properties.load(inputStream);

            String mysqlJarPath=getMySQLJarFromLocalRepository();
            String druidJarPath=getDruidLJarFromLocalRepository();
            if (mysqlJarPath==null){
                mysqlJarPath="https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar";
            }else {
                mysqlJarPath="file:"+mysqlJarPath;
            }
            if (druidJarPath==null){
                druidJarPath="https://repo1.maven.org/maven2/com/alibaba/druid/1.2.16/druid-1.2.16.jar";
            }else {
                druidJarPath="file:"+druidJarPath;
            }

            URL mysqlJarURL = new URL(mysqlJarPath);
            URL druidJarURL = new URL(druidJarPath);
            urlClassLoader = new URLClassLoader(new URL[] { mysqlJarURL,druidJarURL }, DataSourceUtil.class.getClassLoader());
            Class<?> druidDataSourceClass = urlClassLoader.loadClass("com.alibaba.druid.pool.DruidDataSource");
            Object druidDataSource = druidDataSourceClass.newInstance();
            //DruidDataSource.setDriverClassName()
            Method setDriverClassName = druidDataSourceClass.getMethod("setDriverClassName", new Class[]{String.class});
            setDriverClassName.setAccessible(true);
            setDriverClassName.invoke(druidDataSource,properties.getProperty("driverClassName"));

            //DruidDataSource.setUrl()
            Method setUrl = druidDataSourceClass.getMethod("setUrl", new Class[]{String.class});
            setUrl.setAccessible(true);
            setUrl.invoke(druidDataSource,properties.getProperty("url"));

            //DruidDataSource.setUsername()
            Method setUsername = druidDataSourceClass.getMethod("setUsername", new Class[]{String.class});
            setUsername.setAccessible(true);
            setUsername.invoke(druidDataSource,properties.getProperty("username"));

            //DruidDataSource.setPassword()
            Method setPassword = druidDataSourceClass.getMethod("setPassword", new Class[]{String.class});
            setPassword.setAccessible(true);
            setPassword.invoke(druidDataSource,properties.getProperty("password"));

            //DruidDataSource.setInitialSize()
            Method setInitialSize = druidDataSourceClass.getMethod("setInitialSize", new Class[]{int.class});
            setInitialSize.setAccessible(true);
            setInitialSize.invoke(druidDataSource,Integer.parseInt(properties.getProperty("initialSize")));


            //DruidDataSource.setMaxActive()
            Method setMaxActive = druidDataSourceClass.getMethod("setMaxActive", new Class[]{int.class});
            setMaxActive.setAccessible(true);
            setMaxActive.invoke(druidDataSource,Integer.parseInt(properties.getProperty("maxActive")));

            //DruidDataSource.setMaxWait()
            Method setMaxWait = druidDataSourceClass.getMethod("setMaxWait", new Class[]{long.class});
            setMaxWait.setAccessible(true);
            setMaxWait.invoke(druidDataSource,Long.parseLong(properties.getProperty("maxWait")));

            dataSource=(DataSource)druidDataSource;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static DataSource getDataSource(){
        return dataSource;
    }

    public static boolean isSQLInject(String sql){
        try {
            Class<?> wallUtilsClass = urlClassLoader.loadClass("com.alibaba.druid.wall.WallUtils");
            Method isValidateMySql = wallUtilsClass.getDeclaredMethod("isValidateMySql", new Class[]{String.class});
            isValidateMySql.setAccessible(true);
            return !(boolean) isValidateMySql.invoke(null, sql);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //获取本地maven库中的mysql-connector-java jar包
    private static String getMySQLJarFromLocalRepository() {
        String localRepositoryPath=getMavenRepositoryPath();
        String mysqlJarPath=null;
        if (localRepositoryPath==null){
            return null;
        }
        String mysqlOutterPath=localRepositoryPath+File.separator+"mysql"+File.separator+"mysql-connector-java";
        File mysqlOutterDir = new File(mysqlOutterPath);
        if (!mysqlOutterDir.exists()){
            return null;
        }
        String[] list = mysqlOutterDir.list();
        if (list==null||list.length<=0){
            return null;
        }
        for (String s : list) {
            if (s.contains("5")){
                mysqlJarPath=mysqlOutterPath+File.separator+s+File.separator+"mysql-connector-java-"+s+".jar";
                File mysqlJarFile = new File(mysqlJarPath);
                if (mysqlJarFile.exists()){
                    return mysqlJarPath;
                }
            }
        }
        return null;
    }

    //获取本地maven库中的druid jar包
    private static String getDruidLJarFromLocalRepository() {
        String localRepositoryPath=getMavenRepositoryPath();
        String druidJarPath=null;
        if (localRepositoryPath==null){
            return null;
        }
        String druidOutterPath=localRepositoryPath+File.separator+"com"+File.separator+"alibaba"+File.separator+"druid";
        File druidOutterDir = new File(druidOutterPath);
        if (!druidOutterDir.exists()){
            return null;
        }
        String[] list = druidOutterDir.list();
        if (list==null||list.length<=0){
            return null;
        }
        for (String s : list) {
            if (s.contains("1.2")){
                druidJarPath=druidOutterPath+File.separator+s+File.separator+"druid-"+s+".jar";
                File mysqlJarFile = new File(druidJarPath);
                if (mysqlJarFile.exists()){
                    return druidJarPath;
                }
            }
        }
        return null;
    }
    //获取本地maven库的路径
    private static String getMavenRepositoryPath(){
        String mavenBinPath=null;
        for (String s : System.getProperty("java.library.path").split(";")) {
            if (s!=null&&(s.contains("maven")||s.contains("mvn"))){
                mavenBinPath=s;
                break;
            }
        }
        if (mavenBinPath!=null){
            String mavenConfPath=mavenBinPath+ File.separator+".."+ File.separator+"conf"+File.separator+"settings.xml";
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(mavenConfPath));
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.parse(fileInputStream);
                NodeList localRepository = doc.getElementsByTagName("localRepository");
                Node item = localRepository.item(0);
                String localRepositoryPath = item.getTextContent();
                File localRepositoryDir = new File(localRepositoryPath);
                if (localRepositoryDir.exists()){
                    return localRepositoryPath;
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
