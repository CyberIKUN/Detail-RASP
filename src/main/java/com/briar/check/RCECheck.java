package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.RCEHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class RCECheck {

    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(RCECheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 1、根据模式进行处理：
     *  CLOSE：直接返回
     *  LOG：记录命令和参数
     *  BLOCK：检查命令和参数，若为黑名单，则抛出，中断执行
     * @param command
     * @param argBlock
     * @param length
     */
    public static void checkLinux(byte[] command,byte[] argBlock,int length) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }

        RCEHook rceHook = HookUtil.getRCEHook();
        if (rceHook==null){
            return;
        }
        String mode = rceHook.getMode();
        if (mode == null || "".equals(mode) || RASPInfo.CLOSE.equals(mode)){
            return;
        }


        if (command==null||command.length<=0){
            return;
        }
        //错误执行命令情况：命令块中带空格
        String cmd = new String(command);
        if(cmd.trim().indexOf(" ")!=-1){
            return;
        }
        cmd=cmd.toLowerCase();
        //错误执行命令情况：参数块中带空格
        String args=null;
        if (argBlock!=null&&argBlock.length>0){
            args = new String(argBlock);
            if (args.trim().indexOf(" ")!=-1){
                return;
            }
        }

        Boolean blockAll = rceHook.getBlockAll();
        if (blockAll){
            if (args==null||"".equals(args)){
                handleAttack(mode,cmd,context);
            } else {
                handleAttack(mode,cmd+" "+args,context);
            }
        }
        /**
         * 检查黑名单命令
         */
        List<String> cmdBlacklist = rceHook.getCommand();
        if (cmdBlacklist!=null&&cmdBlacklist.size()>0){
            for (String blackCMD : cmdBlacklist) {
                if (cmd.equals(blackCMD)){
                   handleAttack(mode,cmd,context);
                }
            }
        }

        List<String> keywords = rceHook.getKeywords();
        if (keywords!=null&&keywords.size()>0){
            for (String keyword : keywords) {
                if (args == null){
                    break;
                }
                if (args.contains(keyword)){
                    handleAttack(mode,args,context);
                }
            }
        }

        /**
         * 正则匹配
         */
        String all = cmd+" "+args;
        String commonPattern = rceHook.getCommonPattern();
        if (commonPattern!=null&&!"".equals(commonPattern)){
            if (Pattern.matches(commonPattern,all)){
                handleAttack(mode,all,context);
            }
        }

        String dnsCMDPattern = rceHook.getDnsCMDPattern();
        if (dnsCMDPattern!=null&&!"".equals(dnsCMDPattern)){
            if (Pattern.matches(dnsCMDPattern,all)){
                if (RASPInfo.LOG.equals(mode)){
                    //todo 记录日志
                } else if (RASPInfo.BLOCK.equals(mode)) {
                    //todo 抛出异常中断执行，并记录日志
                }
            }
        }

        String dnsDomainPattern = rceHook.getDnsDomainPattern();
        if (dnsDomainPattern!=null&&!"".equals(dnsDomainPattern)){
            if (Pattern.matches(dnsDomainPattern,all)){
                handleAttack(mode,all,context);
            }
        }


    }
    public static void checkWindows(String cmdstr) throws BlockAttackException {
        System.out.println("进入checkWindows");
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;

        }
        Context context=webInformation.context.get();
        System.out.println("context为："+context);
        if (context==null){
            return;
        }

        RCEHook rceHook = HookUtil.getRCEHook();
        if (rceHook==null){
            return;
        }
        String mode = rceHook.getMode();
        if (mode == null || "".equals(mode) || RASPInfo.CLOSE.equals(mode)){
            return;
        }
        Boolean blockAll = rceHook.getBlockAll();
        if (blockAll){
            handleAttack(mode,cmdstr,context);
        }
        if (cmdstr==null||"".equals(cmdstr)){
            return;
        }
        String newstr=cmdstr.trim();
        //命令
        int i=newstr.indexOf(" ");

        String command=null;
        List<String> args=new LinkedList<>();
        if (i==-1){
            command=newstr;
        }else{
            //存在参数，提取参数
            command=newstr.substring(1,i);
            //提取参数到args
            //存在参数时会自动为整条字符串加上双引号，这里需要去除双引号
            String argStr = newstr.substring(i).trim();
            argStr=argStr.substring(0,argStr.length()-1);
            char[] chars = argStr.toCharArray();
            StringBuffer stringBuffer = new StringBuffer();
            for (int a=0;a<chars.length;a++){
                if ((a+1)!=chars.length&&chars[a] != ' '&&chars[a+1] ==' '){
                    stringBuffer.append(chars[a]);
                    args.add(stringBuffer.toString());
                    stringBuffer=new StringBuffer();
                }  else if (a+1==chars.length){
                    stringBuffer.append(chars[a]);
                    args.add(stringBuffer.toString());
                    stringBuffer=new StringBuffer();
                } else if (chars[a] != ' '){
                    stringBuffer.append(chars[a]);
                }
            }
        }


        //检查命令执行参数

        List<String> cmdBlacklist = rceHook.getCommand();
        if (cmdBlacklist!=null&&cmdBlacklist.size()>0){
            for (String blackCMD : cmdBlacklist) {
                if (blackCMD.equals(command)){
                    handleAttack(mode,command,context);
                }
            }
        }

        List<String> keywords = rceHook.getKeywords();
        if (keywords!=null&&keywords.size()>0){
            for (String keyword : keywords) {
                if (newstr.contains(keyword)){
                    handleAttack(mode,newstr,context);
                }
            }
        }


        String commonPattern = rceHook.getCommonPattern();
        if (commonPattern!=null&&!"".equals(commonPattern)){
            if (Pattern.matches(commonPattern, newstr)){
                handleAttack(mode,newstr,context);
            }
        }

        String dnsCMDPattern = rceHook.getDnsCMDPattern();
        if (dnsCMDPattern!=null&&!"".equals(dnsCMDPattern)){
            if (Pattern.matches(dnsCMDPattern, newstr)){
                handleAttack(mode,newstr,context);
            }
        }

        String dnsDomainPattern = rceHook.getDnsDomainPattern();
        if (dnsDomainPattern!=null&&!"".equals(dnsDomainPattern)){
            if (Pattern.matches(dnsDomainPattern, newstr)){
                handleAttack(mode,newstr,context);
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
            AttackInfo attackInfo = new AttackInfo(RASPInfo.RCE,context,false,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到命令执行攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.RCE,context,true,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到命令执行攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到命令执行攻击！进行阻断！");
        }
    }

}
