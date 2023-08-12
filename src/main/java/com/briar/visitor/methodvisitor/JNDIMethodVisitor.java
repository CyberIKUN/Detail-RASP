package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class JNDIMethodVisitor extends AdviceAdapter {
    private String checkClass;
    public JNDIMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc,String checkClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
    }

    @Override
    protected void onMethodEnter() {
        if (checkClass == null||"".equals(checkClass)){
            return;
        }
        mv.visitVarInsn(ALOAD,1);
        //插入字节码
        mv.visitMethodInsn(INVOKESTATIC, checkClass, "check", "(Ljava/lang/Object;)V", false);
    }
}
