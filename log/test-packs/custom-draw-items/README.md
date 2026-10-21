# 自定义抽奖物品示例

适用于1.21.1；仅提供文件，未自动部署或执行。完整规则见[自定义抽奖物品与现代2](../../2.0.0自定义抽奖物品与现代2.md)。

- `data-pack/`：放入目标存档的 `datapacks/`，数据包格式48；包含卡池、单抽/三抽套餐及两个物品定义。
- `resource-pack/`：放入客户端 `resourcepacks/` 并启用，资源包格式34；通过模型值1把祈愿图腾外形改为绿宝石图形。
- `server_scripts/gacha_items.js`：可选，放入实例 `kubejs/server_scripts/`；仍需上述数据包提供卡池，新增脚本抽奖券定义。

部署和重载由服主操作。普通玩家抽奖需权限等级至少2；物品发放命令需现有MANAGE_ADMIN能力（默认等级4）。示例使用末地城原版战利品表，抽奖后消耗载体，不额外收费。

```mcfunction
/gacha item give @s gacha_demo:olive_ticket 1
/gacha item give @s gacha_demo:chest 1
```

第一项使用现代2和自定义外形，第二项使用多轮箱子演出。启用KubeJS脚本后还可发放 `gacha_demo:script_ticket`。控制台请将 `@s` 改为玩家名或 `@a`。

未安装资源包时仍能抽奖，但外形回到原载体。未安装KubeJS时数据包定义及指令仍可用。新语言键已通过禁用Make的Data配置进入中英文生成资源，运行边界见[入场与定时演出](../../2.0.0入场与定时演出.md)。
