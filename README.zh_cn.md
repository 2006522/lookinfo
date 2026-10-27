# 方块信息 (LookInfo)

准星指到哪，就在屏幕顶部显示那块东西的信息。一个轻量的替代品 —— 用在天还没跟上的快照上
（比如现在的 Jade）。

指向方块：

- 方块名（跟随语言设置）
- 来自哪个模组——只对模组方块显示，原版方块不占一行
- 硬度（不可破坏的显示成红色）
- 需要正确工具才能掉落时会标出来
- 容器格数

指向生物：

- 名字
- 来自哪个模组（只对模组生物显示）
- 生命 `当前 / 最大`，低于 30% 变红
- 有护甲时显示护甲值

---

## 环境要求

| | |
| --- | --- |
| Minecraft | 26.4-snapshot-5 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 必须 |

**纯客户端**，不向服务端发任何东西，可以带着上原版服务器。

## 安装

1. 装好 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)；
2. 把 `LookInfo-<版本>.jar` 放进 `mods/`；
3. 启动游戏。

> 同一个 mod id 只能有一份 jar，升级前先删旧的。

## 用法

指哪看哪。按 `I` 开关面板（可在「选项 → 控制 → 按键绑定」里改）。

## 配置

`config/lookinfo.properties`，按需关掉某几项：

```properties
# 总开关（I 键会写回这里）
enable=true

# 面板离屏幕顶边的距离（GUI 像素）
offset_y=6

# 显示方块/生物来自哪个模组
show_source=true

# 方块硬度
show_hardness=true

# 「需要正确工具」提示
show_tool=true

# 容器格数
show_container=true

# 生物血量 / 护甲
show_health=true
```

## 已知限制

- **看不到容器里装了什么。** 原版只在打开界面时才把容器内容同步给客户端，不打开的时候客户端
  是真的不知道里面有什么。不加服务端来回的话，只能拿到格数。
- 只显示准星当前指着的那一个，不做掉落物预测、不读 NBT。
- 硬度和挖掘时间一致：黑曜石那一档显示 50，基岩那一档显示「不可破坏」。

## 为什么不用 Jade

Jade 26.3 在 26.4-snapshot-5 上跑不起来，而且不是打补丁能解决的：游戏把 `Mth.clamp` 删了、
`AbstractTexture` 换成了 `TextureHandle`、`CycleButton` 重构了、`Renderable#extractRenderState`
少了鼠标坐标参数——这些都在 Jade 的渲染核心里，等于要重写一遍。这个 mod 先顶上常用需求。

## 许可证

[MIT](LICENSE)
