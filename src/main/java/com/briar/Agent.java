package com.briar;


import com.briar.check.ExpressionInjectCheck;
import com.briar.constant.OperatorSystem;
import com.briar.constant.SQLSever;
import com.briar.constant.StartUpMode;
import com.briar.constant.WebServer;
import com.briar.exception.HookClassNotConfigException;
import com.briar.exception.InternetCannotAccessException;
import com.briar.exception.JavaHomePropertiesException;
import com.briar.exception.ToolsJarNotConfigException;
import com.briar.info.WebInformation;
import com.briar.transformer.BriarClassFileTransformer;
import com.briar.util.HookUtil;
import com.briar.util.LoggerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;


public class Agent
{
    public static String jarPath = null;
    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(Agent.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void init(Instrumentation instrumentation) {
        System.out.println("\033[31m    ____       __        _ __      ____  ___   _____ ____ \n" +
                "   / __ \\___  / /_____ _(_) /     / __ \\/   | / ___// __ \\\n" +
                "  / / / / _ \\/ __/ __ `/ / /_____/ \033[35m/_/ / /| | \\__ \\/ /_/ /\n" +
                " / /_/ /  __/ /_/ /_/ \033[36m/ / /_____/ _, _/ ___ |___/ / ____/ \n" +
                "/_____/\\___/\\__/\\__,_/_/_/     /_/ |_/_/  |_/____/_/      \n" +
                "                                        --author:ZeanHike  ");
        logger.info("成功Attach到其他jvm，开始初始化Agent");
        //设置默认的contextClassLoader
        Thread.currentThread().setContextClassLoader(Agent.class.getClassLoader());
        jarPath = com.briar.util.JarFile.getJarPath();

        /*将jar包添加到BootstrapClassLoader所加载的类的classpath中，防止类似java.io等类无法调用到agent.jar中的检测入口*/
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(jarPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public static void main(String[] args ) throws ToolsJarNotConfigException, URISyntaxException, InternetCannotAccessException {
        /**
         * 由于不同系统下的tools.jar包的内容不同，这些需要动态加载jar包，再进行动态attach；
         */
        try {
            System.out.println("尝试从本地查找tools.jar包路径···");
            String toolsPathStr = System.getProperty("java.home") + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
            File toolsJarfile = new File(toolsPathStr);
            if (!toolsJarfile.exists()) {
                System.out.println("本地查找不到tools.jar包，尝试从远程拉取···");
                //判断系统类型
                String systemName = System.getProperty("os.name");
                String arch = System.getProperty("os.arch");
                if (systemName.contains("Linux")) {
                    if (arch != null && !arch.equals("")) {
                        if (arch.equalsIgnoreCase("x86_64") || arch.equalsIgnoreCase("amd64")) {
                            toolsPathStr = "https://github.com/CyberIKUN/detail-rasp-tools/releases/download/tools/linux_x64_tools.jar";
                        } else if (arch.equalsIgnoreCase("aarch64") || arch.equalsIgnoreCase("arm64-v8a")) {
                            toolsPathStr = "https://github.com/CyberIKUN/detail-rasp-tools/releases/download/tools/linux_arm64_tools.jar";
                        }
                    }
                } else if (systemName.contains("Windows")) {
                    if ("amd64".equalsIgnoreCase(arch)) {
                        toolsPathStr = "https://github.com/CyberIKUN/detail-rasp-tools/releases/download/tools/windows_x64_tools.jar";
                    }
                }
                if (toolsPathStr.startsWith("https")) {
                    System.out.println("找到适合当前系统的tools.jar包，位于" + toolsPathStr + "！");

                } else {
                    throw new ToolsJarNotConfigException("无法找到适合本机的tools.jar包，请检查本机的JAVA_HOME环境变量设置！");
                }
            } else {
                System.out.println("成功从本地查找到tools.jar包！");
            }


            URL toolsPath = null;
            if (toolsPathStr.startsWith("https"))
            {
                InputStream inputStream = new URL(toolsPathStr).openConnection().getInputStream();
                URL location = Agent.class.getProtectionDomain().getCodeSource().getLocation();
                String downloadPath = new File(location.toURI()).getAbsolutePath()+File.separator+"..";
                File file = new File(downloadPath+File.separator+"tools.jar");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int i =0;
                while ((i=inputStream.read(bytes))!=-1){
                    fileOutputStream.write(bytes,0,i);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                toolsPath=new URL("file:"+file.getCanonicalPath());
            } else{
                toolsPath=new URL("file:"+toolsPathStr);
            }

            URLClassLoader myClassLoader1 = new URLClassLoader(new URL[] { toolsPath },Agent.class.getClassLoader());
            Class<?> virtualMachineClass = myClassLoader1.loadClass("com.sun.tools.attach.VirtualMachine");
            if (args.length==0){
                Method listMethod = virtualMachineClass.getDeclaredMethod("list");
                listMethod.setAccessible(true);
                List list = (List) listMethod.invoke(null);
                for (Object o : list) {
                    Class<?> virtualMachineDescriptorClass = myClassLoader1.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
                    Method idMethod = virtualMachineDescriptorClass.getDeclaredMethod("id");
                    idMethod.setAccessible(true);
                    String pid = (String) idMethod.invoke(o);
                    Method displayNameMethod = virtualMachineDescriptorClass.getDeclaredMethod("displayName");
                    displayNameMethod.setAccessible(true);
                    String displayName = (String) displayNameMethod.invoke(o);
                    System.out.println("进程ID："+pid+"，进程名称："+displayName);
                }
            }else{
                String pid = args[0];
                Method attachMethod = virtualMachineClass.getDeclaredMethod("attach",new Class[]{String.class});
                attachMethod.setAccessible(true);
                Object virtualMachine = attachMethod.invoke(null, pid);
                URL location = Agent.class.getProtectionDomain().getCodeSource().getLocation();
                String absolutePath = new File(location.toURI()).getAbsolutePath();

                Method loadAgentMethod = virtualMachineClass.getDeclaredMethod("loadAgent", new Class[]{String.class});
                loadAgentMethod.setAccessible(true);
                loadAgentMethod.invoke(virtualMachine,absolutePath);

                Method detachMethod = virtualMachineClass.getDeclaredMethod("detach");
                detachMethod.setAccessible(true);
                detachMethod.invoke(virtualMachine);
            }
        }  catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }  catch (MalformedURLException e) {
            throw new JavaHomePropertiesException("未设置JAVA_HOME环境变量！无法找到tools.jar包！");
        } catch (IOException e) {
            throw new InternetCannotAccessException("无法连接互联网！");
        }

    }
    public static void premain(String args, Instrumentation instrumentation) throws HookClassNotConfigException {
        init(instrumentation);
        Hook(args, StartUpMode.PREMAIN,instrumentation);
    };
    public static void agentmain(String args, Instrumentation instrumentation) throws HookClassNotConfigException {
        init(instrumentation);
        Hook(args,StartUpMode.ATTACH,instrumentation);
    }

    //Hook方法，添加字节码
    private static void Hook(String args, String mode,Instrumentation instrumentation) throws HookClassNotConfigException {
        //采集系统信息
        WebInformation.getInstance().init();
        BriarClassFileTransformer briarClassFileTransformer = new BriarClassFileTransformer();
        instrumentation.addTransformer(briarClassFileTransformer,true);


        if (instrumentation.isNativeMethodPrefixSupported()){
            instrumentation.setNativeMethodPrefix(briarClassFileTransformer,"DetailRASP");
        }


        if (mode.equals(StartUpMode.ATTACH)){
            //拿到所有需要Hook的类进行retransform()，得到的格式为：org.springframework.boot.jackson.JsonMixin
            List<String> hookClasslist = HookUtil.getAllHookClass();

            if (hookClasslist == null || hookClasslist.size()<=0){
                throw new HookClassNotConfigException("没有需要Hook的类，请检查配置！配置文件为HookArgsManager.yml！");
            }
            for (Class loadedClass : instrumentation.getAllLoadedClasses()) {
                if (instrumentation.isModifiableClass(loadedClass)){
                    //得到的格式为：org.springframework.boot.jackson.JsonMixin
                    String className = loadedClass.getName();
                    Map<String, List<String>> sqlMarkMap = SQLSever.sqlMarkMap;
                    if (sqlMarkMap!=null&&sqlMarkMap.size()>0){
                        for (Map.Entry<String, List<String>> sqlMarkMapEntry : sqlMarkMap.entrySet()) {
                            List<String> value = sqlMarkMapEntry.getValue();
                            if (value==null||value.size()<=0){
                                continue;
                            }
                            if (value.contains(className)){
                                WebInformation.getInstance().sqlInformation.add(sqlMarkMapEntry.getKey());
                            }
                        }
                    }

                    Map<String, String> frameworkMarkMap = WebServer.frameworkMarkMap;
                    if (frameworkMarkMap!=null&&frameworkMarkMap.size()>0){
                        for (Map.Entry<String, String> frameworkMarkMapEntry : frameworkMarkMap.entrySet()) {
                            String value = frameworkMarkMapEntry.getValue();
                            if (className.equals(value)){
                                WebInformation.getInstance().framework=frameworkMarkMapEntry.getKey();
                                break;
                            }
                        }
                    }


                    Map<String, String> webServerMarkMap = WebServer.WebServerMarkMap;
                    if (webServerMarkMap!=null&&webServerMarkMap.size()>0){
                        for (Map.Entry<String, String> webServerMarkMapEntry : webServerMarkMap.entrySet()) {
                            String value = webServerMarkMapEntry.getValue();
                            if (className.equals(value)){
                                WebInformation.getInstance().webServer=webServerMarkMapEntry.getKey();
                                break;
                            }
                        }
                    }

                    if (hookClasslist.contains(className) ){
                        try {
                            instrumentation.retransformClasses(loadedClass);
                        } catch (UnmodifiableClassException e) {
                            System.out.println(className);
                            throw new RuntimeException(e);
                        }catch (Throwable e){
                            //某些类例如InputBuffer，在加载进虚拟机后，再进行retransformClasses会存在VerifyError错误
                        }
                    }
                }
            }
        }
        logger.info("Agent加载完毕！");
    }
}
