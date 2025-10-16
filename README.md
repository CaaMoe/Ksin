<div align="center">

# Ksin

![GitHub commit activity](https://img.shields.io/github/commit-activity/t/CaaMoe/Ksin)
[![GitHub license](https://img.shields.io/github/license/CaaMoe/Ksin?style=flat-square)](https://github.com/CaaMoe/Ksin/blob/master/LICENSE)
[![QQ Group](https://img.shields.io/badge/QQ%20group-832210691-yellow?style=flat-square)](https://jq.qq.com/?_wv=1027&k=WrOTGIC7)
[![Join our Discord](https://img.shields.io/discord/1225725211727499347.svg?logo=discord&label=)](https://discord.gg/9vh4kZRFCj)
[![bStats](https://img.shields.io/bstats/servers/26924?color=brightgreen&label=bStats&logo=bs&style=flat-square)](https://bstats.org/plugin/velocity/ksin/26924)

</div>

## 概述

Ksin 是一个专为 Minecraft Velocity 代理服务器设计的插件, 功能是修复签名受损的皮肤, 并提供其他额外的皮肤管理功能(如果有).

## 使用场景

- 你的服务器使用了多外置登录, 但这些外置服务器生成的皮肤的签名不被 Minecraft 官方认可, 导致不同外置玩家的客户端无法正确加载皮肤.

## 安装

1. 确保你的服务器是 Velocity 类型代理端
2. 确保服务器运行 Java 21 或更高版本
3. 从 [Actions](https://github.com/CaaMoe/Ksin/actions) 页面下载最新构建版本的 Ksin.jar(如果没有的话可以参考下方的构建步骤自己构建)
4. 将下载的 JAR 文件放入 Velocity 服务器的 `plugins` 文件夹中
5. 重启 Velocity 服务器

## 配置

插件启动后会在 `plugins/ksin` 目录下生成默认配置文件 `config.conf`，你可以根据需要进行修改

## 构建

1. 克隆这个项目
2. 执行 `./gradlew build`
3. 在 `build/libs` 下寻找你需要的

或者你也可以

1. [Fork](https://github.com/CaaMoe/Ksin/fork) 此项目
2. 开启 Actions
3. 随便提交一个文件

## 贡献指南

欢迎通过以下方式为 Ksin 插件贡献力量：

1. 提交 bug 报告或功能建议到 [Issues](https://github.com/CaaMoe/Ksin/issues)
2. 提交代码改进通过 [Pull Requests](https://github.com/CaaMoe/Ksin/pulls)

***

如果本插件对你有帮助，请考虑给我们一个 ⭐️，这将是对我们最大的鼓励！

## 贡献者

<a href="https://github.com/CaaMoe/Ksin/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CaaMoe/Ksin"  alt="作者头像"/>
</a>

[我也想为贡献者之一？](https://github.com/CaaMoe/Ksin/pulls)
