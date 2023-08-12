package com.briar.util;

import com.briar.info.AttackInfo;
import com.briar.info.Context;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    public JsonUtil() {
    }

    public static String toJson(AttackInfo attackInfo) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name("attack_type").value(attackInfo.getAttackType());
            jsonWriter.name("context");
            jsonWriter.beginObject();
            Context context = attackInfo.getContext();
            jsonWriter.name("protocol").value(context.getProtocol());
            jsonWriter.name("source_ip").value(context.getSourceIp());
            jsonWriter.name("url").value(context.getUrl());
            jsonWriter.name("uri").value(context.getUri());
            jsonWriter.name("hostname").value(context.getHostname());
            jsonWriter.name("port").value(context.getPort());
            jsonWriter.name("content_type").value(context.getContentType());
            jsonWriter.name("length").value(context.getLength());
            jsonWriter.name("header");
            jsonWriter.beginObject();
            Map<String, String> header = context.getHeader();
            Iterator var5 = header.entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry<String, String> headerEntry = (Map.Entry)var5.next();
                jsonWriter.name((String)headerEntry.getKey()).value((String)headerEntry.getValue());
            }

            jsonWriter.endObject();
            jsonWriter.name("urlArgs");
            jsonWriter.beginObject();
            Map<String, String[]> urlArgs = context.getUrlArgs();
            Iterator var14 = urlArgs.entrySet().iterator();

            while(var14.hasNext()) {
                Map.Entry<String, String[]> urlArgsEntry = (Map.Entry)var14.next();
                jsonWriter.name((String)urlArgsEntry.getKey());
                jsonWriter.beginArray();
                String[] var8 = (String[])urlArgsEntry.getValue();
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    String value = var8[var10];
                    jsonWriter.value(value);
                }

                jsonWriter.endArray();
            }

            jsonWriter.endObject();
            jsonWriter.name("body").value(context.getBody());
            jsonWriter.name("destination_ip").value(context.getDestinationIp());
            jsonWriter.name("queryString").value(context.getQueryString());
            jsonWriter.endObject();
            jsonWriter.name("is_block").value(attackInfo.isBlock());
            jsonWriter.name("severity").value(attackInfo.getSeverity());
            jsonWriter.name("attack_time").value(attackInfo.getAttackTime());
            jsonWriter.name("payload").value(attackInfo.getPayload());
            jsonWriter.name("stack");
            jsonWriter.beginArray();
            List<String> stack = attackInfo.getStack();
            Iterator var16 = stack.iterator();

            while(var16.hasNext()) {
                String line = (String)var16.next();
                jsonWriter.value(line);
            }

            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
            return out.toString("UTF-8");
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }
    }
}
