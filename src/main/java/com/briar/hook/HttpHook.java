package com.briar.hook;

import com.briar.visitor.HttpVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.util.LinkedList;

public class HttpHook extends CommonHook{
    public HttpHook() {
        super.type = "HttpInformation";
        super.hookClassAndMethodList = new LinkedList();
        HookClassAndMethod hookClassAndMethod = new HookClassAndMethod();
        hookClassAndMethod.setHookClass("io/undertow/servlet/handlers/ServletInitialHandler");
        hookClassAndMethod.setHookMethod("dispatchRequest(Lio/undertow/server/HttpServerExchange;Lio/undertow/servlet/handlers/ServletRequestContext;Lio/undertow/servlet/handlers/ServletChain;Ljavax/servlet/DispatcherType;)V");
        hookClassAndMethod.setHookMethod("handleFirstRequest(Lio/undertow/server/HttpServerExchange;Lio/undertow/servlet/handlers/ServletRequestContext;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod);
        HookClassAndMethod hookClassAndMethod1 = new HookClassAndMethod();
        hookClassAndMethod1.setHookClass("io/undertow/servlet/spec/ServletInputStreamImpl");
        hookClassAndMethod1.setHookMethod("read([BII)I");
        super.hookClassAndMethodList.add(hookClassAndMethod1);
        HookClassAndMethod hookClassAndMethod2 = new HookClassAndMethod();
        hookClassAndMethod2.setHookClass("io/undertow/server/HttpServerExchange");
        hookClassAndMethod2.setHookMethod("getQueryParameters()Ljava/util/Map;");
        super.hookClassAndMethodList.add(hookClassAndMethod2);
        HookClassAndMethod hookClassAndMethod3 = new HookClassAndMethod();
        hookClassAndMethod3.setHookClass("org/apache/catalina/connector/CoyoteAdapter");
        hookClassAndMethod3.setHookMethod("service(Lorg/apache/coyote/Request;Lorg/apache/coyote/Response;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod3);
        HookClassAndMethod hookClassAndMethod4 = new HookClassAndMethod();
        hookClassAndMethod4.setHookClass("org/apache/catalina/core/StandardWrapperValve");
        hookClassAndMethod4.setHookMethod("invoke(Lorg/apache/catalina/connector/Request;Lorg/apache/catalina/connector/Response;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod4);
        HookClassAndMethod hookClassAndMethod5 = new HookClassAndMethod();
        hookClassAndMethod5.setHookClass("org/apache/catalina/connector/InputBuffer");
        hookClassAndMethod5.setHookMethod("read([BII)I");
        hookClassAndMethod5.setHookMethod("readByte()I");
        super.hookClassAndMethodList.add(hookClassAndMethod5);
        HookClassAndMethod hookClassAndMethod6 = new HookClassAndMethod();
        hookClassAndMethod6.setHookClass("org/eclipse/jetty/server/Server");
        hookClassAndMethod6.setHookMethod("handle(Lorg/eclipse/jetty/server/HttpChannel;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod6);
        HookClassAndMethod hookClassAndMethod7 = new HookClassAndMethod();
        hookClassAndMethod7.setHookClass("org/eclipse/jetty/server/HttpInput");
        hookClassAndMethod7.setHookMethod("read([BII)I");
        super.hookClassAndMethodList.add(hookClassAndMethod7);
        HookClassAndMethod hookClassAndMethod8 = new HookClassAndMethod();
        hookClassAndMethod8.setHookClass("org/eclipse/jetty/server/Server");
        hookClassAndMethod8.setHookMethod("handle(Lorg/eclipse/jetty/server/AbstractHttpConnection;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod8);
        HookClassAndMethod hookClassAndMethod9 = new HookClassAndMethod();
        hookClassAndMethod9.setHookClass("org/sparkproject/jetty/server/Server");
        hookClassAndMethod9.setHookMethod("handle(Lorg/sparkproject/jetty/server/HttpChannel;)V");
        super.hookClassAndMethodList.add(hookClassAndMethod9);
        HookClassAndMethod hookClassAndMethod10 = new HookClassAndMethod();
        hookClassAndMethod10.setHookClass("org/sparkproject/jetty/server/HttpInput");
        hookClassAndMethod10.setHookMethod("read([BII)I");
        super.hookClassAndMethodList.add(hookClassAndMethod10);
        super.checkClass = "com/briar/info/HttpInfo";
    }

    protected byte[] insertBeforeForType(String methodAndDesc, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, 1);
        ClassVisitor visitor = new HttpVisitor(327680, writer, methodAndDesc, super.checkClass, reader.getClassName());
        reader.accept(visitor, 8);
        return writer.toByteArray();
    }
}
