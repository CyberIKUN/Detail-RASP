package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.SSRFVisitor;
import com.briar.visitor.XXEVisitor;
import com.briar.visitor.methodvisitor.XXEMethodVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;

public class XXEHook extends CommonHook{

    public XXEHook() {
        super.hookClassAndMethodList=new LinkedList<>();
        super.type= RASPInfo.XXE;
    }

    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new XXEVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
