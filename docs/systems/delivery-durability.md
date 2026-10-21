# 领取与投递持久化窗口：当前证据与主动保存方案

状态（2026-10-21）：背包、直接掉落与邮箱领取的收据、主动检查点和恢复代码已接入。下文各基础批次中的“尚未接入”描述保留为当时的实施边界；当前实现见末节。迁移前检查结果见 verification-20261021.md，不能作为单工程合并后的验证结果，也不证明真实世界中断恢复。

## 已核实的当前版本行为

目标为 NeoForge 21.1.249 / Minecraft 1.21.1。证据来自 IDEA 当前合并源码：

| 入口 | 当前行为 | 对确认逻辑的影响 |
| --- | --- | --- |
| PlayerDataStorage.save，33–46 行 | 序列化玩家、写临时压缩 NBT、调用 safeReplaceFile，再发 SaveToFile；异常只记录 | 方法返回或事件到达不能独立证明写入成功 |
| Util.safeReplaceFile / safeReplaceOrMoveFile，682–701 行 | 外层丢弃内层替换成功 boolean | 不能只依赖“未抛异常” |
| PlayerList.load，334–347 行 | 单人拥有者优先加载 WorldData 的玩家 Tag，其余加载 playerIo | 单人模式只验证 playerdata/<uuid>.dat 不够 |
| IntegratedPlayerList.save | 先缓存玩家 Tag，再调用基类保存 | level.dat 与玩家文件有不同保存时点 |
| MinecraftServer.saveEverything，574–585 行 | 先 saveAll 玩家，再 saveAllChunks | 可作为主动批量检查点入口，仍要核对实际保存结果 |
| ServerLevel.save，810–833 行 | flush 时保存全部实体，随后触发 LevelEvent.Save | 需要区分实体区块与 level.dat 的确认时点 |
| EntityStorage.flush / IOWorker.synchronize | 同步 pending writes，再同步底层存储 | 地面实体不能仅靠生成成功就永久确认 |

Inventory.add 是逐步修改真实槽位的操作。当前每堆后写账本的方式不能令背包保存与账本进度自动形成原子提交。原来的玩家 boolean pending 标记在插入前写入，也不足以表达明确的提交收据。

## 已确认的窗口

1. 邮箱先 take 删除文件，后给物品；进程在两者之间退出会丢失领取意图。
2. 背包已改、玩家尚未保存，账本已推进；恢复时只返 remainder 会漏掉未落盘的已插入部分。
3. 玩家文件、单人 level.dat、实体区块分别保存；一个成功事件不能代表三者一致。
4. DROP 模式在实体持久化之前完成账本，可能恢复出“已交付但实体不存在”。
5. 把领取简单改为最后 take，会把窗口变为重复交付，仍不是解决方案。

## 已实施的独立保护

GachaFileRewardDeliveryLedger 不再在 AtomicMoveNotSupportedException 后降级为普通覆盖；现在抛 IOException，保留原有账本目标，临时文件仍由 finally 清理。支持原子替换的正常路径、写入版本和编码保持不变。

这只防止账本在不支持原子替换的文件系统上继续作弱保证，不证明领取或掉落窗口已修复。IDEA 对该文件的错误和警告均为空；后续编译及已有格式检查通过。

修改前备份为 patches/refactor-baseline-20261020-224218-222，共 680 文件。原 HEAD 和索引校验保持一致。

## 两种时序的共同设计边界

- 先查询并保留原邮箱意图，使用既有投递账本记录领取目的、原 entry ID 和本次 attempt，不另建数据库或独立领取日志。
- 每批在副本上计算原版兼容的槽位结果，避免对真实背包逐堆执行可重入的中间状态；批量应用与本次收据一起形成明确状态。
- 地面掉落在确认前带可恢复标识并暂不可拾取，确认后恢复原拾取规则；重启不能仅根据“当前未找到实体”重新掉落。
- 收据要和背包在同一玩家 NBT 中保存。单人拥有者还须验证实际会被加载的玩家快照；核心层不直接引用客户端 IntegratedPlayerList 类型。
- 先证明确认条件，再完成账本及删除原邮箱。若删除失败，重复领取要先识别已提交账本，不得再给物品。
- 不在 SaveToFile 事件里重写玩家 .dat。该事件只能观察和触发后续确认，不能伪造成功检查点。
- 失败保持待恢复状态，不把未确认资产当作成功交付；具体恢复必须区分已落盘收据、未落盘收据及待处理地面实体，不能直接猜剩余数量。
- 新账本字段、版本边界和收据清理需随实现记录；不为旧的模糊进度伪造已确认收据。

## D08：已选择 A 的到账时序

A（推荐）：保留同次结算确认到账，在批量结算时主动完成必要的同步检查点。涉及单人拥有者或地面掉落时可能需要世界级保存；避免每堆单独保存，但会增加该次结算的保存开销。最终确认前不放开掉落物。

B：结算先排队，等保存检查点完成后才到账。避免每次请求立即触发保存，代价是奖励存在等待期，界面需要表达待到账状态。不能用“已经给出但未确认”冒充延迟提交。

用户已选择 A：同次结算主动完成必要的同步检查点，验证实际保存收据后才确认到账。B 不实施；本次确认尚不代表现行到账流程已完成修复。

## 已实现的玩家收据与保存检查点基础

GachaRewardDeliveryReceipt 记录 playerId、transactionId 和 attemptId，编码版本为 1，位于玩家 NeoForgeData 下本模组的 reward_delivery_receipts。查询核对事务键与载荷身份，移除只针对完全匹配的 attempt，不能清掉同事务的新 attempt 或其他事务。原 pending_reward_deliveries 布尔标记不算收据。

GachaPlayerSaveCheckpoint.saveAndVerify 限定服务端线程，并先核对实际玩家、内存收据和预期背包。普通玩家调用原版玩家保存入口；单人拥有者调用主动世界保存入口。随后读取实际 playerdata/<uuid>.dat，拥有者还核对 level.dat 的 Data.Player；两者均须包含匹配身份、收据和完整背包列表。读取不退回 .dat_old，不因保存方法返回或保存事件而宣称成功，也不在事件中改写玩家文件。

Player Save Checkpoint Regression Checks 已经 IDEA 编译运行，exitCode=0。检查收据身份、独立事务和替换 attempt 的清理隔离、格式拒绝、玩家／背包匹配、旧布尔标记拒绝，以及临时压缩 NBT 文件中的玩家与单人快照一致性。缺失玩家文件时，存在匹配 .dat_old 仍不得确认。错误类型的 Inventory 列表不能被 getter 强制转成空背包。仅有终端高级功能不可用提示。

基础批次当时四文件 IDEA 分析为 0 错误、1 条 saveAndVerify 尚未使用警告；后续已接入普通背包投递，见下节。该基础检查本身不证明真实世界保存、实体存储、停服或资产中断恢复，未启动游戏。

后续仍需完成领取与掉落：地面实体要有确认前保护和实际存储证明。仅核对玩家收据不能替代地面实体持久化。

基础实现前备份为 patches/refactor-baseline-20261020-233517-451，共 690 文件；原 HEAD 与索引 SHA256 保持一致。

## 普通背包投递接入

既有账本格式 4 增加 attempt 与 inbox_entry_id，不建立平行仓库。GachaRewardDeliveryAttempt 持有收据、预期背包及待确认掉落快照；GachaRewardDropSnapshot 核对物品实体类型、身份、有限坐标和收据。快照和所有导出 NBT 均深复制。尝试不能逐堆改写计划，未对账的尝试不能直接走退回分支；已完成记录保留尝试身份。

在线 INVENTORY 先复制原背包，在副本上调用原版 Inventory.add 计算每堆余量，再将完整计划写入账本。实际背包批量替换后写收据，推进 ONLINE_APPLIED，主动保存并读取实际快照确认，最后幂等写余量邮箱、完成账本并清内存收据。库存满时仍保留原有余量进入邮箱的语义。副本制作逐槽复制 ItemStack；原版 replaceWith 只引用传入栈，不能当作深复制入口。

保存事件对含 attempt 的记录不推进账本，因此主动检查点中的 SaveToFile 不会提前确认。恢复看到匹配的玩家收据时确认当前快照，不再重新给物品；没有收据的已规划背包投递重建计划再执行。后续正常物品使用／转移仍遵循原版保存边界，不宣称跨玩家所有交易都成为原子提交。

账本 version 3 可以读取但不补造收据，也不因读取而改写文件。旧背包 APPLYING/ONLINE_APPLIED 缺乏可证明资产状态，现明确报错并保留记录，不再凭 boolean 标记确认或仅返 remainder；需要明确对账，不能安全猜测历史资产。旧 PREPARED 可进入新流程，已完成状态仍保持已完成。

Persistence Format Regression Checks 与 Player Save Checkpoint Regression Checks 在本次接入后分别通过 IDEA 编译运行，exitCode=0。检查覆盖元数据、阶段和临时 NBT 快照，不是实际玩家的背包、登出或重启测试。邮箱领取尚未接入 inbox_entry_id；直接 DROP 仍走原流程，掉落模型尚不代表实体保护和持久化已完成。

本次修改前备份 patches/refactor-baseline-20261020-234108-652 共 694 文件，原 HEAD 和索引保持一致。未运行游戏或故障注入。

## 待确认掉落的保护与原始存储核对

GachaPendingRewardDrops 为实体写入当前收据标记并暂设不可拾取、无敌；ItemEntityMixin 在标记存在时禁止玩家拾取和实体 tick，阻止确认前的移动、合并及自然消失。既有漏斗保护 Mixin 对 addItem(Container, ItemEntity) 的确切重载增加保护，避免漏斗绕过拾取延迟。release 必须匹配本次收据，恢复原无敌属性和既有立即拾取／归属玩家规则。普通实体不受该标记保护。

GachaEntitySaveCheckpoint 按维度和区块分组，经三个 Accessor 读取原版 EntityStorage 的 SimpleRegionStorage；同步既有 IO 后读取原始 NBT，不调用 loadEntities，也不再次实例化实体。确认条件包括区块坐标、实体 UUID 唯一存在、物品 NBT、实体位置和本次收据。不能以“内存中找到实体”替代保存证明。

已扩展 Persistence Format Regression Checks，IDEA 编译运行 exitCode=0：匹配快照通过，缺失实体、错误区块、物品数量／attempt 不一致、重复 UUID 和缺失位置均拒绝。这里核对的是原始 NBT 规则，没有运行真实世界的 Accessor／实体存储路径，也没有执行游戏拾取或漏斗场景。

相关九文件 IDEA 分析全部完成，0 错误、9 警告，分别为五个跨模块 Mixin 配置关联提示、两处借用原版存储对象的资源提示和两个尚未接入方法。四个新 Mixin 类型已登记在根清单；没有为消除提示复制清单或关闭原版拥有的存储。

**掉落生产与恢复尚未接入**：protect 和 verifyAfterSave 还未被发奖流程调用，现行 DROP 仍有原窗口。下一批需在发布实体前记录计划，保存确认后释放，并处理重新加载实体的匹配／过期 attempt。原版 CommonHooks.onPlayerTossEvent 会先建立实体、发布 ItemTossEvent 再加入世界，已核对其捕获和生成顺序，不能先生成可被抽走的实体再补标记。

本批修改前备份 patches/refactor-baseline-20261020-235416-334 共 696 文件；原 HEAD 与索引校验保持一致。

上述基础批次只对标准服务端线程和有效保存快照建立方案；没有声称能超越原版对后续普通物品交易、手工回滚、介质损坏和任意第三方异步改背包的保证。

## 完整接入后的当前实现

GachaPlayerRewardDeliveryHandler 在发布实体前写入完整 attempt：预期背包、玩家收据与掉落快照。INVENTORY 批量替换背包；DROP 只写收据，不替换或广播背包。普通背包余量保留进入邮箱，邮箱领取余量按原规则掉落。掉落实体在捕获的原版投掷流程中准备，保留 ItemTossEvent，并在加入世界前保护。

没有掉落时走玩家保存检查点；包含掉落时主动保存世界，随后核对实际玩家／单人快照与原始实体存储。检查点成功后才写余量邮箱、完成账本、确认原邮箱条目、释放已加载掉落并清除内存收据。旧 SaveToFile 确认入口已移除。

GachaRewardInboxClaim 先 find 保留原条目，由玩家与原 entry ID 派生稳定领取事务。重复已提交领取返回原结果，不再次交付。原条目仅在账本 DELIVERED 后 take；原条目尚在但账本已完成时只完成确认清理。

恢复发现匹配玩家收据时，先检查已加载实体及原始保存记录，再恢复确实缺失的计划实体并重新确认。没有收据时废弃已加载旧 attempt，重建 PREPARED 和新 attempt；旧的未加载实体在后续加入世界时按账本对账。DELIVERED 不重新发奖；登录也清理完全匹配的遗留收据。旧版无收据 APPLYING／ONLINE_APPLIED 在 INVENTORY 与 DROP 下均报错并保留记录，不猜测资产。

迁移前的 IDEA 编译及五项针对性检查通过，标准 Data 退出 0；准确覆盖与第三方错误见 verification-20261021.md。当前源码已迁入根 src，合并后的检查验证待完成；没有执行游戏、真实世界保存／拾取、重启或故障注入。
