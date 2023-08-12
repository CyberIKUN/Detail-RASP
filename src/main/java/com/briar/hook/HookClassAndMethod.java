package com.briar.hook;

import java.util.LinkedList;
import java.util.List;

public class HookClassAndMethod {
    private String hookClass;
    private List<String> hookMethod;

    public String getHookClass() {
        return hookClass;
    }

    public List<String> getHookMethod() {
        return hookMethod;
    }

    public HookClassAndMethod() {
        this.hookMethod = new LinkedList<String>();
    }

    public void setHookClass(String hookClass) {
        this.hookClass = hookClass;
    }

    public void setHookMethod(String hookMethod) {
        this.hookMethod.add(hookMethod);
    }


}
