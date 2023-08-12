package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.ExpressionHook;
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

public class ExpressionInjectCheck {
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(ExpressionInjectCheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void checkSpel(String expression) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        ExpressionHook expressionHook = HookUtil.getExpressionHook();
        if (expressionHook==null || expression==null || "".equals(expression)){
            return;
        }
        String mode = expressionHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        int expressionMinLength = expressionHook.getExpressionMinLength();
        int spelExpressionMaxLength = expressionHook.getSpelExpressionMaxLength();

        if (expression.length()<expressionMinLength||expression.length()>spelExpressionMaxLength){
            return;
        }

        List<String> spelblacklist = expressionHook.getSpelblacklist();
        if (spelblacklist==null||spelblacklist.size()<=0){
            return;
        }
        for (String s : spelblacklist) {
            if (expression.contains(s)){
                handleAttack(mode,expression,context);
            }
        }

    }
    public static void checkOgnl(Object node) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        ExpressionHook expressionHook = HookUtil.getExpressionHook();
        if (expressionHook==null || node==null){
            return;
        }
        String mode = expressionHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        String expression = String.valueOf(node);
        if (expression==null||"".equals(expression)){
            return;
        }

        int expressionMinLength = expressionHook.getExpressionMinLength();
        int ognlExpressionMaxLength = expressionHook.getOgnlExpressionMaxLength();



        if (expression.length()<expressionMinLength||expression.length()>ognlExpressionMaxLength){
            return;
        }

        List<String> ognlblacklist = expressionHook.getOgnlblacklist();
        if (ognlblacklist==null||ognlblacklist.size()<=0){
            return;
        }
        for (String s : ognlblacklist) {
            if (expression.contains(s)){
                handleAttack(mode,expression,context);
            }
        }
    }

    private static void handleAttack(String mode, String expression, Context context) throws BlockAttackException {
        LinkedList<String> stack = new LinkedList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stack.add("at "+stackTraceElement);
        }
        //不等于白名单，根据用户选择的模式进行相应的操作
        if(RASPInfo.LOG.equals(mode)){
            AttackInfo attackInfo = new AttackInfo(RASPInfo.EXPRESSIONINJECT,context,false,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),expression,stack);
            logger.info("遭受到表达式注入攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.EXPRESSIONINJECT,context,true,RASPInfo.SEVERITY_HIGH,System.currentTimeMillis(),expression,stack);
            logger.info("遭受到表达式注入攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到表达式攻击！进行阻断！");
        }
    }
}
