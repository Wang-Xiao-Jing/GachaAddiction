# 自定义抽奖道具定义

## 责任边界

| 入口 | 职责 |
| --- | --- |
| core data/drawitem/GachaDrawItemDefinition | 沿用原 Codec 的数据模型 |
| core data/drawitem/GachaDrawItemDefinitionReloadListener | 发现、解析和校验 JSON，成功后整体发布定义 |
| core data/drawitem/GachaDrawItemDefinitions | 保存当前定义快照，按资源 ID 查询、列举和清理 |
| core world/item/GachaDrawItemFactory | 读取或解析道具绑定，根据定义创建真实 ItemStack |
| core world/item/GachaDrawItem | 注册道具的使用和提示入口 |
| core world/item/GachaItemDraws | 服务端道具抽奖会话与确认、结算 |
| compat compat/kubejs/GachaDrawItemKubeJS | 沿用 define/create 脚本接口，分别接入生成路径与物品工厂 |

定义路径仍是 data/<namespace>/gacha_items/<path>.json。没有新增“每个 JSON 一个物品注册 ID”的注册流程，现有通用载体及其资源包外观方式保留。

## 格式和发布

原有字段保留：card_pool、package、source、item、preview_items。解析继续使用当前 HolderLookup.Provider 创建 RegistryOps，载体必须是 GachaDrawItem，动画必须已在 GachaTypes 注册。

重载先构建局部 parsed 表。全部 JSON 解析、载体检查和动画检查成功后，再以 Map.copyOf 整体替换当前定义。错误发生在发布之前时，不逐条替换旧快照。没有追加新的字段默认值、容错解析或旧格式兼容分支。

## 道具绑定与创建

minecraft:custom_data 内的 gachaaddiction.draw_item 绑定键保留。工厂读取的是绑定 ID；创建时复制定义中的 ItemStack，缺省仍使用 WISH_TOTEM，将数量设为 1，再写入绑定并应用来源设置。

服务端使用道具时仍从当前定义解析 card_pool、package 和 preview_items。未知绑定仍抛出原有错误，不能只信任客户端或物品中缓存的卡池和套餐数据。

脚本 define 仍生成 gacha_items/<path>.json；脚本 create 仍返回真实物品。命令、来源能力和服务端使用入口通过语义成员迁移转到工厂，没有保留旧类的转发包装。

## 本批证据

相关 10 个文件的 IDEA 检查无错误。重载器新文件补齐参数空值约定后再次检查，错误和警告均为空；其他既有物品与脚本入口仍有空值注解、借用世界资源提示及非 Java 调用方法“未使用”等警告。

没有运行 Data、KJS、游戏或资源重载。尚未以执行结果证明资源生成、热重载和玩家使用场景；这些验收仍在完整计划中。

