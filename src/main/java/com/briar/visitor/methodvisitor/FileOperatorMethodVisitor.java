package com.briar.visitor.methodvisitor;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class FileOperatorMethodVisitor extends AdviceAdapter {
    private String checkClass;
    private String methodName;
    private String currentClass;
    public FileOperatorMethodVisitor(int i, MethodVisitor methodVisitor, int access, String name, String desc, String checkClass, String currentClass) {
        super(i, methodVisitor, access, name, desc);
        this.checkClass=checkClass;
        this.methodName=name;
        this.currentClass=currentClass;
    }

    @Override
    protected void onMethodEnter() {
        if ("java.io.FileInputStream".equals(currentClass)){
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkFileRead","(Ljava/io/File;)V",false);
        } else if ("java.io.FileOutputStream".equals(currentClass)) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkFileUpload","(Ljava/io/File;)V",false);
        } else if ("java.io.File".equals(currentClass)) {
            if ("delete".equals(methodName)){
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkFileDelete","(Ljava/io/File;)V",false);
            } else if ("renameTo".equals(methodName)) {
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkFileUpload","(Ljava/io/File;)V",false);
            } else if ("list".equals(methodName)) {
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"checkFileList","(Ljava/io/File;)V",false);
            }
        } else if ("java.io.RandomAccessFile".equals(currentClass)) {
            mv.visitVarInsn(ALOAD,1);
            mv.visitVarInsn(ALOAD,2);
            mv.visitMethodInsn(INVOKESTATIC,checkClass,"convert","(Ljava/io/File;Ljava/lang/String;)V",false);
        } else if ("java.nio.file.Files".equals(currentClass)) {
            if ("readAllBytes".equals(methodName)||"newInputStream".equals(methodName)){
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"fileReadPathToFile","(Ljava/nio/file/Path;)V",false);
            } else if ("createFile".equals(methodName) || "newOutputStream".equals(methodName) || "copy".equals(methodName) || "move".equals(methodName)) {
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"fileUploadPathToFile","(Ljava/nio/file/Path;)V",false);
            } else if ("delete".equals(methodName) || "deleteIfExists".equals(methodName)) {
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"fileDeletePathToFile","(Ljava/nio/file/Path;)V",false);
            } else if ("newDirectoryStream".equals(methodName)) {
                mv.visitVarInsn(ALOAD,0);
                mv.visitMethodInsn(INVOKESTATIC,checkClass,"fileListPathToFile","(Ljava/nio/file/Path;)V",false);
            }
        }
    }
}
