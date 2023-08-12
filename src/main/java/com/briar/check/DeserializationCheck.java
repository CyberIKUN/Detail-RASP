package com.briar.check;


import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.DeserializableHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class DeserializationCheck {
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(DeserializationCheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String YAML_TAG = "tag:yaml.org,2002:";

    public static void checkJDK(Object parameter) throws  BlockAttackException,SQLException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        DeserializableHook deserializableHook = HookUtil.getDeserializableHook();
        if (deserializableHook==null || parameter==null){
            return;
        }
        String mode = deserializableHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }


        ObjectStreamClass objectStreamClass=(ObjectStreamClass) parameter;
        String className = objectStreamClass.getName();
        matchBlackAndWhitelist(deserializableHook, mode, className,context);
    }
    public static void checkFastJson(Object parameter) throws BlockAttackException,SQLException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        DeserializableHook deserializableHook = HookUtil.getDeserializableHook();
        if (deserializableHook==null || parameter==null){
            return;
        }
        String mode = deserializableHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        matchBlackAndWhitelist(deserializableHook, mode, (String) parameter,context);

    }
    public static void checkJackson(Object parameter) throws BlockAttackException,SQLException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        DeserializableHook deserializableHook = HookUtil.getDeserializableHook();
        if (deserializableHook==null || parameter==null){
            return;
        }
        String mode = deserializableHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        try {
            Class<?> beanDeserializerClass = Thread.currentThread().getContextClassLoader().loadClass(parameter.getClass().getName());
            Method handledTypeMethod = beanDeserializerClass.getDeclaredMethod("handledType");
            handledTypeMethod.setAccessible(true);
            Class aClass = (Class) handledTypeMethod.invoke(parameter);
            String className =aClass.getName();
            matchBlackAndWhitelist(deserializableHook, mode, (String) parameter,context);
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
    public static void checkXstream(Object parameter) throws BlockAttackException,SQLException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        DeserializableHook deserializableHook = HookUtil.getDeserializableHook();
        if (deserializableHook==null || parameter==null){
            return;
        }
        String mode = deserializableHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        String className=((ObjectStreamClass)parameter).getName();
        matchBlackAndWhitelist(deserializableHook,mode,className,context);

    }
    public static void checkSnakeyaml(Object parameter) throws BlockAttackException,SQLException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        DeserializableHook deserializableHook = HookUtil.getDeserializableHook();
        if (deserializableHook==null || parameter==null){
            return;
        }
        String mode = deserializableHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        try {

            Class<?> nodeClass = Thread.currentThread().getContextClassLoader().loadClass(parameter.getClass().getName());
            Method getTag = nodeClass.getDeclaredMethod("getTag");
            getTag.setAccessible(true);
            Object tag =  getTag.invoke(parameter);

            if (tag == null) {
                return;
            }
            Class<?> tagClass = Thread.currentThread().getContextClassLoader().loadClass(tag.getClass().getName());
            Method toString = tagClass.getDeclaredMethod("toString");
            toString.setAccessible(true);
            String className = (String) toString.invoke(tag);
            if (className==null||"".equals(className)){
                return;
            }
            className = URLDecoder.decode(className.substring(YAML_TAG.length()), "UTF-8");


            matchBlackAndWhitelist(deserializableHook, mode, className,context);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }


    }

    private static void matchBlackAndWhitelist(DeserializableHook deserializableHook, String mode, String className,Context context) throws BlockAttackException, SQLException {
        //白名单检测
        List<String> whitelist = deserializableHook.getWhitelist();
        if (whitelist != null && whitelist.size()>0){
            if (!whitelist.contains(className)){
                handleAttack(mode,className,context);
            }
        }
        //黑名单
        List<String> jsonYamlClass = deserializableHook.getJsonYamlClass();
        if (jsonYamlClass != null && jsonYamlClass.size()>0){
            if (jsonYamlClass.contains(className)){
                handleAttack(mode,className,context);
            }
        }

        List<String> jsonYamlPackage = deserializableHook.getJsonYamlPackage();
        if (jsonYamlPackage != null && jsonYamlPackage.size()>0){
            for (String s : jsonYamlPackage) {
                if (className.contains(s)){
                    handleAttack(mode,className,context);
                    break;
                }
            }
        }

        List<String> xmlClass = deserializableHook.getXmlClass();
        if (xmlClass != null && xmlClass.size()>0){
            if (xmlClass.contains(className)){
                handleAttack(mode,className,context);
            }
        }

        List<String> xmlPackage = deserializableHook.getXmlPackage();
        if (xmlPackage != null && xmlPackage.size()>0){
            for (String s : xmlPackage) {
                if (className.contains(s)){
                    handleAttack(mode,className,context);
                    break;
                }
            }
        }

        List<String> xmlKeywords = deserializableHook.getXmlKeywords();
        if (xmlKeywords != null && xmlKeywords.size()>0){
            for (String xmlKeyword : xmlKeywords) {
                if (className.contains(xmlKeyword)){
                    handleAttack(mode,className,context);
                    break;
                }
            }
        }
    }

    private static void handleAttack(String mode,String className,Context context) throws BlockAttackException, SQLException {
        LinkedList<String> stack = new LinkedList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stack.add("at "+stackTraceElement);
        }
        //不等于白名单，根据用户选择的模式进行相应的操作
        if(RASPInfo.LOG.equals(mode)){
            AttackInfo attackInfo = new AttackInfo(RASPInfo.DESERIALIZATION,context,false,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),className,stack);
            logger.info("遭受到反序列化攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.DESERIALIZATION,context,true,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),className,stack);
            logger.info("遭受到反序列化攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到反序列化攻击！进行阻断！");
        }
    }

}
