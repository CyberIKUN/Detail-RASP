package com.briar.info;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpInfo {

    public static void storeRequestInfo(Object exchange){
        try {

            WebInformation.getInstance().context.set(new Context());
            Context context = WebInformation.getInstance().context.get();

            context.setRequest(exchange);
            Class<?> exchangClass = Thread.currentThread().getContextClassLoader().loadClass(exchange.getClass().getName());
            InetSocketAddress destinationAddress = (InetSocketAddress) getReturnObj(exchangClass,"getDestinationAddress",exchange,null,null);


            String  destinationIp= destinationAddress.getAddress().getHostAddress();
            String hostName = destinationAddress.getHostName();
            int port = destinationAddress.getPort();
            context.setDestinationIp(destinationIp);
            context.setHostname(hostName);
            context.setPort(String.valueOf(port));
            context.setResponse(getReturnObj(exchangClass,"getResponseSender",exchange,null,null));
            Object requestMethod = getReturnObj(exchangClass,"getRequestMethod",exchange,null,null);
            Class<?> httpStringClass = Thread.currentThread().getContextClassLoader().loadClass(requestMethod.getClass().getName());
            String method = (String) getReturnObj(httpStringClass,"toString",requestMethod,null,null);
            context.setMethod(method);

            String url=(String) getReturnObj(exchangClass,"getRequestURL",exchange,null,null);
            context.setUrl(url);

            String uri=(String) getReturnObj(exchangClass,"getRequestURI",exchange,null,null);
            context.setUri(uri);

            context.setLength((int)getReturnObj(exchangClass,"getRequestContentLength",exchange,null,null));

            Object httpString=getReturnObj(exchangClass,"getProtocol",exchange,null,null);
            String protocol = (String) getReturnObj(httpStringClass,"toString",httpString,null,null);
            context.setProtocol(protocol);

            InetSocketAddress sourceAddress = (InetSocketAddress) getReturnObj(exchangClass, "getSourceAddress", exchange,null,null);
            String sourceIp = sourceAddress.getAddress().getHostAddress();
            context.setSourceIp(sourceIp);

            String queryString = (String) getReturnObj(exchangClass, "getQueryString", exchange,null,null);
            context.setQueryString(queryString);

            HashMap<String, String> headers = new HashMap<>(32);
            Object headersMap = getReturnObj(exchangClass,"getRequestHeaders",exchange,null,null);
            if (headersMap!=null){
                Class<?> headersMapClass = Thread.currentThread().getContextClassLoader().loadClass(headersMap.getClass().getName());
                Object iterator = getReturnObj(headersMapClass, "iterator", headersMap,null,null);
                Class<?> iteratorClass = Thread.currentThread().getContextClassLoader().loadClass(iterator.getClass().getName());
                int i=0;
                Class<?> nextClass=null;
                while ((boolean) getReturnObj(iteratorClass,"hasNext",iterator,null,null)){
                    Object next = getReturnObj(iteratorClass,"next",iterator,null,null);
                    if (i==0){
                        nextClass = Thread.currentThread().getContextClassLoader().loadClass(next.getClass().getName());
                        i++;
                    }
                    Object headerNamehttpString = getReturnObj(nextClass,"getHeaderName",next,null,null);
                    String key = (String) getReturnObj(httpStringClass,"toString",headerNamehttpString,null,null);
                    String value=toString((String[]) getReturnObj(nextClass,"toArray",next,null,null));
                    headers.put(key,value);
                }
            }
            context.setHeader(headers);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void storeParameter(Object object){
        if (object==null){
            return;
        }
        Map<String, String[]> storeParameters = new HashMap<>();
        Map<String, Deque<String>> queryParameters = (Map<String, Deque<String>>)object;
        // 类型转换  Deque<String> =====> String[]
        for (Map.Entry<String, Deque<String>> entry : queryParameters.entrySet()) {
            storeParameters.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        Context context = WebInformation.getInstance().context.get();
        if (context!=null){
            context.setUrlArgs(storeParameters);
        }
    }
    public static void storeJettyRequestInfo(Object httpChannel){
        WebInformation.getInstance().context.set(new Context());
        Context context = WebInformation.getInstance().context.get();
        if (httpChannel==null){
            return;
        }
        try {
            Class<?> httpChannelClass = Thread.currentThread().getContextClassLoader().loadClass(httpChannel.getClass().getName());
            Object request= getReturnObj(httpChannelClass,"getRequest",httpChannel,null,null);
            context.setResponse(getReturnObj(httpChannelClass,"getResponse",httpChannel,null,null));
            Class<?> requestClass = Thread.currentThread().getContextClassLoader().loadClass(request.getClass().getName());
            context.setRequest(request);
            context.setHostname(String.valueOf((int) getReturnObj(requestClass,"getLocalPort",request,null,null)));
            context.setDestinationIp((String) getReturnObj(requestClass,"getLocalAddr",request,null,null));
            String method = (String) getReturnObj(requestClass,"getMethod",request,null,null);
            context.setMethod(method);
            context.setLength((int)getReturnObj(requestClass,"getContentLength",request,null,null));
            String contentType=(String) getReturnObj(requestClass,"getContentType",request,null,null);
            context.setContentType(contentType);
            context.setQueryString((String) getReturnObj(requestClass,"getQueryString",request,null,null));
            context.setProtocol((String) getReturnObj(requestClass,"getProtocol",request,null,null));
            context.setSourceIp((String) getReturnObj(requestClass,"getRemoteHost",request,null,null));
            context.setUrl(((StringBuffer)getReturnObj(requestClass,"getRequestURL",request,null,null)).toString());
            context.setUri((String) getReturnObj(requestClass,"getRequestURI",request,null,null));

            if ("get".equalsIgnoreCase(method)||(contentType!=null&&contentType.contains("application/x-www-form-urlencoded"))){
                context.setUrlArgs((Map<String, String[]>) getReturnObj(requestClass,"getParameterMap",request,null,null));
            }
            // 请求header
            Map<String, String> header = new HashMap<>();
            Enumeration<String> headerNames = (Enumeration<String>) getReturnObj(requestClass,"getHeaderNames",request,null,null);
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String key = headerNames.nextElement();
                    String value = (String)getReturnObj(requestClass,"getHeader",request,new Class[]{String.class},new Object[]{key});
                    header.put(key.toLowerCase(), value);
                }
            }
            context.setHeader(header);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void storeTomcatRequestInfo(Object request){
        WebInformation.getInstance().context.set(new Context());
        Context context = WebInformation.getInstance().context.get();
        context.setRequest(request);
        try {
            Class<?> requestClass = Thread.currentThread().getContextClassLoader().loadClass(request.getClass().getName());
            context.setResponse(getReturnObj(requestClass,"getResponse",request,null,null));
            String destinationIp = (String) getReturnObj(requestClass,"getLocalAddr",request,null,null);
            context.setDestinationIp(destinationIp);
            Object host = getReturnObj(requestClass,"getHost",request,null,null);
            Class<?> hostClass = Thread.currentThread().getContextClassLoader().loadClass(host.getClass().getName());
            String hostName=(String) getReturnObj(hostClass,"getName",host,null,null);
            context.setHostname(hostName);

            String port = String.valueOf((int)getReturnObj(requestClass,"getLocalPort",request,null,null));
            context.setPort(port);

            String method=(String) getReturnObj(requestClass,"getMethod",request,null,null);
            context.setMethod(method);

            int length=(int) getReturnObj(requestClass,"getContentLength",request,null,null);
            context.setLength(length);

            String contentType = (String) getReturnObj(requestClass,"getContentType",request,null,null);
            context.setContentType(contentType);

            String queryString=(String) getReturnObj(requestClass,"getQueryString",request,null,null);
            context.setQueryString(queryString);

            String protocol = (String) getReturnObj(requestClass,"getProtocol",request,null,null);
            context.setProtocol(protocol);

            String sourceIp = (String) getReturnObj(requestClass,"getRemoteHost",request,null,null);
            context.setSourceIp(sourceIp);

            String url = ((StringBuffer)getReturnObj(requestClass,"getRequestURL",request,null,null)).toString();
            context.setUrl(url);

            String uri = (String) getReturnObj(requestClass,"getRequestURI",request,null,null);
            context.setUri(uri);


            if ("get".equalsIgnoreCase(method)||(contentType!=null&&contentType.contains("application/x-www-form-urlencoded"))){
                Map<String,String[]> urlArgs=(Map<String, String[]>) getReturnObj(requestClass,"getParameterMap",request,null,null);
                context.setUrlArgs(urlArgs);
            }

            // 请求header
            Map<String, String> header = new HashMap<>();
            Enumeration<String> headerNames = (Enumeration<String>) getReturnObj(requestClass,"getHeaderNames",request,null,null);
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String key = headerNames.nextElement();
                    String value = (String) getReturnObj(requestClass,"getHeader",request,new Class[]{String.class},new Object[]{key});
                    header.put(key.toLowerCase(), value);
                }
            }
            context.setHeader(header);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void inputToOutput(Integer length,Object inputStream,byte[] buffer,Integer offset){
        Context context = WebInformation.getInstance().context.get();
        if (length != -1 && context != null) {
            //当循环读写时，用inputStream做标记，防止覆盖。
            if (context.getInputStream() == null) {
                context.setInputStream(inputStream);
            }
            if (context.getInputStream() == inputStream) {
                context.getByteArrayOutputStream().write(buffer,offset,length);
            }
        }
    }
    public static void inputToOutput(int length,Object inputStream){
        Context context = WebInformation.getInstance().context.get();
        if (length != -1 && context != null) {
            if (context.getInputStream() == null) {
                context.setInputStream(inputStream);
            }
            if (context.getInputStream() == inputStream) {
                context.getByteArrayOutputStream().write(length);
            }
        }
    }
    private static String toString(String[] arrays) {
        final StringBuilder buf = new StringBuilder(64);
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            if (arrays[i] != null) {
                buf.append(arrays[i]);
            }
        }
        return buf.toString();
    }
    private static Object getReturnObj(Class clazz,String methodName,Object obj,Class[] argsClass,Object[] args){
        if (clazz==null||methodName==null||"".equals(methodName)||obj==null){
            return null;
        }
        if (argsClass!=null&&argsClass.length>0&&args!=null&&args.length>0&&argsClass.length==args.length){
            try {
                Method method = clazz.getDeclaredMethod(methodName,argsClass);
                method.setAccessible(true);
                return method.invoke(obj,args);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (argsClass==null&&args==null) {
            try {
                Method method = clazz.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(obj);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
