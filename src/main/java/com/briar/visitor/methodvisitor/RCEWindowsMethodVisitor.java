package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import jdk.internal.org.objectweb.asm.commons.JSRInlinerAdapter;

public class RCEWindowsMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String targetClass;
    private String name;
    private String desc;
    public RCEWindowsMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass, String targetClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.name=name;
        this.desc=desc;
        this.targetClass=targetClass;
    }

    //方法区末尾添加指令
    @Override
    public void visitEnd() {
        if (checkClass == null||"".equals(checkClass)){
            //执行带有前缀的native方法
            loadArgs();
            mv.visitMethodInsn(INVOKESTATIC, targetClass,"DetailRASP"+name, desc, false);
            returnValue();
            super.visitEnd();
        }else{
            //检查命令执行参数
            mv.visitVarInsn(ALOAD,0); //命令+参数
            mv.visitMethodInsn(INVOKESTATIC, checkClass, "checkWindows", "(Ljava/lang/String;)V", false);
            //执行带有前缀的native方法
            loadArgs();
            mv.visitMethodInsn(INVOKESTATIC, targetClass,"DetailRASP"+name, desc, false);
            returnValue();
            super.visitEnd();
        }

    }
}
