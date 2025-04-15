# 后端模板构建
1. sql/create_table.sql 定义了数据库的初始化建库建表语句
2. sql/post_es_mapping.json 帖子表在 ES 中的建表语句
3. aop：用于全局权限校验、全局日志记录
4. common：万用的类，比如通用响应类
5. config：用于接收 application.yml 中的参数，初始化一些客户端的配置类（比如对象存储客户端）
6. constant：定义常量
7. controller：接受请求
8. esdao：类似 mybatis 的 mapper，用于操作 ES
9. exception：异常处理相关
10. job：任务相关（定时任务、单次任务）
11. manager：服务层（一般是定义一些公用的服务、对接第三方 API 等）
12. mapper：mybatis 的数据访问层，用于操作数据库
13. model：数据模型、实体类、包装类、枚举值
14. service：服务层，用于编写业务逻辑
15. utils：工具类，各种各样公用的方法
16. wxmp：公众号相关的包
17. test：单元测试
18. MainApplication：项目启动入口
19. Dockerfile：用于构建 Docker 镜像


redis-cli -h awentest.redis.rds.aliyuncs.com -p 6379 -a Zheng55828784

# 获取所有键（不推荐在生产环境大量数据时使用）
KEYS *