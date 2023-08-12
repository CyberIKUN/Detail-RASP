package com.briar.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    private static String logFilePath=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".log";

    public static Logger getLogger(String className) throws IOException {
        // 获取日志记录器对象
        Logger logger = Logger.getLogger(className);

        // 关闭默认配置，即不使用父Logger的Handlers
        logger.setUseParentHandlers(false);

        // 设置记录器的日志级别为ALL
        logger.setLevel(Level.ALL);

        // 日志记录格式，使用简单格式转换对象
        CoolFormatter coolFormatter = new CoolFormatter();

        // 控制台输出Handler,并且设置日志级别为INFO，日志记录格式
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(coolFormatter);

        // 文件输出Handler,并且设置日志级别为FINE，日志记录格式
        FileHandler fileHandler = new FileHandler(logFilePath,0,1,true);
        fileHandler.setLevel(Level.INFO);
        fileHandler.setFormatter(coolFormatter);



        // 记录器关联处理器，即此logger对象的日志信息输出到这两个Handler进行处理
        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);


        return logger;
    }
}
