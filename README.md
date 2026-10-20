# Item Restrict

NeoForge **1.21.1** 服务端模组：给物品添加 tag 限制，只有拥有指定 scoreboard tag 的玩家才能右键使用或交互。

- **只装服务端即可**：客户端不需要安装这个模组。
- 限制保存在原版 `minecraft:custom_data` 中，不注册自定义 DataComponent。
- 支持 `/itemrestrict add/remove/clear/info/check`。
- 拦截右键使用物品、右键方块使用物品、右键实体使用物品。
- 中英文提示，actionbar 提示 + 村民拒绝音效。

## 下载

| 文件 | 说明 |
|---|---|
| [dist/itemrestrict-1.1.0-serveronly.jar](dist/itemrestrict-1.1.0-serveronly.jar) | 服务端成品 |
| [docs/使用说明.md](docs/使用说明.md) | 中文使用说明 |

SHA-256：

```text
335816C809FB65DA3E1354ACC0D21AB3FEDE4B13C89E6EB6A4BAD4B8D7FC961F
```

要求：**Minecraft 1.21.1 + NeoForge 21.1.x + Java 21**。

## 安装

1. 把 `itemrestrict-1.1.0-serveronly.jar` 放进服务端的 `mods` 文件夹。
2. 客户端不需要安装。装了同一个 jar 会额外显示 tooltip；不装不影响进服。
3. 重启服务器。

> 1.0.0 不能只装服务端。它注册了自定义 `itemrestrict:restriction` DataComponent，客户端没装模组会在登录阶段断开。1.1.0 已改为 `minecraft:custom_data`，不再有这个注册表同步问题。

## 使用

所有命令需要权限等级 2，默认作用于主手物品。

```text
/itemrestrict add <tag>       添加一个允许 tag
/itemrestrict remove <tag>    移除一个允许 tag
/itemrestrict clear           清除主手物品的限制
/itemrestrict info            查看主手物品的限制
/itemrestrict check           检查自己能否使用主手物品
```

给玩家加 tag：

```text
/tag <玩家> add <tag>
```

示例：

```text
/tag Steve add vip
/itemrestrict add vip
```

`allowed_tags` 为空表示“谁都不能用”；没有 `itemrestrict:restriction` 表示“不限制”。

## 数据格式

限制保存在物品的 `minecraft:custom_data` 中：

```snbt
{
  "minecraft:custom_data": {
    "itemrestrict:restriction": {
      allowed_tags: ["vip", "admin"],
      deny_message: "你没有权限使用这件物品。"
    }
  }
}
```

`deny_message` 可选，是普通字符串，不是 JSON 文本组件。

## 拦截范围

- 右键空气/直接使用物品：食物、药水、弓、盾、雪球等；
- 右键方块使用物品：锄、铲、桶、打火石、放置方块等；
- 右键实体使用物品：命名牌、拴绳、桶、剪刀等。

**不会**因为主手拿着限制物品就禁止打开箱子/门，因为这属于方块交互，不是“使用主手物品”。如果你希望“拿着限制物品就完全不能和任何方块交互”，需要另外加逻辑。

## 构建

```bash
./gradlew build
```

产物：

```text
build/libs/itemrestrict-1.1.0.jar
```

## 兼容性说明

- 1.1.0 与 1.0.0 的物品数据不兼容。1.0.0 锁过的物品使用自定义组件 `itemrestrict:restriction`，1.1.0 不再注册该组件，旧物品加载时会被判定为 invalid item。
- 如果只是测试，删掉旧测试物品/世界即可。
- 如果正式存档里已经有 1.0.0 锁过的物品，升级前需要先做数据迁移，不要直接替换 jar。

## 目录结构

```text
src/main/java/                 源码
src/main/resources/            lang、pack.mcmeta
dist/                          编译好的 jar
docs/                          中文说明
```

## 许可

MIT，见 [LICENSE](LICENSE)。

本项目是非官方模组，与 Mojang、Microsoft、NeoForged 无隶属关系。见 [NOTICE.md](NOTICE.md)。
