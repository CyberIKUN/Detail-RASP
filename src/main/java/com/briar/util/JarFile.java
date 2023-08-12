package com.briar.util;

import com.briar.Agent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class JarFile {
    /**
     * 拿到Agent Jar包的位置
     */
    public static final String getJarPath(){
        String path=null;
        try {
            //提前将文件路径带有+号的编码为%2B，防止URL解码时直接将+号解码为%20（空格）
            path = URLDecoder.decode(Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile().replace("+","%2B"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return path;
    }
}
