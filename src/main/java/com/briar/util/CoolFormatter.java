package com.briar.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CoolFormatter extends Formatter {
    public CoolFormatter() {
    }

    public String format(LogRecord record) {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " [" + Thread.currentThread().getName() + "] Thread-ID:" + record.getThreadID() + " " + record.getSourceClassName() + "." + record.getSourceMethodName() + "() " + record.getLevel() + ": " + record.getMessage() + "\n";
    }
}
