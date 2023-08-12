# Detail-RASP
一款开源RASP项目，仅供学习参考！

可以先git clone代码，慢慢看

### 简单使用

获取要hook的jvm进程

```
java -jar Detail-RASP.jar
```

hook进程

```
java -jar Detail-RASP.jar 1444
```

设计为4个模块：

### 信息收集模块

收集目标web应用相关信息：

* 系统信息（Linux、Windows、Mac）
* 架构（x86、ARM）
* jdk版本（主版本、次版本）
* 服务器信息（Tomcat、JBoss、Jetty、Wildfly、Resin、WebLogic、WebSphere）、版本
* 框架（SpringBoot or Struts）
* 数据库信息（MySQL、Oracle、MSSQL、Redis、SQLite3、PostgreSQL、DB2、HSQLDB (WebGoat 使用的嵌入数据库)，可能连接多个数据库）

### hook点管理模块

hook点和对应的写入配置文件中

![image-20230205124748911](https://raw.githubusercontent.com/CyberIKUN/picture/main/img/202302051247270.png)

配置文件格式：

- type：漏洞类型
- mode：检测模式分为三种：log（不拦截攻击，仅记录日志）、block（攻击拦截且记录日志）、close（不记录日志、不拦截攻击）
- hook_class_and_method：
  - hook_class：需要hook的类
  - hook_class：对应方法的描述
- whitelist：白名单
- blacklist：黑名单
- check_class：检测是否存在漏洞的类

同一种漏洞类型可能hook多个类，比如反序列化包括jdk反序列化、fastjson、snakeyaml、xml

hook之后进行字节码转换

负责根据不同的漏洞类型进行对应类的Hook，漏洞类型包括：

- [x] SSRF
- [x] 命令执行（包括反射调用native方法）

在JDK9的时候把UNIXProcess合并到了ProcessImpl当中

- [x] SQL注入（MySQL）
- [x] 文件操作（文件读取/下载、文件写入/上传）

文件读取：

1. 防止路径穿越；（../或..\）
2. 防止读取敏感文件；（/etc/passwd，/etc/shadow，/etc/sysconfig/network-scripts/ifcfg-eth0，/etc/hosts，/root/.bash_history）
3. 防止读取源码文件；（.war或者.jar）

文件删除：

防止路径穿越；（../或..\）

文件上传：

1. 检查后缀

.list：

列出目录下的所有东西，防止list敏感目录，做黑名单匹配

- [x] 表达式注入（Spel、Ognl）

  黑名单+长度限制

- [x] 反序列化（jdk反序列化、fastjson、jackson、xstream、snakeyaml）

- [x] JNDI注入

- [x] XXE（主动防御，直接设置所有属性不引用外部DTD）

这里将**字节码转换模块**纳入Hook点管理模块

对不同的类进行Hook后调用字节码转换模块进行字节码插入。

### 检测模块

插入的字节码调用检测模块进行检测。达到尽量不在插入的字节码中进行检测的目的，减少插入字节码量，提高性能。

### 攻击日志采集模块

context：包含一次请求的所有信息，在请求结束后清空

请求到来时，清空context内容；

包括：

- method：请求方法
- protocol：请求协议
- sourceIp：攻击者ip
- url：访问url
- url：访问uri
- hostname：受害主机名
- port：端口
- contentType：攻击者的操作系统类型
- length：请求长度
- header：请求头
- urlArgs：url参数键值对
- queryString：url参数
- body：请求体内容
- request：请求对象
- response：响应对象
- byteArrayOutputStream：保存读取的流数据
- inputStream：标记作用
- destinationIp：目的Ip

数据格式：

- type：攻击类型
- stack：调用堆栈
- isBlock：是否拦截
- severity：严重等级（Low、Medium、High）
- timestamp：攻击时间
- context：请求完整信息
- payload：攻击载荷

存储到数据库
