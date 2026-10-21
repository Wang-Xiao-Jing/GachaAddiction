# 持久化格式与删除清单

旧格式删除批次按照 D01=B 移除被替代的读取适配；随后 B03 账本接入了投递收据。当前版本如下：

| 存储 | 当前写入版本 | 已移除的读取适配 |
| --- | --- | --- |
| 事务 NBT payload | 2 | version 1 的 card_pool、package、card_pool_snapshot 来源拼装 |
| 卡池 NBT snapshot | 5 | version 3/4 的执行计划 ID 与投递处理器缺省 |
| 奖励邮箱 | 2 | version 1 的 REWARD kind 缺省 |
| 投递账本 | 4 | version 1/2 的退回奖励和交付进度缺省；version 3 仅保留明确无收据的读取状态 |

除账本明确保留 version 3 外，读取器拒绝不等于当前版本的输入。当前卡池事务来源必须包含 delivery_handler；不再根据旧 delivery_mode 字段或缺失字段隐式选择处理器。卡池服务创建来源时继续显式写入当前处理器。

旧格式删除批次当时的四个写入实现与该批修改前快照逐项比较一致；随后账本版本提升为 4，新增 attempt 和 inbox_entry_id。文件名和目录不变，读取 version 3 不重写原文件，也不凭空补收据。无收据的旧背包 APPLYING/ONLINE_APPLIED 不自动确认或猜测剩余资产，需明确对账；PREPARED 和已完成状态保留其实际含义。这里没有访问、转换或删除真实存档。工作区代码备份不是玩家存档备份。

## 已删除入口

- IToServerAndClientPayload：IDEA 语义引用为空，源码、资源、测试夹具和运行配置搜索仅命中自身声明；删除了公共层剩余的 AbstractClientPlayer 引用。
- GachaRarity：原先的组合模型已由分别查询名称、颜色、等级的提供器替代，语义与文本引用检查仅命中自身声明。
- Payloads.clientAndServer、playToServer、playToClient、playBidirectional：删除空入口及无调用私有包装。实际 server/client 注册方法体与删除前一致，协议版本仍为 4。
- 四个模块的 .gitkeep：源码根建立后的占位文件，已经备份后删除。
- 矿车 Mixin 未使用的 lootTable Shadow 字段：保留实际使用的 getLootTable Shadow 和既有状态、NBT、注入签名。
- 快照解码器中仅供旧版本使用或已无引用的四个私有键常量。

后续已迁移 secondary_demo 等三份夹具并删除 reward_pool 卡池适配，详见 card-pool-execution-plans.md。原版容器适配、模板界面和既有槽机翻译键保留；现行功能入口只明确了职责名称。

## 备份

删除前备份：patches/refactor-baseline-20261020-205208-847。

包含 670 个文件、SHA256 清单、原始 Git 索引、HEAD、状态与两份二进制差异。上述被删除的完整文件均已核对存在于清单中。备份前后索引 SHA256 保持 89602C8DB0AA3AF6443438B2021EBD5C5555CFF728B488FBC25E299B170B016A，HEAD 保持 786f920b346f2780c7b1887debeee9d407042ae1。

## 检查与未执行项

本批 8 个 Java 文件均完成 IDEA 分析，0 错误。诊断保留两条快照编解码器写法建议，以及矿车 Mixin 的“未找到配置”提示。根 gachaaddiction.mixins.json 仍包含 entity.AbstractMinecartContainerMixin，元数据模板仍声明该清单；未通过重复放置清单或忽略注解掩盖提示，待多模块产物装配验证。

core/test 的 GachaPersistenceFormatRegressionChecks 已经 IDEA 编译运行通过，exitCode=0。除原有事务、空奖励邮箱／账本与旧版本拒绝检查外，新增投递计划／掉落快照深复制、重复实体和不匹配收据拒绝、账本各阶段元数据往返、version 3 无伪造收据／无读取重写，以及收据元数据降版拒绝。检查只操作临时目录，不使用存档；非空物品注册表编解码、真实领取原子性、断电恢复和当前完整卡池往返仍未覆盖。

早期格式删除批次没有主动运行验证；后续邮箱领取和直接掉落均已接入收据、保护及主动检查点，当前实现见 delivery-durability.md。单工程的编译、格式检查和 Data 均已完成，清单 Mixin 对应类也已核对位于 JAR；结果见 single-project-verification-20261021.md。真实世界验证仍未执行，不能由格式检查推断中断恢复已经通过。
