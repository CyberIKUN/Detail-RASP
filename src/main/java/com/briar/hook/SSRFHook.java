package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.JNDIVisitor;
import com.briar.visitor.SSRFVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;
import java.util.List;

public class SSRFHook extends CommonHook{
    private List<String> protocolBlacklist;
    private List<String> ipBlacklist;
    private List<String> domainBlacklist;
    private List<String> ipWhitelist;
    private List<String> domainWhitelist;

    public List<String> getProtocolBlacklist() {
        return protocolBlacklist;
    }

    public void setProtocolBlacklist(String protocolBlacklist) {
        this.protocolBlacklist.add(protocolBlacklist);
    }

    public List<String> getIpBlacklist() {
        return ipBlacklist;
    }

    public void setIpBlacklist(String ipBlacklist) {
        this.ipBlacklist.add(ipBlacklist);
    }

    public List<String> getDomainBlacklist() {
        return domainBlacklist;
    }

    public void setDomainBlacklist(String domainBlacklist) {
        this.domainBlacklist.add(domainBlacklist);
    }

    public List<String> getIpWhitelist() {
        return ipWhitelist;
    }

    public void setIpWhitelist(String ipWhitelist) {
        this.ipWhitelist.add(ipWhitelist);
    }

    public List<String> getDomainWhitelist() {
        return domainWhitelist;
    }

    public void setDomainWhitelist(String domainWhitelist) {
        this.domainWhitelist.add(domainWhitelist);
    }

    public SSRFHook(){
        super.type= RASPInfo.SSRP;
        super.hookClassAndMethodList=new LinkedList<>();
        this.domainBlacklist=new LinkedList<>();
        this.domainWhitelist=new LinkedList<>();
        this.ipBlacklist=new LinkedList<>();
        this.ipWhitelist=new LinkedList<>();
        this.protocolBlacklist=new LinkedList<>();
    }
    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new SSRFVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
