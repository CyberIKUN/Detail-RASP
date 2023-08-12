package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.DeserializationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DeserializableHook extends CommonHook {
    private List<String> whitelist;
    private List<String> jsonYamlClass;
    private List<String> jsonYamlPackage;
    private List<String> xmlClass;
    private List<String> xmlPackage;
    private List<String> xmlKeywords;

    public List<String> getWhitelist() {
        return whitelist;
    }
    public void setWhitelist(String whitelist) {
        this.whitelist.add(whitelist);
    }
    public List<String> getJsonYamlClass() {
        return jsonYamlClass;
    }
    public void setJsonYamlClass(String jsonYamlClass) {
        this.jsonYamlClass.add(jsonYamlClass);
    }
    public List<String> getJsonYamlPackage() {
        return jsonYamlPackage;
    }
    public void setJsonYamlPackage(String jsonYamlPackage) {
        this.jsonYamlPackage.add(jsonYamlPackage);
    }
    public List<String> getXmlClass() {
        return xmlClass;
    }
    public void setXmlClass(String xmlClass) {
        this.xmlClass.add(xmlClass);
    }
    public List<String> getXmlPackage() {
        return xmlPackage;
    }
    public void setXmlPackage(String xmlPackage) {
        this.xmlPackage.add(xmlPackage) ;
    }
    public List<String> getXmlKeywords() {
        return xmlKeywords;
    }
    public void setXmlKeywords(String xmlKeywords) {
        this.xmlKeywords.add(xmlKeywords);
    }
    public void setHookClassAndMethodList(HookClassAndMethod hookClassAndMethod){
        super.hookClassAndMethodList.add(hookClassAndMethod);
    }
    public DeserializableHook() {
        super.type = RASPInfo.DESERIALIZATION;
        super.hookClassAndMethodList = new LinkedList<>();
        this.whitelist = new LinkedList<>();
        this.jsonYamlClass= new LinkedList<>();
        this.jsonYamlPackage=new LinkedList<>();
        this.xmlClass= new LinkedList<>();
        this.xmlPackage = new LinkedList<>();
        this.xmlKeywords = new LinkedList<>();
    }

    /**
     * 插入字节码
     * 调用检测模块
     */
    @Override
    public byte[] insertBeforeForType(String methodAndDesc,byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new DeserializationVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
