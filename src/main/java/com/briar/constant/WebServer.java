package com.briar.constant;

import java.util.*;

/**
 * 服务器相关常量
 * （Tomcat、JBoss、Jetty、Wildfly、Resin、WebLogic、WebSphere）
 * 框架
 * SpringBoot
 */
public class WebServer {
    public static final Map<String, String> WebServerMarkMap=new HashMap<String,String>(){{
        put(WebServer.TOMCAT,WebServer.TOMCAT_CLASS);
        put(WebServer.SPRINGBOOT_TOMCAT,WebServer.SPRINGBOOT_TOMCAT_CLASS);
        put(WebServer.JBOSS_EAP,WebServer.JBOSS_CLASS);
        put(WebServer.JETTY,WebServer.JETTY_CLASS);
        put(WebServer.RESIN,WebServer.RESIN_CLASS);
        put(WebServer.WEBLOGIC,WebServer.WEBLOGIC_CLASS);
        put(WebServer.WEBSPHERE,WebServer.WEBSPHERE_CLASS);
        put(WebServer.SPRINGBOOT_JETTY,WebServer.SPRINGBOOT_JETTY_CLASS);
        put(WebServer.SPRINGBOOT_UNDERTOW,WebServer.SPRINGBOOT_UNDERTOW_CLASS);
    }};

    public static final Map<String,String> frameworkMarkMap=new HashMap<String,String>(){{
       put(WebServer.SPRINGBOOT,WebServer.SPRINGBOOT_CLASS);
       put(WebServer.STRUTS2,WebServer.STRUTS2_CLASS);
    }};

    public static final String JBOSS_CLASS = "org.jboss.modules.Main";
    public static final String JETTY_CLASS = "org.eclipse.jetty.server.Server";
    public static final String RESIN_CLASS = "com.caucho.server.resin.Resin";
    public static final String TOMCAT_CLASS = "org.apache.catalina.startup.Bootstrap";
    public static final String WEBLOGIC_CLASS = "weblogic.servlet.internal.WebAppServletContext";
    public static final String WEBSPHERE_CLASS = "org.eclipse.core.launcher.Main";
    public static final String SPRINGBOOT_CLASS = "org.springframework.boot.SpringApplication";
    public static final String SPRINGBOOT_JETTY_CLASS = "org.springframework.boot.web.embedded.jetty.JettyWebServer";
    public static final String SPRINGBOOT_UNDERTOW_CLASS="org.springframework.boot.web.embedded.undertow.UndertowWebServer";
    public static final String SPRINGBOOT_TOMCAT_CLASS="org.springframework.boot.web.embedded.tomcat.TomcatWebServer";
    public static final String STRUTS2_CLASS="org.apache.struts2.dispatcher.Dispatcher";


    public static final String TOMCAT="Tomcat";
    public static final String SPRINGBOOT_TOMCAT="SpringBootTomcat";
    public static final String SPRINGBOOT_JETTY="SpringBootJetty";
    public static final String SPRINGBOOT_UNDERTOW="SpringBootUndertow";
    public static final String JBOSS_EAP="JBossEAP";
    public static final String JETTY="Jetty";
//    public static final String WILDFLY="Wildfly";
    public static final String RESIN="Resin";
    public static final String WEBLOGIC="WebLogic";
    public static final String WEBSPHERE="WebSphere";

    public static final String SPRINGBOOT="SpringBoot";
    public static final String STRUTS2="Struts2";
}
