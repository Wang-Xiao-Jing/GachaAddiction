# 多选一与多连抽测试数据包

适用于当前Minecraft 1.21.1工程。本包的卡池与奖励池已原样加入模组内置数据资源；命名空间、卡池与套餐ID保持不变。

使用包含本次资源修改的模组时，无需额外安装数据包；世界加载时按正常数据资源流程读取。用 `/gacha admin list card_pools` 确认出现 `gacha_test:secondary_demo` 后执行下列命令。同级 `secondary-interactions.zip` 保留为独立分发包；旧版模组仍需手动部署该包。

内置文件为 `src/main/resources/data/gacha_test/gacha/card_pools/secondary_demo.json` 和 `src/main/resources/data/gacha_test/gacha/reward_pools/secondary_demo.json`。本次只添加JSON及更新说明，IDEA诊断与游戏内加载验收分开记录；未运行构建、Data、游戏或重载。

测试池使用原版末地城战利品，单抽为1轮、连抽为3轮；不收取套餐费用，道具仍沿用原有消耗规则，生存模式消耗一个启动道具。卡池仅权限等级2及以上可用。

## 宝箱多选一

```mcfunction
/give @s gachaaddiction:sealed_chest[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:secondary_demo",package:"gacha_test:single",gacha_type:"gachaaddiction:chest",secondary_type:"select_one",choice_count:3,force_watch:0b}}]
```

## 宝箱三连抽

```mcfunction
/give @s gachaaddiction:sealed_chest[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:secondary_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:chest",secondary_type:"multiple",force_watch:0b}}]
```

## 图腾多选一

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:secondary_demo",package:"gacha_test:single",gacha_type:"gachaaddiction:totem",secondary_type:"select_one",choice_count:3,force_watch:0b}}]
```

## 图腾三连抽

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:secondary_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:totem",secondary_type:"multiple",force_watch:0b}}]
```

手持道具右键。候选同时播放入场并转正；悬浮显示白色轮廓；选择后其余对象下落，当前对象触发并下落，进入小总览。三连抽在每轮小总览后返回剩余对象，最后展示全部奖励。

将客户端默认动画设为现代或老虎机，分别使用上述显式指定的宝箱、图腾道具，核对来源动画覆盖默认值。也可交换道具的 `gacha_type`，核对显示模型来源与动画类型相互独立。`example:pool` 是旧文档的占位符，不是内置卡池，不再作为本包命令使用。

以上命令已按当前解析器核对字段和引用，尚未执行游戏内抽奖、重载或资产发放验证。
