package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class RCELinuxMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String targetClass;
    private String name;
    private String desc;
    public RCELinuxMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc,String checkClass,String targetClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.name=name;
        this.desc=desc;
        this.targetClass=targetClass;
    }



    //方法区末尾添加指令
    @Override
    public void visitEnd() {
        System.out.println("开始修改字节码："+checkClass+"："+targetClass+"："+name+"："+desc);
        if (checkClass == null||"".equals(checkClass)){
            //执行带有前缀的native方法
            loadThis();
            loadArgs();
            mv.visitMethodInsn(INVOKESPECIAL, targetClass,"DetailRASP"+name, desc, false);
            returnValue();
            super.visitEnd();
        }else{
            //检查命令执行参数
            mv.visitVarInsn(ALOAD,3); //命令
            mv.visitVarInsn(ALOAD,4); //参数
            mv.visitVarInsn(ALOAD,5); //长度
            mv.visitMethodInsn(INVOKESTATIC, checkClass, "checkLinux", "([B[BI)V", false);
            //执行带有前缀的native方法
            loadThis();
            loadArgs();
            mv.visitMethodInsn(INVOKESPECIAL, targetClass,"DetailRASP"+name, desc, false);
            returnValue();
            super.visitEnd();
        }
        System.out.println("修改结束");
    }
}
