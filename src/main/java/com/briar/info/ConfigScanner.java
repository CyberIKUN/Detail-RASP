package com.briar.info;

import com.briar.constant.RASPInfo;
import com.briar.hook.*;
import com.briar.util.LoggerUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 加载配置文件
 */
public class ConfigScanner {

    private static final Logger logger;

    static {
        try {
            logger = LoggerUtil.getLogger(ConfigScanner.class.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String configPath="HookArgsManager.yml";
    public static void scan(){
        if (ConfigScanner.class.getClassLoader()==null){
            logger.info("开始加载配置文件HookArgsManager.yml（包括Hook类、Hook方法以及黑白名单）");
        }
        String jarPath = null;
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> aClass = contextClassLoader.loadClass("com.briar.Agent");
            Field jarPath1 = aClass.getDeclaredField("jarPath");
            jarPath = (String) jarPath1.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JarEntry jarEntry = jarFile.getJarEntry(configPath);
        InputStream inputStream = null;
        try {
            inputStream = jarFile.getInputStream(jarEntry);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Yaml yaml = new Yaml(new SafeConstructor());
        Map map =(Map) yaml.load(inputStream);

        //封装信息
        List<Map> module = (List) map.get("modules");
        for (Map map1 : module) {
            String type = (String) map1.get("type");
            if ("".equals(type) || type == null){
                continue;
            }
            CommonHook commonHook = parseCommonHook(map1,type);
            if (commonHook==null){
                continue;
            }
            //根据不同类型漏洞的差距具体解析特定属性
            if (RASPInfo.DESERIALIZATION.equals(type)){
                parseDeserialization(map1, (DeserializableHook) commonHook);
                WebInformation.getInstance().hookContext.add((DeserializableHook)commonHook);
                continue;
            }

            if (RASPInfo.JNDIINJECT.equals(type)){
                parseJNDIINJECT(map1,(JNDIHook)commonHook);
                WebInformation.getInstance().hookContext.add((JNDIHook)commonHook);
                continue;
            }

            if (RASPInfo.RCE.equals(type)){
                parseRCE(map1,(RCEHook)commonHook);
                WebInformation.getInstance().hookContext.add((RCEHook)commonHook);
                continue;
            }

            if (RASPInfo.SSRP.equals(type)){
                parseSSRF(map1,(SSRFHook)commonHook);
                WebInformation.getInstance().hookContext.add((SSRFHook)commonHook);
                continue;
            }

            if (RASPInfo.SQLI.equals(type)){
                parseSQLI(map1,(SQLIHook)commonHook);
                WebInformation.getInstance().hookContext.add((SQLIHook)commonHook);
                continue;
            }

            if (RASPInfo.FILEOPERATOR.equals(type)){
                parseFileOperator(map1,(FileOperatorHook)commonHook);
                WebInformation.getInstance().hookContext.add((FileOperatorHook)commonHook);
                continue;
            }

            if (RASPInfo.EXPRESSIONINJECT.equals(type)){
                parseExpression(map1,(ExpressionHook)commonHook);
                WebInformation.getInstance().hookContext.add((ExpressionHook)commonHook);
                continue;
            }

            if (RASPInfo.XXE.equals(type)){
                WebInformation.getInstance().hookContext.add((XXEHook)commonHook);
                continue;
            }
        }
        WebInformation.getInstance().hookContext.add(new HttpHook());
        if (ConfigScanner.class.getClassLoader()==null){
            logger.info("配置文件加载完毕！");
        }
    }

    private static void parseExpression(Map map1, ExpressionHook expressionHook) {
        Integer expressionMinLength = (Integer) map1.get("expression_min_length");
        if (expressionMinLength!=null){
            expressionHook.setExpressionMinLength(expressionMinLength);
        }

        Integer spelExpressionMaxLength = (Integer) map1.get("spel_expression_max_length");
        if (spelExpressionMaxLength!=null){
            expressionHook.setSpelExpressionMaxLength(spelExpressionMaxLength);
        }

        Integer ognlExpressionMaxLength = (Integer) map1.get("ognl_expression_max_length");
        if (ognlExpressionMaxLength!=null){
            expressionHook.setOgnlExpressionMaxLength(ognlExpressionMaxLength);
        }

        Map<String,List<String>> blacklist = (Map<String, List<String>>) map1.get("blacklist");
        if (blacklist==null||blacklist.size()<=0){
            return;
        }
        List<String> blacklistSpel = blacklist.get("blacklist_spel");
        if (blacklistSpel!=null&&blacklistSpel.size()>0){
            for (String s : blacklistSpel) {
                expressionHook.setSpelblacklist(s);
            }
        }

        List<String> blacklistOgnl = blacklist.get("blacklist_ognl");
        if (blacklistOgnl!=null&&blacklistOgnl.size()>0){
            for (String s : blacklistOgnl) {
                expressionHook.setOgnlblacklist(s);
            }
        }
    }

    private static void parseFileOperator(Map map1, FileOperatorHook fileOperatorHook) {
        Map<String,List<String>> blacklist = (Map<String, List<String>>) map1.get("blacklist");
        if (blacklist==null||blacklist.size()<=0){
            return;
        }
        List<String> fileExtensionList = blacklist.get("file_extension");
        if (fileExtensionList!=null&&fileExtensionList.size()>0){
            for (String fileExtension : fileExtensionList) {
                fileOperatorHook.setFileExtension(fileExtension);
            }
        }

        List<String> directoryList = blacklist.get("directory");
        if (directoryList!=null&&directoryList.size()>0){
            for (String directory : directoryList) {
                fileOperatorHook.setDirectory(directory);
            }
        }

        List<String> fileList = blacklist.get("file");
        if (fileList!=null&&fileList.size()>0){
            for (String file : fileList) {
                fileOperatorHook.setFile(file);
            }
        }
    }

    private static void parseSQLI(Map map1, SQLIHook sqliHook) {
        Integer minLength = (Integer) map1.get("min_length");
        sqliHook.setMinLength(minLength);

        Integer maxLength=(Integer) map1.get("max_length");
        sqliHook.setMaxLength(maxLength);

    }

    private static void parseSSRF(Map map1, SSRFHook ssrfHook) {
        Map<String,List<String>> whitelist = (Map<String, List<String>>) map1.get("whitelist");
        if (whitelist != null && whitelist.size()>0 ) {
            List<String> ipWhitelist = whitelist.get("ip");
            if (ipWhitelist!=null&&ipWhitelist.size()>0){
                for (String whiteip : ipWhitelist) {
                    ssrfHook.setIpWhitelist(whiteip);
                }
            }

            List<String> domainWhitelist = whitelist.get("domain");
            if (domainWhitelist!=null&&domainWhitelist.size()>0){
                for (String whiteDomain : domainWhitelist) {
                    ssrfHook.setDomainWhitelist(whiteDomain);
                }
            }
        }
        Map<String,List<String>> blacklist = (Map<String, List<String>>) map1.get("blacklist");
        if (blacklist != null && blacklist.size()>0 ) {
            List<String> ipBlacklist = blacklist.get("ip");
            if (ipBlacklist!=null&&ipBlacklist.size()>0){
                for (String blackIp : ipBlacklist) {
                    ssrfHook.setIpBlacklist(blackIp);
                }
            }

            List<String> domainBlacklist = blacklist.get("domain");
            if (domainBlacklist!=null&&domainBlacklist.size()>0){
                for (String blackDomain : domainBlacklist) {
                    ssrfHook.setDomainBlacklist(blackDomain);
                }
            }

            List<String> protocolBlacklist = blacklist.get("protocol");
            if (protocolBlacklist!=null&&protocolBlacklist.size()>0){
                for (String protocol : protocolBlacklist) {
                    ssrfHook.setProtocolBlacklist(protocol);
                }
            }
        }



        //解决白名单和黑名单冲突
        List<String> blacklistIp = ssrfHook.getIpBlacklist();
        List<String> blacklistDomain = ssrfHook.getDomainBlacklist();
        List<String> whitelistIp = ssrfHook.getIpWhitelist();
        List<String> whitelistDomain = ssrfHook.getDomainWhitelist();
        if (blacklistIp != null&&blacklistIp.size()>0){
            if (whitelistIp != null&&whitelistIp.size()>0){
                for (String whiteIp : whitelistIp) {
                    if (blacklistIp.contains(whiteIp)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
            if (whitelistDomain!=null&&whitelistDomain.size()>0){
                for (String whiteDomain : whitelistDomain) {
                    if (blacklistIp.contains(whiteDomain)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
        }
        if (blacklistDomain!=null&&blacklistDomain.size()>0){
            if (whitelistIp != null&&whitelistIp.size()>0){
                for (String whiteIp : whitelistIp) {
                    if (blacklistDomain.contains(whiteIp)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
            if (whitelistDomain!=null&&whitelistDomain.size()>0){
                for (String whiteDomain : whitelistDomain) {
                    if (blacklistDomain.contains(whiteDomain)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
        }
    }

    private static void parseRCE(Map map1, RCEHook rceHook) {
        List<String> whitelist = (List<String>) map1.get("whitelist");
        if (whitelist!=null&&whitelist.size()>0){
            for (String white : whitelist) {
                rceHook.setWhitelist(white);
            }
        }

        Map blacklist = (Map) map1.get("blacklist");
        if (blacklist==null||blacklist.size()<=0){
            return;
        }
        Boolean all = (Boolean) blacklist.get("all");
        if (all!=null){
            rceHook.setBlockAll(all);
        }
        String commonPattern = (String) blacklist.get("common_pattern");
        if (commonPattern!=null&&!"".equals(commonPattern)){
            rceHook.setCommonPattern(commonPattern.replace("\\\\","\\"));
        }
        String dnsCmdPattern = (String) blacklist.get("dns_cmd_pattern");
        if (dnsCmdPattern!=null&&!"".equals(dnsCmdPattern)){
            rceHook.setDnsCMDPattern(dnsCmdPattern.replace("\\\\","\\"));
        }
        String dnsDomainPattern = (String) blacklist.get("dns_domain_pattern");
        if (dnsDomainPattern!=null&&!"".equals(dnsDomainPattern)){
            rceHook.setDnsDomainPattern(dnsDomainPattern.replace("\\\\","\\"));
        }
        List<String> commandList = (List<String>) blacklist.get("command");
        if (commandList!=null&&commandList.size()>0){
            for (String command : commandList) {
                rceHook.setCommand(command);
            }
        }
        List<String> keywords = (List<String>) blacklist.get("keywords");
        if (keywords!=null&&keywords.size()>0){
            for (String keyword : keywords) {
                rceHook.setKeywords(keyword);
            }
        }
    }

    private static void parseJNDIINJECT(Map map1, JNDIHook jndiHook) {
        Map<String,List<String>> whitelist = (Map<String, List<String>>) map1.get("whitelist");
        if (whitelist != null&&whitelist.size()>0){
            List<String> whitelistIp = whitelist.get("ip");
            if (whitelistIp != null&&whitelistIp.size()>0){
                for (String ip : whitelistIp) {
                    jndiHook.setWhitelistIp(ip);
                }
            }

            List<String> whitelistDomain = whitelist.get("domain");
            if (whitelistDomain!=null&&whitelistDomain.size()>0){
                for (String domain : whitelistDomain) {
                    jndiHook.setWhitelistDomain(domain);
                }
            }
        }

        Map<String,List<String>> blacklist = (Map<String, List<String>>) map1.get("blacklist");
        if (blacklist != null&&blacklist.size()>0){
            List<String> blacklistProtocol = blacklist.get("protocol");
            if (blacklistProtocol!=null&&blacklistProtocol.size()>0){
                for (String protocol : blacklistProtocol) {
                    jndiHook.setBlacklistProtocol(protocol);
                }
            }

            List<String> blacklistIp = blacklist.get("ip");
            if (blacklistIp!=null&&blacklistIp.size()>0){
                for (String ip : blacklistIp) {
                    jndiHook.setBlacklistIp(ip);
                }
            }

            List<String> blacklistDomain = blacklist.get("domain");
            if (blacklistDomain!=null&&blacklistDomain.size()>0){
                for (String domain : blacklistDomain) {
                    jndiHook.setBlacklistDomain(domain);
                }
            }

        }

        //解决白名单和黑名单冲突
        List<String> blacklistIp = jndiHook.getBlacklistIp();
        List<String> blacklistDomain = jndiHook.getBlacklistDomain();
        List<String> whitelistIp = jndiHook.getWhitelistIp();
        List<String> whitelistDomain = jndiHook.getWhitelistDomain();
        if (blacklistIp != null&&blacklistIp.size()>0){
            if (whitelistIp != null&&whitelistIp.size()>0){
                for (String whiteIp : whitelistIp) {
                    if (blacklistIp.contains(whiteIp)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
            if (whitelistDomain!=null&&whitelistDomain.size()>0){
                for (String whiteDomain : whitelistDomain) {
                    if (blacklistIp.contains(whiteDomain)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
        }
        if (blacklistDomain!=null&&blacklistDomain.size()>0){
            if (whitelistIp != null&&whitelistIp.size()>0){
                for (String whiteIp : whitelistIp) {
                    if (blacklistDomain.contains(whiteIp)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
            if (whitelistDomain!=null&&whitelistDomain.size()>0){
                for (String whiteDomain : whitelistDomain) {
                    if (blacklistDomain.contains(whiteDomain)){
                        //todo：抛出白名单和黑名单冲突异常！！！
                    }
                }
            }
        }


    }

    private static void parseDeserialization(Map map1,DeserializableHook deserializableHook) {

        List<String> list = (List<String>) map1.get("whitelist");
        if (list!=null&&list.size()>0){
            for (String s : list) {
                deserializableHook.setWhitelist(s);
            }
        }

        Map<String,List<String>> map2 = (Map<String, List<String>>) map1.get("blacklist");
        if (map2==null||map2.size()<=0){
            return;
        }

        List<String> list1 = map2.get("json_yaml_class");
        if (list1!=null&&list1.size()>0){
            for (String s : list1) {
                deserializableHook.setJsonYamlClass(s);
            }
        }

        List<String> list2 = map2.get("json_yaml_package");
        if (list2!=null&&list2.size()>0){
            for (String s : list2) {
                deserializableHook.setJsonYamlPackage(s);
            }
        }

        List<String> list3 = map2.get("xml_class");
        if (list3!=null&&list3.size()>0){
            for (String s : list3) {
                deserializableHook.setXmlClass(s);
            }
        }

        List<String> list4 = map2.get("xml_package");
        if (list4!=null&&list4.size()>0){
            for (String s : list4) {
                deserializableHook.setXmlPackage(s);
            }
        }

        List<String> list5 = map2.get("xml_keywords");
        if (list5!=null&&list5.size()>0){
            for (String s : list5) {
                deserializableHook.setXmlKeywords(s);
            }
        }



    }

    private static CommonHook parseCommonHook(Map map1,String type) {
        CommonHook commonHook = null;
        //根据不同类型创建不同Hook类。
        if (RASPInfo.DESERIALIZATION.equals(type)){
            commonHook = new DeserializableHook();
        }else if(RASPInfo.JNDIINJECT.equals(type)){
            commonHook = new JNDIHook();
        }else if(RASPInfo.RCE.equals(type)){
            commonHook = new RCEHook();
        } else if (RASPInfo.SSRP.equals(type)){
            commonHook = new SSRFHook();
        } else if (RASPInfo.FILEOPERATOR.equals(type)) {
            commonHook = new FileOperatorHook();
        } else if (RASPInfo.EXPRESSIONINJECT.equals(type)) {
            commonHook = new ExpressionHook();
        } else if (RASPInfo.SQLI.equals(type)) {
            commonHook = new SQLIHook();
        } else if (RASPInfo.XXE.equals(type)) {
            commonHook = new XXEHook();
        } else{
            return null;
        }
        String mode = (String) map1.get("mode");
        //默认为Close模式
        if (mode!=null && !"".equals(mode)){
            mode = upperCase(mode.toLowerCase());
        }else{
            mode = RASPInfo.CLOSE;
        }
        commonHook.setMode(mode);
        String checkClass= (String) map1.get("check_class");
        commonHook.setCheckClass(checkClass);

        List<Map> list6 = (List<Map>) map1.get("hook_class_and_method");
        if (list6 == null || list6.size()<=0){
            return commonHook;
        }
        for (Map hookClassAndMethodMap : list6) {

            String hookClass = (String) hookClassAndMethodMap.get("hook_class");
            if (hookClass==null||"".equals(hookClass)){
                continue;
            }
            HookClassAndMethod hookClassAndMethod = new HookClassAndMethod();
            hookClassAndMethod.setHookClass(hookClass);

            List<String> hookMethods= (List<String>) hookClassAndMethodMap.get("hook_method");
            if (hookMethods == null||hookMethods.size()<=0){
                continue;
            }
            for (String hookMethod : hookMethods) {
                hookClassAndMethod.setHookMethod(hookMethod);
            }
            commonHook.setHookClassAndMethodList(hookClassAndMethod);
        }
        return commonHook;
    }

    /**
     * 将字符串首字符大写
     * @param str
     * @return
     */
    private static String upperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

}
