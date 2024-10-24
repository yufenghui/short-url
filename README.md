# 短链服务

- 一个在项目初期可使用的短链服务
- TPS要求没那么高，100左右就满足要求
- 支持集群部署


## 接口

| 接口      | 地址           |
|---------|--------------|
| 创建-数据库锁 | gen          |
| 创建-分布式锁 | /genWithLock |
| 短链跳转    | /a/*         |

## 防重复

- 数据库锁： select for update where 空记录会导致表锁，达到类似分布式锁的功能。
- Redis分布式锁：这个肯定不重复了，但性能会差一些

## 缓存

- 短链生成后，存入数据库中，会立即在缓存中存一份
- 查询不存在，从数据库中获取后，会刷新缓存

## 构建

```shell
docker build -t short-url-application:v1.0 .
```

```shell
docker run -it -p 8080:8080 short-url-application:v1.0
```

## 问题

#### 为什么不采用 Bloom Filter？

1. 性能要求没那么高。
2. 不想依赖Redis的持久化功能，有些Redis云服务是不带存储的。

## 短链算法

MurmurHash
