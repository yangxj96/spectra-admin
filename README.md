# 光谱

# 0 为啥搞这个

最近多年工作中,基本还是用不到微服务等高端的技术

但是单体的后端SpringBoot加上前端VUE框架的前后端分离的项目

倒是一直再用.为了简化开发,也简化工作的重复内容.

所以想要做一个比较通用的前后端都包含的框架

[前端项目](https://github.com/yangxj96/spectra-web)

# 1 技术选型

技术架构:

> 基本都是选取最新版本整合,版本会一直跟着框架版本更新

- 🆕️  `JavaJDK 21`
- 🆕️  `Maven 3.9.9`
- 🆕️  `PostgreSQL 17`
- 🆕️  `spring-boot  3.5.3`
- 🆕️  `mybatis-plus 3.5.12`
- 🆕️  `sa-token 1.44.0`
- 🆕️  `mapstruct 1.6.3`
- ...

# 2 IDEA环境`JVM`参数配置

> tips: 主要目的减少web服务的的内存占用

`JVM`参数: `-Xms256m -Xmx256m -Xmn100m`

# 3 env文件说明

> 默认不上传.env文件,请在spectra-api文件夹下自建.env文件

| 环境变量                      | 说明         |
|---------------------------|------------|
| JASYPT_ENCRYPTOR_PASSWORD | jasypt加密密码 |
