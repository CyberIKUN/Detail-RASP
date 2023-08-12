package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.exception.BlockAttackException;
import com.briar.hook.FileOperatorHook;
import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.briar.info.WebInformation;
import com.briar.util.HookUtil;
import com.briar.util.JsonUtil;
import com.briar.util.LoggerUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class FileOperatorCheck {
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(FileOperatorCheck.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkFileUpload(File file) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        FileOperatorHook fileOperatorHook = HookUtil.getFileOperatorHook();
        if (fileOperatorHook==null || file==null){
            return;
        }
        String mode = fileOperatorHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }

        String name = file.getName();
        if (name==null||"".equals(name)){
            return;
        }

        String ext = name.trim().substring(name.trim().lastIndexOf("."));

        List<String> fileExtensionList = fileOperatorHook.getFileExtension();
        if (fileExtensionList!=null&&fileExtensionList.size()>0){
            for (String fileExtension : fileExtensionList) {
                if (fileExtension.equals(ext)){
                    handleAttack(mode,name,context);
                    break;
                }
            }
        }

    }
    public static void checkFileDelete(File file) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        FileOperatorHook fileOperatorHook = HookUtil.getFileOperatorHook();
        if (fileOperatorHook==null || file==null){
            return;
        }
        String mode = fileOperatorHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        String filePath = file.getAbsolutePath();
        if (filePath==null||"".equals(filePath)){
            return;
        }
        if (filePath.contains("../")||filePath.contains("..\\")){
            handleAttack(mode,filePath,context);
        }

    }
    public static void checkFileRead(File file) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        FileOperatorHook fileOperatorHook = HookUtil.getFileOperatorHook();
        if (fileOperatorHook==null || file==null){
            return;
        }
        String mode = fileOperatorHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        //防止目录穿越
        String filePath = file.getAbsolutePath();
        if (filePath==null||"".equals(filePath)){
            return;
        }
        if (filePath.contains("../")||filePath.contains("..\\")){
            handleAttack(mode,filePath,context);
        }
        //防止读取源码
        String name = file.getName();
        String ext = name.trim().substring(name.trim().lastIndexOf("."));

        List<String> fileExtensionList = fileOperatorHook.getFileExtension();
        if (fileExtensionList!=null&&fileExtensionList.size()>0){
            if (fileExtensionList.contains(ext)){
                handleAttack(mode,name,context);
            }
        }
        //防止读取敏感文件
        try {
            String canonicalFile = file.getCanonicalPath();
            List<String> sensitiveFileList = fileOperatorHook.getFile();
            if (sensitiveFileList!=null&&sensitiveFileList.size()>0){
                if (sensitiveFileList.contains(canonicalFile)){
                    handleAttack(mode,canonicalFile,context);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void checkFileList(File file) throws BlockAttackException {
        WebInformation webInformation = WebInformation.getInstance();
        if (webInformation==null){
            return;
        }
        Context context=webInformation.context.get();
        if (context==null){
            return;
        }
        FileOperatorHook fileOperatorHook = HookUtil.getFileOperatorHook();
        if (fileOperatorHook==null || file==null){
            return;
        }
        String mode = fileOperatorHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        List<String> directory = fileOperatorHook.getDirectory();
        if (directory==null||directory.size()<=0){
            return;
        }
        try {
            if (directory.contains(file.getCanonicalPath())){
               handleAttack(mode,file.getCanonicalPath(),context);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void convert(File file,String mode) throws BlockAttackException {
        if (mode!=null&&mode.contains("rw")){
            checkFileUpload(file);
        }else {
            checkFileRead(file);
        }
    }
    public static void fileReadPathToFile(Path path) throws BlockAttackException {
        checkFileRead(path.toFile());
    }
    public static void fileUploadPathToFile(Path path) throws BlockAttackException {
        checkFileUpload(path.toFile());
    }
    public static void fileDeletePathToFile(Path path) throws BlockAttackException {
        checkFileDelete(path.toFile());
    }
    public static void fileListPathToFile(Path path) throws BlockAttackException {
        checkFileList(path.toFile());
    }

    private static void handleAttack(String mode, String payload, Context context) throws BlockAttackException {
        LinkedList<String> stack = new LinkedList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stack.add("at "+stackTraceElement);
        }
        //不等于白名单，根据用户选择的模式进行相应的操作
        if(RASPInfo.LOG.equals(mode)){
            AttackInfo attackInfo = new AttackInfo(RASPInfo.FILEOPERATOR,context,false,RASPInfo.SEVERITY_LOW,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到文件操作攻击，RASP选择的模式为："+mode+"；未阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
        } else if (RASPInfo.BLOCK.equals(mode)) {
            AttackInfo attackInfo = new AttackInfo(RASPInfo.FILEOPERATOR,context,true,RASPInfo.SEVERITY_LOW,System.currentTimeMillis(),payload,stack);
            logger.info("遭受到文件操作攻击，RASP选择的模式为："+mode+"；已阻塞攻击！");
            logger.info(JsonUtil.toJson(attackInfo));
            WebInformation.saveAttackInfo(attackInfo);
            throw new BlockAttackException("遭受到文件操作攻击！进行阻断！");
        }
    }
}
