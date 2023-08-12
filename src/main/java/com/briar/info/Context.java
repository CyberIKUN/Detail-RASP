package com.briar.info;


import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    private int id;
    private String method;
    private String protocol;
    private String sourceIp;
    private String url;
    private String uri;
    private String hostname;
    private String port;
    private String contentType;
    private Integer length;
    private Map<String,String> header;
    private Map<String,String[]> urlArgs;
    //这个字段不保存到数据库中
    private Object request;
    //这个字段不保存到数据库中
    private Object response;
    //请求体
    private String body;
    //这个字段不保存到数据库中
    private ByteArrayOutputStream byteArrayOutputStream;
    private String destinationIp;
    private String queryString;

    //这个字段不保存到数据库中
    private Object inputStream;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public Object getInputStream() {
        return inputStream;
    }



    public void setInputStream(Object inputStream) {
        this.inputStream = inputStream;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Context() {
        this.header = new HashMap<>();
        this.urlArgs = new HashMap<>();
        this.byteArrayOutputStream=new ByteArrayOutputStream();
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }



    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Map<String, String[]> getUrlArgs() {
        return urlArgs;
    }

    public void setUrlArgs(Map<String, String[]> urlArgs) {
        this.urlArgs = urlArgs;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getBody() {
        try {
            this.body=this.byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return body;
    }


    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }
}
