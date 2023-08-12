package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class SSRFMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String methodName;
    private String currentClass;
    public SSRFMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass,String currentClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.methodName=name;
        this.currentClass=currentClass;
    }

    @Override
    protected void onMethodEnter() {
        if (checkClass == null||"".equals(checkClass)){
            return;
        }
        if (currentClass.equals("java.net.Socket")&&methodName.equals("<init>")){
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkSocket","(Ljava/lang/Object;)V",false);
        } else if (currentClass.equals("okhttp3.internal.http.RealInterceptorChain")&&methodName.equals("proceed")) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkOkhttp3","(Ljava/lang/Object;)V",false);
        } else if (currentClass.equals("com.squareup.okhttp.Call$ApplicationInterceptorChain")&&methodName.equals("proceed")) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkOkhttp2","(Ljava/lang/Object;)V",false);
        } else if (currentClass.equals("org.apache.http.impl.client.CloseableHttpClient")&&methodName.equals("execute")) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkHttpClient","(Ljava/lang/Object;)V",false);
        }
    }
}
