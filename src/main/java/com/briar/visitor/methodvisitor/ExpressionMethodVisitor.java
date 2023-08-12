package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class ExpressionMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String methodName;
    private String currentClass;
    public ExpressionMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass, String currentClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.methodName=name;
        this.currentClass=currentClass;
    }

    @Override
    protected void onMethodEnter() {
        //直接拿到表达式的
        if ("org.springframework.expression.common.TemplateAwareExpressionParser".equals(currentClass)){
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkSpel","(Ljava/lang/String;)V",false);
        }
    }

    @Override
    protected void onMethodExit(int i) {
        if ("ognl.OgnlParser".equals(currentClass)) {
            dup(); // 复制栈顶的返回值
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkOgnl","(Ljava/lang/Object;)V",false);
        }
    }
}
