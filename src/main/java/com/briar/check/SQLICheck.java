package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.SQLIHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.DataSourceUtil;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.logging.Logger;

public class SQLICheck {

    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(SQLICheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void checkSQL(String sql) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SQLIHook sqliHook = HookUtil.getSQLIHook();
        if (sqliHook==null || sql==null){
            return;
        }
        String mode = sqliHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        matchSQLIBlacklist(sql, sqliHook, mode,context);

    }

    private static void matchSQLIBlacklist(String sql, SQLIHook sqliHook, String mode,Context context) throws BlockAttackException {
        if (sql.length()< sqliHook.getMinLength()|| sql.length()> sqliHook.getMaxLength()){
            return;
        }
        if (DataSourceUtil.isSQLInject(sql)){
            handleAttack(mode,sql,context);
        }
    }

    public static void checkPreSQL(Object preparedStatement) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        SQLIHook sqliHook = HookUtil.getSQLIHook();
        if (sqliHook==null || preparedStatement==null){
            return;
        }
        String mode = sqliHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(preparedStatement.getClass().getName());
            Method getPreparedSqlMethod = aClass.getDeclaredMethod("getPreparedSql");
            getPreparedSqlMethod.setAccessible(true);
            String sql = (String) getPreparedSqlMethod.invoke(preparedStatement);
            matchSQLIBlacklist(sql,sqliHook,mode,context);
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


    private static void handleAttack(String mode, String payload, Context context) throws BlockAttackException {
        LinkedList<String> stack = new LinkedList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stack.add("at "+stackTraceElement);
        }
        //不等于白名单，根据用户选择的模式进行相应的操作
        if(RASPInfo.LOG.equals(mode)){
            AttackInfo attackInfo = new AttackInfo(RASPInfo.SQLI,context,false,RASPInfo.SEVERITY_MEDIUM,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到SQL注入攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.SQLI,context,true,RASPInfo.SEVERITY_MEDIUM,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到SQL注入攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到SQL注入攻击！进行阻断！");
        }
    }
}
