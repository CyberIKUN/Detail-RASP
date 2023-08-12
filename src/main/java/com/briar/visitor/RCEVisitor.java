package com.briar.visitor;

import com.briar.constant.OperatorSystem;
import com.briar.info.WebInformation;
import com.briar.visitor.methodvisitor.RCELinuxMethodVisitor;
import com.briar.visitor.methodvisitor.RCEWindowsMethodVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import jdk.internal.org.objectweb.asm.commons.JSRInlinerAdapter;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class RCEVisitor extends ClassVisitor {
    private String methodName;
    private String checkClass;
    private String targetClass;
    private String desc;

    public RCEVisitor(int i, ClassVisitor classVisitor,String methodAndDesc,String checkClass,String targetClass) {
        super(i, classVisitor);
        this.methodName=methodAndDesc.substring(0,methodAndDesc.indexOf("("));
        this.desc=methodAndDesc.substring(methodAndDesc.indexOf("("));
        this.checkClass=checkClass;
        this.targetClass=targetClass;
    }

    //linux下ProcessImpl/UNIXProcess的forkAndExec
    //windows下ProcessImpl的create
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (OperatorSystem.LINUX.equals(WebInformation.getInstance().os)){
            if (name.equals(methodName)&&desc.equals(this.desc)){
                //当访问到forkAndExec方法时添加带有前缀的native方法；
                cv.visitMethod(access,"DetailRASP"+methodName,desc,signature,exceptions).visitEnd();


                int newAccess = access & ~Opcodes.ACC_NATIVE;//去掉native
                MethodVisitor methodVisitor = super.visitMethod(newAccess, name, desc, signature, exceptions);
                //在Java7以前的版本会用到jsr指令，本质原因是为了程序的兼容性，兼容Jar包和JDK一些老类
                return new RCELinuxMethodVisitor(ASM5, new JSRInlinerAdapter(methodVisitor, newAccess, name, desc, signature, exceptions),
                        newAccess, name, desc,checkClass,targetClass);
            }
        } else if (OperatorSystem.WINDOWS.equals(WebInformation.getInstance().os)) {
            if (name.equals(methodName)&&desc.equals(this.desc)){
                //当访问到create方法时添加自定义前缀的方法；
                cv.visitMethod(access,"DetailRASP"+methodName,desc,signature,exceptions).visitEnd();

                int newAccess= access & ~Opcodes.ACC_NATIVE;//去掉native
                MethodVisitor methodVisitor = super.visitMethod(newAccess, name, desc, signature, exceptions);
                //在Java7以前的版本会用到jsr指令，本质原因是为了程序的兼容性，兼容Jar包和JDK一些老类
                return new RCEWindowsMethodVisitor(ASM5, new JSRInlinerAdapter(methodVisitor, newAccess, name, desc, signature, exceptions),
                        newAccess, name, desc,checkClass,targetClass);
            }
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

}
