package com.briar.visitor;

import com.briar.visitor.methodvisitor.XXEMethodVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

public class XXEVisitor extends ClassVisitor{
    private String methodName;
    private String checkClass;
    private String currentClass;
    private String desc;

    public XXEVisitor(int i, ClassVisitor classVisitor, String methodAndDesc, String checkClass, String currentClass) {
        super(i, classVisitor);
        this.methodName = methodAndDesc.substring(0, methodAndDesc.indexOf("("));
        this.desc = methodAndDesc.substring(methodAndDesc.indexOf("("));
        this.checkClass = checkClass;
        this.currentClass = currentClass;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(this.methodName) && desc.equals(this.desc)) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            return (MethodVisitor)(this.checkClass != null && !"".equals(this.checkClass) ? new XXEMethodVisitor(327680, methodVisitor, access, name, desc, this.checkClass, this.currentClass) : methodVisitor);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
