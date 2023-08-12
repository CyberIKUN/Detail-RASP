package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.SQLIVisitor;
import com.briar.visitor.SSRFVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;

public class SQLIHook extends CommonHook{
    private int minLength;
    private int maxLength;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public SQLIHook() {
        super.type= RASPInfo.SQLI;
        super.hookClassAndMethodList=new LinkedList<>();
    }

    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new SQLIVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
