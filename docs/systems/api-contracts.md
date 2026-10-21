# Java 契约与兼容迁移

## 命名和客户端边界

普通顶层领域／服务接口使用 I 前缀。当前已逐项检查 68 个顶层接口，为 45 个普通接口通过 IDEA 语义重命名更新实现类和调用方。TranslationKeys 是常量归集接口，六个 Mixin Accessor 遵循框架名称；现有桥接接口和 GachaTypeClient.ScreenCreator 嵌套回调不机械改名。

客户端契约位于根源码的 client/api 包。该目录包含 GachaCardPoolClientApi、IReelHost、GachaTypeClient、GachaRewardPhase、IGachaPresentationExtension、IGachaItemBorderRenderer。公共端不通过 GachaType 查找客户端实现，界面查找仍归客户端注册表。

本次 D01=B 不保留旧名称的转发包装。第三方源码需要更新类型名称与 import，使用旧二进制类名的扩展需要重新编译。注册 ID、资源路径、Codec 字段和网络协议不随 Java 类型名改变。

后续已通过 IDEA 将 api.GachaaData 更名为 api.GachaData，现位于根源码；保留记录成员 isOpenGachaaUi、既有桥接方法和 NBT 键，不改变四段 StreamCodec 的字段顺序。B03 新增三个实体存储 Accessor，仍采用框架命名，不加普通接口 I 前缀。运行时移除未使用的 cardPoolService 取值入口，避免绕过统一队列；领取经 claimRewards 返回 CompletionStage，命令返回 1 表示请求已受理，实际结果随后发送。

## 普通接口迁移表

| 原全限定名 | 当前全限定名 |
| --- | --- |
| xiaojin.gachaaddiction.api.client.ReelHost | xiaojin.gachaaddiction.client.api.IReelHost |
| xiaojin.gachaaddiction.data.GachaRuleDefinitionCompiler | xiaojin.gachaaddiction.data.IGachaRuleDefinitionCompiler |
| xiaojin.gachaaddiction.reward.GachaLootTableResolver | xiaojin.gachaaddiction.reward.IGachaLootTableResolver |
| xiaojin.gachaaddiction.reward.GachaRewardPool | xiaojin.gachaaddiction.reward.IGachaRewardPool |
| xiaojin.gachaaddiction.reward.GachaRewardPoolResolver | xiaojin.gachaaddiction.reward.IGachaRewardPoolResolver |
| xiaojin.gachaaddiction.reward.rule.GachaRuleAction | xiaojin.gachaaddiction.reward.rule.IGachaRuleAction |
| xiaojin.gachaaddiction.reward.rule.GachaProbabilityCondition | xiaojin.gachaaddiction.reward.rule.IGachaProbabilityCondition |
| xiaojin.gachaaddiction.reward.custom.GachaCustomTableResolver | xiaojin.gachaaddiction.reward.custom.IGachaCustomTableResolver |
| xiaojin.gachaaddiction.reward.custom.GachaItemTagResolver | xiaojin.gachaaddiction.reward.custom.IGachaItemTagResolver |
| xiaojin.gachaaddiction.reward.number.GachaDoubleProvider | xiaojin.gachaaddiction.reward.number.IGachaDoubleProvider |
| xiaojin.gachaaddiction.reward.custom.GachaCustomEntry | xiaojin.gachaaddiction.reward.custom.IGachaCustomEntry |
| xiaojin.gachaaddiction.reward.quantity.GachaQuantityOperation | xiaojin.gachaaddiction.reward.quantity.IGachaQuantityOperation |
| xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeResolver | xiaojin.gachaaddiction.reward.parameter.IGachaParameterTypeResolver |
| xiaojin.gachaaddiction.reward.source.GachaRewardSource | xiaojin.gachaaddiction.reward.source.IGachaRewardSource |
| xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescribable | xiaojin.gachaaddiction.reward.probability.IGachaProbabilityDescribable |
| xiaojin.gachaaddiction.reward.number.GachaNumberExpression | xiaojin.gachaaddiction.reward.number.IGachaNumberExpression |
| xiaojin.gachaaddiction.reward.number.GachaLongProvider | xiaojin.gachaaddiction.reward.number.IGachaLongProvider |
| xiaojin.gachaaddiction.cardpool.GachaCardPoolSnapshotResolver | xiaojin.gachaaddiction.cardpool.IGachaCardPoolSnapshotResolver |
| xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedger | xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardDeliveryLedger |
| xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinitionResolver | xiaojin.gachaaddiction.cardpool.IGachaCardPoolDefinitionResolver |
| xiaojin.gachaaddiction.reward.rule.GachaRuleCondition | xiaojin.gachaaddiction.reward.rule.IGachaRuleCondition |
| xiaojin.gachaaddiction.cardpool.parameter.GachaPlayerChoiceConstraint | xiaojin.gachaaddiction.cardpool.parameter.IGachaPlayerChoiceConstraint |
| xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInbox | xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardInbox |
| xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionParameterSchemaResolver | xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionParameterSchemaResolver |
| xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionEffects | xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionEffects |
| xiaojin.gachaaddiction.cardpool.parameter.GachaParameterStateRepository | xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterStateRepository |
| xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionPayloadEncoder | xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionPayloadEncoder |
| xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDeliveryHandlerResolver | xiaojin.gachaaddiction.cardpool.transaction.IGachaRewardDeliveryHandlerResolver |
| xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDeliveryHandler | xiaojin.gachaaddiction.cardpool.transaction.IGachaRewardDeliveryHandler |
| xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionHandlerResolver | xiaojin.gachaaddiction.cardpool.transaction.IGachaConsumptionHandlerResolver |
| xiaojin.gachaaddiction.cardpool.parameter.GachaParameterStateReader | xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterStateReader |
| xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionParticipant | xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionParticipant |
| xiaojin.gachaaddiction.execution.GachaParameterValue | xiaojin.gachaaddiction.execution.IGachaParameterValue |
| xiaojin.gachaaddiction.execution.GachaExecutionPlanResolver | xiaojin.gachaaddiction.execution.IGachaExecutionPlanResolver |
| xiaojin.gachaaddiction.execution.GachaParameterMappingDecoder | xiaojin.gachaaddiction.execution.IGachaParameterMappingDecoder |
| xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionHandler | xiaojin.gachaaddiction.cardpool.transaction.IGachaConsumptionHandler |
| xiaojin.gachaaddiction.persistence.GachaPoolSnapshotArchive | xiaojin.gachaaddiction.persistence.IGachaPoolSnapshotArchive |
| xiaojin.gachaaddiction.persistence.GachaCardPoolSnapshotCodec | xiaojin.gachaaddiction.persistence.IGachaCardPoolSnapshotCodec |
| xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionJournal | xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionJournal |
| xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDelivery | xiaojin.gachaaddiction.cardpool.transaction.IGachaRewardDelivery |
| xiaojin.gachaaddiction.execution.GachaExecutionComponentResolver | xiaojin.gachaaddiction.execution.IGachaExecutionComponentResolver |
| xiaojin.gachaaddiction.execution.GachaExecutionStep | xiaojin.gachaaddiction.execution.IGachaExecutionStep |
| xiaojin.gachaaddiction.cardpool.GachaCardPoolSnapshotManager | xiaojin.gachaaddiction.cardpool.IGachaCardPoolSnapshotManager |
| xiaojin.gachaaddiction.cardpool.parameter.GachaParameterFingerprint | xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterFingerprint |
| xiaojin.gachaaddiction.execution.GachaExecutionStepType | xiaojin.gachaaddiction.execution.IGachaExecutionStepType |

其余原 api/client 中的客户端类型保持简单类名，只将包名改为 client/api；IReelHost 同时完成普通接口命名调整。公开事件 RegisterGachaRuntimeTypesEvent 的当前包名为 event。

## 当前证据

- 45 个接口的当前声明体与重命名前快照逐项比较：仅替换类型标识符并排除 package/import 和空白差异后，45/45 一致。
- 迁移后的全量 IDEA 错误诊断覆盖 528 个文件，全部 analyzed、complete=true、errorCount=0；本次使用错误过滤，不能解释为全项目没有警告。
- main Java 源码中没有原 api.client 包引用；剩余未带 I 前缀的顶层接口只包含上述常量接口和六个 Accessor。
- 本批没有执行编译、Data、测试、游戏或持久化恢复场景。

## 旧格式清理的审查结果

下列兼容路径已经定位，按实际删除状态记录；格式清理的删除证据和未执行验证见 persistence-formats.md：

| 当前入口 | 已确认的旧读取或依赖 |
| --- | --- |
| GachaTransactionNbtPayloadDecoder | 已删除 payload version 1 的来源解码；只接受 2 |
| GachaCardPoolNbtSnapshotCodec | 已删除 snapshot version 3/4 的套餐和投递读取；只接受 5 |
| GachaFileRewardInbox | 已删除 inbox version 1 的缺省 kind；只接受 2 |
| GachaFileRewardDeliveryLedger | 新写入 4；保留 3 的明确无收据状态，不伪造确认；已删除 1/2 缺省适配 |
| GachaCardPoolTransactionSource | 已删除 delivery_mode 兼容字段；来源显式指定处理器 |
| IToServerAndClientPayload | 完成语义、非 Java 入口检查和备份后，通过 IDEA 安全删除 |
| GachaCardPoolCompiler | 已迁移三份夹具并删除 reward_pool 及隐式生成计划分支；当前只接受 execution_plan，详见 card-pool-execution-plans.md |

现行容器适配、模板界面功能均保留，入口已分别改名为 inspectContainer/openContainer、isLootContainer 和 createTemplate。槽机的 LEGACY 翻译键属于既有资源身份，未因 Java 命名整理而改变；旧方法名与当前名称的迁移表见 card-pool-execution-plans.md。

用户资产、存档及原文件备份保持保护；版本读取清理不等于删除数据文件。每个删除批次需要独立记录当前写入版本、拒绝旧版本行为、引用证据和恢复入口。
