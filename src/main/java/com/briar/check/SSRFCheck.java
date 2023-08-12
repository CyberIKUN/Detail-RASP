package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.SSRFHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SSRFCheck {
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(SSRFCheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkSocket(Object socketAddress) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SSRFHook ssrfHook = HookUtil.getSSRFHook();
        if (ssrfHook==null || socketAddress==null){
            return;
        }
        String mode = ssrfHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        if (socketAddress instanceof InetSocketAddress) {
            String hostName = ((InetSocketAddress) socketAddress).getHostName();
            int port = ((InetSocketAddress) socketAddress).getPort();

            if (hostName==null||"".equals(hostName)){
                return;
            }
            matchBlackAndWhiteList(ssrfHook, mode, hostName,null,context);
        }
    }
    public static void checkOkhttp3(Object request) throws BlockAttackException{
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SSRFHook ssrfHook = HookUtil.getSSRFHook();
        if (ssrfHook==null || request==null){
            return;
        }
        String mode = ssrfHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        try {
            Class<?> requestClass = Thread.currentThread().getContextClassLoader().loadClass(request.getClass().getName());
            Method urlMethod = requestClass.getDeclaredMethod("url");
            urlMethod.setAccessible(true);
            Object url = urlMethod.invoke(request);

            Class<?> urlClass = Thread.currentThread().getContextClassLoader().loadClass(url.getClass().getName());
            Method hostMethod = urlClass.getDeclaredMethod("host");
            Method portMethod = urlClass.getDeclaredMethod("port");
            Method toStringMethod = urlClass.getDeclaredMethod("toString");
            hostMethod.setAccessible(true);
            portMethod.setAccessible(true);
            toStringMethod.setAccessible(true);
            String hostName = (String) hostMethod.invoke(url);
            int port = (int) portMethod.invoke(url);
            String toString = (String) toStringMethod.invoke(url);

            matchBlackAndWhiteList(ssrfHook, mode, hostName,toString,context);
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
    public static void checkOkhttp2(Object request) throws BlockAttackException{
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SSRFHook ssrfHook = HookUtil.getSSRFHook();
        if (ssrfHook==null || request==null){
            return;
        }
        String mode = ssrfHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        try {
            Class<?> requestClass = Thread.currentThread().getContextClassLoader().loadClass(request.getClass().getName());
            Method urlMethod = requestClass.getDeclaredMethod("url");
            urlMethod.setAccessible(true);
            URL url = (URL) urlMethod.invoke(request);

            String hostName = url.getHost();
            int port = url.getPort();

            matchBlackAndWhiteList(ssrfHook, mode, hostName,url.toString(),context);
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
    public static void checkHttpClient(Object httpHost) throws BlockAttackException{
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SSRFHook ssrfHook = HookUtil.getSSRFHook();
        if (ssrfHook==null || httpHost==null){
            return;
        }
        String mode = ssrfHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        try {
            Class<?> requestClass = Thread.currentThread().getContextClassLoader().loadClass(httpHost.getClass().getName());
            Method getHostNameMethod = requestClass.getDeclaredMethod("getHostName");
            getHostNameMethod.setAccessible(true);
            String hostName = (String) getHostNameMethod.invoke(httpHost);

            Method getPortMethod = requestClass.getDeclaredMethod("getPort");
            getPortMethod.setAccessible(true);
            String port = (String) getPortMethod.invoke(httpHost);

            Method toStringMethod = requestClass.getDeclaredMethod("toString");
            toStringMethod.setAccessible(true);
            String url = (String) toStringMethod.invoke(httpHost);

            matchBlackAndWhiteList(ssrfHook, mode, hostName,url,context);

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
    private static void matchBlackAndWhiteList(SSRFHook ssrfHook, String mode, String hostName,String url,Context context) throws BlockAttackException {
        List<String> ipWhitelist = ssrfHook.getIpWhitelist();
        if (ipWhitelist != null && ipWhitelist.size()>0) {
            if (!ipWhitelist.contains(hostName)){
                handleAttack(mode,hostName,context);
            }
        }

        List<String> domainWhitelist = ssrfHook.getDomainWhitelist();
        if (domainWhitelist!=null && domainWhitelist.size()>0){
            if (!domainWhitelist.contains(hostName)){
                handleAttack(mode,hostName,context);
            }
        }

        if (url!=null){
            List<String> protocolBlacklist = ssrfHook.getProtocolBlacklist();
            if (protocolBlacklist!=null&&protocolBlacklist.size()>0){
                for (String protocol : protocolBlacklist) {
                    if (url.contains(protocol)){
                        handleAttack(mode,url,context);
                        break;
                    }
                }
            }
        }


        List<String> ipBlacklist = ssrfHook.getIpBlacklist();
        if (ipBlacklist!=null&&ipBlacklist.size()>0){
            if (ipBlacklist.contains(hostName)){
                handleAttack(mode,hostName,context);
            }
        }

        List<String> domainBlacklist = ssrfHook.getDomainBlacklist();
        if (domainBlacklist!=null&&domainBlacklist.size()>0){
           if (domainBlacklist.contains(hostName)){
               handleAttack(mode,hostName,context);
           }
        }
    }


    private static void handleAttack(String mode, String payload, Context context) throws BlockAttackException {
        LinkedList<String> stack = new LinkedList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stack.add("at "+stackTraceElement);
        }
        //不等于白名单，根据用户选择的模式进行相应的操作
        if(RASPInfo.LOG.equals(mode)){
            AttackInfo attackInfo = new AttackInfo(RASPInfo.SSRP,context,false,RASPInfo.SEVERITY_LOW,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到SSRF攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.SSRP,context,true,RASPInfo.SEVERITY_LOW,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到SSRF攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到SSRF攻击！进行阻断！");
        }
    }
}
