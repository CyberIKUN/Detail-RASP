package com.briar.util;


import com.briar.constant.RASPInfo;
import com.briar.hook.*;


import com.briar.info.ConfigScanner;
import com.briar.info.WebInformation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HookUtil {
    public static List<String> getAllHookClass(){
        List<String> hookClasslist = new ArrayList<>();
        Set<CommonHook> hookContext = WebInformation.getInstance().hookContext;

        if (hookContext==null||hookContext.size()<=0){
            return hookClasslist;
        }

        for (CommonHook commonHook : hookContext) {
            List<HookClassAndMethod> allHookClassAndMethod = commonHook.getHookClassAndMethodList();
            if (allHookClassAndMethod==null||allHookClassAndMethod.size()<=0){
                continue;
            }
            for (HookClassAndMethod hookClassAndMethod : allHookClassAndMethod) {
                //得到的格式为：java/lang/ObjectInputStream，需要进行转换
                String hookClass = hookClassAndMethod.getHookClass();
                if (hookClass==null||"".equals(hookClass)){
                    continue;
                }
                hookClass = hookClass.replace("/",".");
                hookClasslist.add(hookClass);
            }
        }
        return hookClasslist;
    }
    public static SQLIHook getSQLIHook() {
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof SQLIHook){
                return (SQLIHook) commonHook;
            }
        }
        return null;
    }

    public static XXEHook getXXEHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof XXEHook){
                return (XXEHook) commonHook;
            }
        }
        return null;
    }
    public static ExpressionHook getExpressionHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof ExpressionHook){
                return (ExpressionHook) commonHook;
            }
        }
        return null;
    }
    public static DeserializableHook getDeserializableHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof DeserializableHook){
                return (DeserializableHook) commonHook;
            }
        }
        return null;
    }
    public static FileOperatorHook getFileOperatorHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof FileOperatorHook){
                return (FileOperatorHook) commonHook;
            }
        }
        return null;
    }
    public static SSRFHook getSSRFHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof SSRFHook){
                return (SSRFHook) commonHook;
            }
        }
        return null;
    }

    public static JNDIHook getJNDIHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof JNDIHook){
                return (JNDIHook) commonHook;
            }
        }
        return null;
    }

    public static RCEHook getRCEHook(){
        if (WebInformation.getInstance().hookContext==null||WebInformation.getInstance().hookContext.size()<=0){
            return null;
        }
        for (CommonHook commonHook : WebInformation.getInstance().hookContext) {
            if (commonHook instanceof RCEHook){
                return (RCEHook) commonHook;
            }
        }
        return null;
    }

}
