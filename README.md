# 总结
## 项目框架搭建
1. Spring Boot 环境搭建
2. 集成Thymeleaf, Result结果封装
3. 集成MyBatis+Druid
4. 集成Jedis+Redis安装+通用缓存Key封装

## 登录功能
1. 明文密码两次MD5处理（明文+固定salt->用户输入+随机salt）
2. JSR303参数检验+全局异常处理器(接口的异常, 还未写页面异常)
3. 分布式Session

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