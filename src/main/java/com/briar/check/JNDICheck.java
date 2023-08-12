package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.JNDIHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class JNDICheck {
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(JNDICheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isWhiteIp = false;
    private static boolean isWhiteDomain = false;
    private static boolean isBlackProtocol=false;
    private static boolean isBlackIp = false;
    private static boolean isBlackDomain=false;

    public static void check(Object parameter) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }

        String name = (String) parameter;

        //格式：rmi://127.0.0.1/Object
        String protocol=name.substring(0,name.indexOf(":"));
        String ip= null;
        if (name.indexOf("/")==-1 || name.indexOf("/")+2>=name.length()){
            return;
        }
        if (name.substring(name.indexOf("/")+2).indexOf(":")!=-1){
             ip= name.substring(name.indexOf("/")+2,name.lastIndexOf(":"));
        }else{
             ip= name.substring(name.indexOf("/")+2,name.lastIndexOf("/"));
        }

        JNDIHook jndiHook = HookUtil.getJNDIHook();
        if (jndiHook==null){
            return;
        }
        String mode = jndiHook.getMode();
        if (mode == null || "".equals(mode) || RASPInfo.CLOSE.equals(mode)){
            return;
        }


        List<String> whitelistIp = jndiHook.getWhitelistIp();
        if (whitelistIp!=null&&whitelistIp.size()>0){
            for (String whiteIp : whitelistIp) {
                if (ip.equals(whiteIp)){
                    isWhiteIp=true;
                }
            }
            if (!isWhiteIp){
                handleAttack(mode,ip,context);
            }
        }

        List<String> whitelistDomain = jndiHook.getWhitelistDomain();
        if (whitelistDomain!=null&&whitelistDomain.size()>0){
            for (String whiteDomain : whitelistDomain) {
                if (ip.equals(whiteDomain)){
                    isWhiteDomain=true;
                }
            }
            if (!isWhiteDomain){
                handleAttack(mode,ip,context);
            }
        }

        List<String> blacklistProtocol = jndiHook.getBlacklistProtocol();
        if (blacklistProtocol!=null&&blacklistProtocol.size()>0){
            for (String blackProtocol : blacklistProtocol) {
                if (protocol.equals(blackProtocol)){
                    isBlackProtocol=true;
                }
            }
            if (isBlackProtocol){
                handleAttack(mode,protocol,context);
            }
        }

        List<String> blacklistIp = jndiHook.getBlacklistIp();
        if (blacklistIp!=null&&blacklistIp.size()>0){
            for (String blackIp : blacklistIp) {
                if (ip.equals(blackIp)){
                    isBlackIp=true;
                }
            }
            if (isBlackIp){
                handleAttack(mode,ip,context);
            }
        }

        List<String> blacklistDomain = jndiHook.getBlacklistDomain();
        if (blacklistDomain!=null&&blacklistDomain.size()>0){
            for (String blackDomain : blacklistDomain) {
                if (ip.equals(blackDomain)){
                    isBlackDomain=true;
                }
            }
            if (isBlackDomain){
                handleAttack(mode,ip,context);
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
            AttackInfo attackInfo = new AttackInfo(RASPInfo.JNDIINJECT,context,false,RASPInfo.SEVERITY_MEDIUM,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到JNDI注入攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.JNDIINJECT,context,true,RASPInfo.SEVERITY_MEDIUM,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到JNDI注入攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到JNDI注入攻击！进行阻断！");
        }
    }
}
