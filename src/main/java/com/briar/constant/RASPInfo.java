package com.briar.constant;

/**
 * 包括一些RASP常量：
 * - 漏洞类型
 * - RASP状态
 */
public class RASPInfo {
    public static final String DESERIALIZATION = "Deserialization";
    public static final String RCE= "RemoteCodeExecute";
    public static final String SSRP = "SSRF";
    public static final String EXPRESSIONINJECT="ExpressionInject";
    public static final String FILEOPERATOR="FileOperator";
    public static final String JNDIINJECT="JNDIInject";
    public static final String XXE = "XXE";
    public static final String SQLI = "SQLInject";
    public static final String HTTP_INFO = "HttpInformation";

    //RASP状态
    public static final String LOG = "Log";
    public static final String BLOCK = "Block";
    public static final String CLOSE= "Close";

    //漏洞严重程度
    public static final String SEVERITY_HIGH="high";
    public static final String SEVERITY_MEDIUM="medium";
    public static final String SEVERITY_LOW="low";
}
