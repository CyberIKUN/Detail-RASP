package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.DeserializationVisitor;
import com.briar.visitor.JNDIVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;
import java.util.List;

public class JNDIHook extends CommonHook{
    private List<String> whitelistIp;
    private List<String> whitelistDomain;
    private List<String> blacklistProtocol;
    private List<String> blacklistIp;
    private List<String> blacklistDomain;

    public List<String> getWhitelistIp() {
        return whitelistIp;
    }
    public void setWhitelistIp(String whitelistIp) {
        this.whitelistIp.add(whitelistIp);
    }
    public List<String> getWhitelistDomain() {
        return whitelistDomain;
    }
    public void setWhitelistDomain(String whitelistDomain) {
        this.whitelistDomain.add(whitelistDomain);
    }
    public List<String> getBlacklistProtocol() {
        return blacklistProtocol;
    }
    public void setBlacklistProtocol(String blacklistProtocol) {
        this.blacklistProtocol.add(blacklistProtocol);
    }
    public List<String> getBlacklistIp() {
        return blacklistIp;
    }
    public void setBlacklistIp(String blacklistIp) {
        this.blacklistIp.add(blacklistIp);
    }
    public List<String> getBlacklistDomain() {
        return blacklistDomain;
    }
    public void setBlacklistDomain(String blacklistDomain) {
        this.blacklistDomain.add(blacklistDomain);
    }

    public JNDIHook() {
        super.type= RASPInfo.JNDIINJECT;
        super.hookClassAndMethodList = new LinkedList<>();
        this.whitelistIp=new LinkedList<>();
        this.whitelistDomain=new LinkedList<>();
        this.blacklistProtocol=new LinkedList<>();
        this.blacklistIp=new LinkedList<>();
        this.blacklistDomain=new LinkedList<>();
    }

    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new JNDIVisitor(ASM5,writer,methodAndDesc,super.checkClass);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
