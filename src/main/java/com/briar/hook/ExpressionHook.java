package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.ExpressionVisitor;
import com.briar.visitor.SQLIVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import org.w3c.dom.ls.LSException;

import java.util.LinkedList;
import java.util.List;

public class ExpressionHook extends CommonHook{
    private int ExpressionMinLength;
    private int SpelExpressionMaxLength;
    private int OgnlExpressionMaxLength;

    private List<String> Spelblacklist;
    private List<String> Ognlblacklist;

    public int getExpressionMinLength() {
        return ExpressionMinLength;
    }

    public void setExpressionMinLength(int expressionMinLength) {
        ExpressionMinLength = expressionMinLength;
    }

    public int getSpelExpressionMaxLength() {
        return SpelExpressionMaxLength;
    }

    public void setSpelExpressionMaxLength(int spelExpressionMaxLength) {
        SpelExpressionMaxLength = spelExpressionMaxLength;
    }

    public int getOgnlExpressionMaxLength() {
        return OgnlExpressionMaxLength;
    }

    public void setOgnlExpressionMaxLength(int ognlExpressionMaxLength) {
        OgnlExpressionMaxLength = ognlExpressionMaxLength;
    }

    public List<String> getSpelblacklist() {
        return Spelblacklist;
    }

    public void setSpelblacklist(String spelblacklist) {
        Spelblacklist.add(spelblacklist);
    }

    public List<String> getOgnlblacklist() {
        return Ognlblacklist;
    }

    public void setOgnlblacklist(String ognlblacklist) {
        Ognlblacklist.add(ognlblacklist);
    }

    public ExpressionHook() {
        super.type= RASPInfo.EXPRESSIONINJECT;
        super.hookClassAndMethodList=new LinkedList<>();
        this.Spelblacklist=new LinkedList<>();
        this.Ognlblacklist=new LinkedList<>();
    }

    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new ExpressionVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
