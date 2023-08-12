package com.briar.hook;

import com.briar.constant.OperatorSystem;
import com.briar.constant.RASPInfo;
import com.briar.constant.WebServer;
import com.briar.info.WebInformation;
import com.briar.visitor.JNDIVisitor;
import com.briar.visitor.RCEVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class RCEHook extends CommonHook{
    private List<String> whitelist;
    private Boolean blockAll;
    private String commonPattern;
    private String dnsCMDPattern;
    private String dnsDomainPattern;
    private List<String> command;
    private List<String> keywords;

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist.add(whitelist);
    }

    public Boolean getBlockAll() {
        return blockAll;
    }

    public void setBlockAll(Boolean blockAll) {
        this.blockAll = blockAll;
    }

    public String getCommonPattern() {
        return commonPattern;
    }

    public void setCommonPattern(String commonPattern) {
        this.commonPattern = commonPattern;
    }

    public String getDnsCMDPattern() {
        return dnsCMDPattern;
    }

    public void setDnsCMDPattern(String dnsCMDPattern) {
        this.dnsCMDPattern = dnsCMDPattern;
    }

    public String getDnsDomainPattern() {
        return dnsDomainPattern;
    }

    public void setDnsDomainPattern(String dnsDomainPattern) {
        this.dnsDomainPattern = dnsDomainPattern;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command.add(command);
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords.add(keywords);
    }

    public RCEHook() {
        super.type= RASPInfo.RCE;
        super.hookClassAndMethodList=new LinkedList<>();
        this.whitelist=new LinkedList<>();
        this.command = new LinkedList<>();
        this.keywords = new LinkedList<>();
        this.blockAll = true; // 默认拦截所有
    }


    @Override
    protected byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new RCEVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();


        return transformeredByteCode;

    }
}
