package com.briar.hook;

import com.briar.constant.RASPInfo;
import com.briar.visitor.FileOperatorVisitor;
import com.briar.visitor.SSRFVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;
import java.util.List;

public class FileOperatorHook extends CommonHook{
    private List<String> fileExtension;
    private List<String> directory;
    private List<String> file;

    public List<String> getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension.add(fileExtension);
    }

    public List<String> getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory.add(directory);
    }

    public List<String> getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file.add(file);
    }

    public FileOperatorHook() {
        super.type= RASPInfo.FILEOPERATOR;
        super.hookClassAndMethodList=new LinkedList<>();
        this.fileExtension=new LinkedList<>();
        this.directory=new LinkedList<>();
        this.file=new LinkedList<>();
    }

    @Override
    public byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new FileOperatorVisitor(ASM5,writer,methodAndDesc,super.checkClass,reader.getClassName());
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] transformeredByteCode = writer.toByteArray();
        return transformeredByteCode;
    }
}
