package com.briar.check;

import com.briar.constant.RASPInfo;
import com.briar.hook.ExpressionHook;
import com.briar.hook.XXEHook;
import com.briar.util.HookUtil;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XXECheck {

    private static final String FEATURE_DEFAULTS_1 = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String FEATURE_DEFAULTS_2 = "http://xml.org/sax/features/external-general-entities";
    private static final String FEATURE_DEFAULTS_3 = "http://xml.org/sax/features/external-parameter-entities";
    private static final String FEATURE_DEFAULTS_4 = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static void updateConfig(Object o){
        XXEHook xxeHook = HookUtil.getXXEHook();
        if (o==null || xxeHook==null ){
            return;
        }
        String mode = xxeHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(o.getClass().getName());
            Method setFeature = aClass.getDeclaredMethod("setFeature",new Class[]{String.class,boolean.class});
            setFeature.setAccessible(true);
            setFeature.invoke(o,new Object[]{FEATURE_DEFAULTS_1,true});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateConfigForJavaXML(Object o){
        XXEHook xxeHook = HookUtil.getXXEHook();
        if (o==null || xxeHook==null ){
            return;
        }
        String mode = xxeHook.getMode();
        if ( mode==null || "".equals(mode)|| RASPInfo.CLOSE.equals(mode)){
            return;
        }
        if ("javax.xml.parsers.DocumentBuilderFactory".equals(o.getClass().getName())){
            DocumentBuilderFactory documentBuilderFactory = (DocumentBuilderFactory) o;
            try {
                documentBuilderFactory.setFeature(FEATURE_DEFAULTS_2,false);
                documentBuilderFactory.setFeature(FEATURE_DEFAULTS_3,false);
                documentBuilderFactory.setFeature(FEATURE_DEFAULTS_4,false);
                documentBuilderFactory.setXIncludeAware(false);
                documentBuilderFactory.setExpandEntityReferences(false);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        } else if ("javax.xml.stream.XMLInputFactory".equals(o.getClass().getName())) {
            XMLInputFactory xmlInputFactory = (XMLInputFactory) o;
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        } else if ("org.xml.sax.helpers.XMLReaderFactory".equals(o.getClass().getName())) {
            try {
                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(o.getClass().getName());
                Method setFeature = aClass.getDeclaredMethod("setFeature",new Class[]{String.class,boolean.class});
                setFeature.setAccessible(true);
                setFeature.invoke(o,new Object[]{FEATURE_DEFAULTS_1,true});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return;
        }

    }
}
