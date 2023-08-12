package com.briar.hook;

import jdk.internal.org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

/**
 * - Hook点管理模块 -
 * 保存Hook点的公用属性
 */
public abstract class CommonHook implements Opcodes {
    protected String type;
    protected List<HookClassAndMethod> hookClassAndMethodList;
    protected String mode;
    protected String checkClass;

    public void setHookClassAndMethodList(HookClassAndMethod hookClassAndMethod) {this.hookClassAndMethodList.add(hookClassAndMethod);}
    public void setMode(String mode) {
        this.mode = mode;
    }
    public void setCheckClass(String checkClass) {
        this.checkClass = checkClass;
    }
    public String getType(){
        return this.type;
    }
    public String getMode(){
        return this.mode;
    }
    public String getCheckClass(){
        return this.checkClass;
    }
    public List<HookClassAndMethod> getHookClassAndMethodList(){
        return this.hookClassAndMethodList;
    }

    protected abstract byte[] insertBeforeForType(String method, byte[] classfileBuffer) ;
    public byte[] insertBefore(String method,byte[] classfileBuffer){
        return insertBeforeForType(method,classfileBuffer);
    }
}
