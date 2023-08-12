package com.briar.visitor;


import com.briar.visitor.methodvisitor.DeserializationMethodVisitor;
import com.briar.visitor.methodvisitor.JNDIMethodVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class JNDIVisitor extends ClassVisitor {
    private String methodName;
    private String checkClass;
    private String desc;
    public JNDIVisitor(int i, ClassVisitor classVisitor,String methodAndDesc,String checkClass) {
        super(i, classVisitor);
        this.methodName=methodAndDesc.substring(0,methodAndDesc.indexOf("("));
        this.desc=methodAndDesc.substring(methodAndDesc.indexOf("("));
        this.checkClass=checkClass;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(methodName)&&desc.equals(this.desc)){
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if (checkClass==null||"".equals(checkClass)){
                return methodVisitor;
            }
            return new JNDIMethodVisitor(ASM5,methodVisitor,access,name, desc,checkClass);
        }
        return super.visitMethod(access,name,desc,signature,exceptions);
    }
}
