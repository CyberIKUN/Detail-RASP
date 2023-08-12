package com.briar.transformer;



import com.briar.hook.*;
import com.briar.info.WebInformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class BriarClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)   {
        //className格式为：java/lang/ObjectInputStream，不用转换

        if (className == null || "".equals(className)||className.startsWith("com/briar")){
            return classfileBuffer;
        }

        WebInformation instance = WebInformation.getInstance();
        if (instance.hookContext == null && instance.hookContext.size()<=0){
            return classfileBuffer;
        }

        for (CommonHook commonHook : instance.hookContext) {
            byte[] newClassfileBuffer = matchClass(className, commonHook, classfileBuffer);
            if (!Arrays.equals(classfileBuffer,newClassfileBuffer)){
                return newClassfileBuffer;
            }
        }

        return classfileBuffer;
    }

    private byte[] matchClass(String className, CommonHook commonHook, byte[] classfileBuffer) {
        byte[] newClassfileBuffer = new byte[classfileBuffer.length];
        System.arraycopy(classfileBuffer,0,newClassfileBuffer,0,newClassfileBuffer.length);

        List<HookClassAndMethod> hookClassAndMethodList = commonHook.getHookClassAndMethodList();
        if (hookClassAndMethodList==null||hookClassAndMethodList.size()<=0){
            return classfileBuffer;
        }
        for (HookClassAndMethod hookClassAndMethod : hookClassAndMethodList) {
            String hookClass = hookClassAndMethod.getHookClass();
            if (hookClass==null||"".equals(hookClass)||!className.equals(hookClass)){
                continue;
            }
            List<String> hookMethods = hookClassAndMethod.getHookMethod();
            if (hookMethods==null||hookMethods.size()<=0){
                continue;
            }
            for (String hookMethod : hookMethods) {
                if (hookMethod!=null&&!"".equals(hookMethod)){
                    newClassfileBuffer = commonHook.insertBefore(hookMethod, newClassfileBuffer);
                }
            }
        }


        return newClassfileBuffer;

    }


}
