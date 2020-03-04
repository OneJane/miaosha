# 总结
## 项目框架搭建
1. Spring Boot 环境搭建
  DemoController.java
  MainApplication.java
  application.properties
  pom.xml
2. 集成Thymeleaf, Result结果封装
  CodeMsg.java
  Result.java
  application.properties
  hello.html
  pom.xml
3. 集成MyBatis+Druid
  UserDao.java
  UserService.java
  application.properties
  pom.xml
  SampleController.java
4. 集成Jedis+Redis安装+通用缓存Key封装
  RedisConfig.java
  RedisPoolFactory.java
  RedisService.java
  
  BasePrefix.java
  KeyPrefix.java
  UserKey.java
  SampleController.java

## 登录功能
1. 明文密码两次MD5处理（明文+固定salt->用户输入+随机salt）
  MD5Util.java
2. JSR303参数检验+全局异常处理器(接口的异常, 还未写页面异常)
  GlobalException.java
  GlobalExceptionHandler.java
  LoginVo.java
  IsMobile.java
  IsMobileValidator.java
  ValidatorUtil.java
3. 分布式Session
  LoginController.java
  GoodsController.java
  UserArgumentResolver.java
  WebConfig.java
  MiaoshaUserService.java
  UUIDUtil.java
## 秒杀功能
1. 商品列表页 (页面缓存)
2. 商品详情页 (url缓存/前后端分离)
3. 订单详情页 (前后端分离)

## 页面优化技术
1. 页面缓存+URL缓存+对象缓存
2. 页面静态化, 前后端分离
3. 静态资源优化
4. CDN优化(3, 4 未涉及)

## 接口优化
1. Redis预减库存减少数据库访问
2. 内存标记减少Redis访问
3. RabbitMQ队列缓冲, 异步下单
4. Nginx水平扩展(本机设置)

## 安全优化
1. 秒杀接口地址隐藏
2. 接口防刷

# 环境安装
tar zxf redis-4.0.2.tar.gz
mv redis-4.0.2 /usr/local/redis
cd /usr/local/redis/ && make
make install
vim redis.conf
bind 0.0.0.0
daemonize yes
protected-mode yes
requirepass 123456
redis-server redis.conf 
redis-cli
shutdown save
exit
./utils/install_server.sh安装成服务
Please select the redis port for this instance: [6379] 
Selecting default: 6379
Please select the redis config file name [/etc/redis/6379.conf] /usr/local/redis/redis.conf      
Please select the redis log file name [/var/log/redis_6379.log] /usr/local/redis/redis.log
Please select the data directory for this instance [/var/lib/redis/6379] /usr/local/redis/data
Please select the redis executable path [/usr/local/bin/redis-server] 

chkconfig --list | grep redis
systemctl status/start/stop redis_6379
redis-cli
auth 123456

http://127.0.0.1:8888/login/to_login  13000000000/123456 登录测试

# 压测
## 商品列表
测试计划-添加线程组-2000线程0秒内循环10次
线程组-配置元件-添加http请求默认值：http localhost 8888
线程组-sampler-http请求：get /goods/to_list
线程组-监听器-聚合报告，图形结果，用表格查看结果，聚合报告中的Throughput表示qps
执行->linux top 查看 mysql排名靠上
## 用户信息
商品列表-禁用
线程组-sampler-http请求：get /user/info - Parameters 添加 token:57c78d16b6704a9f8f0a357b28b097a6   单个token配置

线程组-配置原件-CSV Data Set Config：																										 多个token配置
    Filename C:\Users\JDD\Desktop\config.txt   内容13000000000,57c78d16b6704a9f8f0a357b28b097a6
	Variable Names(comma-delimited) userId,userToken
	Delimiter(use '\t' for tab) ,
	修改用户信息中参数为 token ${userToken}
执行查看图形结果的qps	

## redis压测
redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -n 100000　开启100个并发共10万个请求
redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100  开启100个字节的快速请求
redis-benchmark -h 127.0.0.1 -p 6379 -q -n 100000 -t set,lpush  指定方法请求
redis-benchmark -h 127.0.0.1 -p 6379 -q -n 100000 script load "redis.call('set','foo','bar')"  指定命令请求

## 命令行压测

``` gherkin
<packaging>war</packaging>
<build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
@SpringBootApplication
public class MainApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MainApplication.class);
    }
}
```
mvn clean package 获得miaosha.war

``` dts
<packaging>jar</packaging>
<build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```
mvn clean package 获得miaosha.jar
nohup java -jar miaosha.jar &
http://10.33.72.81:8888/goods/to_list
配置线程组5000-0-10
UserUtil 生成tokens.txt
配置CSV Data Set Config 
Filename C:\Users\JDD\Desktop\tokens.txt
Variable Names(comma-delimited) userId,userToken
Delimiter(use '\t' for tab)

配置商品秒杀HTTP请求
goodsId 1
token ${userToken}
上传miaosha.jmx时修改miaosha.jmx中路径
<stringProp name="filename">C:\Users\JDD\Desktop\tokens.txt</stringProp>
<stringProp name="filename">C:\Users\JDD\Desktop\result.jtl</stringProp> 
sh apache-jmeter-3.2/bin/jmeter.sh -n -t miaosha.jmx -l result.jtl 打开聚合报告-浏览-result.jtl
