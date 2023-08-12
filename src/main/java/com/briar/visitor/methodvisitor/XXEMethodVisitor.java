package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class XXEMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String methodName;
    private String currentClass;

    public XXEMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass, String currentClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass = checkClass;
        this.methodName = name;
        this.currentClass = currentClass;
    }

    protected void onMethodEnter() {
        if ("org.dom4j.io.SAXReader".equals(this.currentClass) || "org.jdom.input.SAXBuilder".equals(this.currentClass) || "org.jdom2.input.SAXBuilder".equals(this.currentClass)) {
            this.mv.visitVarInsn(25, 0);
            this.mv.visitMethodInsn(184, this.checkClass, "updateConfig", "(Ljava/lang/Object;)V", false);
        }

    }

    protected void onMethodExit(int i) {
        if ("javax.xml.parsers.DocumentBuilderFactory".equals(this.currentClass) || "javax.xml.stream.XMLInputFactory".equals(this.currentClass) || "org.xml.sax.helpers.XMLReaderFactory".equals(this.currentClass)) {
            this.dup();
            this.mv.visitMethodInsn(184, this.checkClass, "updateConfigForJavaXML", "(Ljava/lang/Object;)V", false);
        }

    }
}
