package com.briar.visitor;

import com.briar.visitor.methodvisitor.ExpressionMethodVisitor;
import com.briar.visitor.methodvisitor.FileOperatorMethodVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class ExpressionVisitor extends ClassVisitor {
    private String checkClass;
    private String methodName;
    private String targetClass;
    private String desc;
    public ExpressionVisitor(int i, ClassVisitor classVisitor,String methodAndDesc,String checkClass,String targetClass) {
        super(i, classVisitor);
        this.methodName=methodAndDesc.substring(0,methodAndDesc.indexOf("("));
        this.desc=methodAndDesc.substring(methodAndDesc.indexOf("("));
        this.checkClass=checkClass;
        this.targetClass=targetClass;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(methodName)&&desc.equals(this.desc)){
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if (checkClass==null||"".equals(checkClass)){
                return methodVisitor;
            }
            return new ExpressionMethodVisitor(ASM5,methodVisitor,access,name, desc,checkClass,targetClass);
        }
        return super.visitMethod(access,name,desc,signature,exceptions);
    }
}
