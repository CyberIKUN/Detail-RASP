package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import jdk.internal.org.objectweb.asm.commons.Method;

public class HttpMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String methodName;
    private String currentClass;
    public HttpMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass, String currentClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.methodName=name;
        this.currentClass=currentClass;
    }


    @Override
    protected void onMethodEnter() {
        if ("io/undertow/servlet/handlers/ServletInitialHandler".equals(currentClass)){
            if ("dispatchRequest".equals(methodName)){
                mv.visitMethodInsn(INVOKESTATIC,"com/briar/info/WebInformation","clearContext","()V",false);
            } else if ("handleFirstRequest".equals(methodName)) {
                mv.visitVarInsn(ALOAD,1);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"storeRequestInfo","(Ljava/lang/Object;)V",false);
            } 
        } else if ("org/apache/catalina/connector/CoyoteAdapter".equals(currentClass)) {
            mv.visitMethodInsn(INVOKESTATIC,"com/briar/info/WebInformation","clearContext","()V",false);
        } else if ("org/apache/catalina/core/StandardWrapperValve".equals(currentClass)) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"storeTomcatRequestInfo","(Ljava/lang/Object;)V",false);
        } else if ("org/eclipse/jetty/server/Server".equals(currentClass)) {
            mv.visitMethodInsn(INVOKESTATIC,"com/briar/info/WebInformation","clearContext","()V",false);
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"storeJettyRequestInfo","(Ljava/lang/Object;)V",false);
        } else if ("org/sparkproject/jetty/server/Server".equals(currentClass)) {
            mv.visitMethodInsn(INVOKESTATIC,"com/briar/info/WebInformation","clearContext","()V",false);
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"storeJettyRequestInfo","(Ljava/lang/Object;)V",false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if ("io/undertow/servlet/spec/ServletInputStreamImpl".equals(currentClass)){
            dup();//写入长度
            mv.visitVarInsn(ALOAD,0);//ServletInputStreamImpl
            mv.visitVarInsn(ALOAD,1);//缓冲器
            mv.visitVarInsn(ALOAD,2);//写入的偏移地址
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"inputToOutput","(ILjava/lang/Object;[BI)V",false);
        }else if ("io/undertow/server/HttpServerExchange".equals(currentClass)) {
            dup();
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"storeParameter","(Ljava/lang/Object;)V",false);
        } else if ("org/apache/catalina/connector/InputBuffer".equals(currentClass)) {
            if ("read".equals(methodName)){
                dup();
                mv.visitVarInsn(ALOAD,0);//InputBuffer
                mv.visitVarInsn(ALOAD,1);//缓冲器
                mv.visitVarInsn(ALOAD,2);//写入的偏移地址
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"inputToOutput","(Ljava/lang/Integer;Ljava/lang/Object;[BLjava/lang/Integer;)V",false);
            } else if ("readByte".equals(methodName)) {
                dup();
                mv.visitVarInsn(ALOAD,0);//InputBuffer
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"inputToOutput","(ILjava/lang/Object;)V",false);
            }
        } else if ("org/eclipse/jetty/server/HttpInput".equals(currentClass)) {
            dup();
            mv.visitVarInsn(ALOAD,0);//HttpInput
            mv.visitVarInsn(ALOAD,1);//缓冲器
            mv.visitVarInsn(ALOAD,2);//写入的偏移地址
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"inputToOutput","(ILjava/lang/Object;[BI)V",false);
        } else if ("org/sparkproject/jetty/server/HttpInput".equals(currentClass)) {
            dup();
            mv.visitVarInsn(ALOAD,0);//HttpInput
            mv.visitVarInsn(ALOAD,1);//缓冲器
            mv.visitVarInsn(ALOAD,2);//写入的偏移地址
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"inputToOutput","(ILjava/lang/Object;[BI)V",false);
        }
    }
}
