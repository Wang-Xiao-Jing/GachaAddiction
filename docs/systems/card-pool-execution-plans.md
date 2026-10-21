# 显式卡池执行计划

## 当前格式

卡池只引用显式声明的 execution_plan。卡池根 reward_pool 入口、套餐级 rounds/consumption 适配、generatedPlans 合并以及 GachaCardPoolCompilation 包装已删除。消费应由执行计划中的 consumption 步骤声明；当前套餐只提供 id、parameters 和 minimum_durability。

GachaCardPoolCompiler 直接返回卡池 Map，GachaRuntimeDefinitionCompiler 使用声明的执行计划进行后续 hydration 和校验。原有参数绑定、展示、可用性和投递解析保留；参数所属者现在明确是执行计划。

旧的隐式 generated/card_pool/... 执行计划不再创建。数据包需要声明 execution_plans 下的资源并显式引用；没有为旧入口建立转发壳。当前 NBT 写入格式和既有卡池、套餐、奖池资源 ID 保持不变。

## 已迁移夹具

| 卡池 | 显式执行计划 | 套餐与轮次 |
| --- | --- | --- |
| gacha_test:secondary_demo | gacha_test:secondary_demo | gacha_test:single=1，gacha_test:triple=3 |
| gacha_demo:custom_items | gacha_demo:custom_items | gacha_demo:single=1，gacha_demo:triple=3 |

secondary_demo 的内置资源与独立数据包目录同步迁移；custom_items 的示例数据包同步迁移。所有展示信息、权限、投递方式和奖池目标与迁移前逐项一致，仍没有套餐额外消费。

每个计划声明 INPUT/required 的 long 参数 rounds。卡池以 CONSTANT、PACKAGE_VALUE 绑定它，各套餐分别提供 1 或 3。reward_pool 步骤从该计划所属参数读取轮次，目标仍为原奖池；普通请求参数不能覆盖套餐提供的轮次。一次和三次仍表示真实轮次，而不是复制同一批奖励。

## 现行功能入口名称

| 旧名称 | 当前名称 | 保留的行为 |
| --- | --- | --- |
| inspectLegacy | inspectContainer | 查询来源与现行原版、扩展容器状态 |
| openLegacy | openContainer | 更新容器打开状态 |
| isLootrContainer | isLootContainer | 判断所有支持的战利品容器，包含原版来源 |
| createLegacy | createTemplate | 创建模板界面，保留扩展失败时的原生回退 |

槽机既有 LEGACY 翻译键未修改。上述功能入口只是明确职责名称，没有删除原版容器、Lootr 适配或 LDLib2 界面能力。

## 检查证据

- 3 份卡池、3 份计划和 3 个 Java 文件的 IDEA 诊断全部完成：0 错误、1 条既有泛型写法建议。
- 4 个方法语义改名后，相关 5 个调用文件完成 IDEA 错误诊断，0 错误。
- 已逐项读取三份迁移前后 JSON：套餐 ID、轮次、presentation、availability、delivery、奖池目标与无消费状态一致。
- main/test 源码搜索没有 legacyPlan、legacyPackages、generatedPlans、GachaCardPoolCompilation 和四个旧方法名称命中。
- 新增 GachaCardPoolFixtureRegressionChecks 与 IDEA Card Pool Fixture Regression Checks 配置，计划检查实际夹具的执行计划编译、1/3 轮参数求值、请求覆盖保护及旧 reward_pool 根字段拒绝；尚未执行。它不包含真实奖池抽取、非空物品发奖或游戏内交互。
- 本批未执行编译、Data、测试、游戏或重载。静态 JSON 对比不能替代执行计划检查和资源加载验收。

## 分发包

log/test-packs/secondary-interactions.zip 已从当前目录重新归档，包含卡池、执行计划、奖池、pack.mcmeta 和 README 五个条目，使用标准正斜杠路径；归档中的执行计划文本已与源文件核对。当前 SHA256：334F719A0A05F2303E13A4079FF8F931DE86713EE48B9E0E6AC63A3DB69CA92A。

原 ZIP 保存在 E:\xm\mc\mod\gachaaddiction\gachaaddiction_1.21.1\patches\secondary-pack-20261020-211716-676\before.zip，原 SHA256：CE71464019B1834E5CAB6634E77B0983CB079C5747657DB79EAC83482B40E928。README 更新前的中间 ZIP 另保存在 E:\xm\mc\mod\gachaaddiction\gachaaddiction_1.21.1\patches\secondary-pack-20261020-211841-023\before.zip。

最初归档尝试因 Windows 条目分隔符、程序集加载及备份参数处理失败，均在原包替换前停止；失败临时目录保留。最终两次归档操作成功退出，最后一次同步当前 README。这里只进行了数据包 ZIP 文件操作，没有调用模组构建任务。

删除前源码和原夹具的完整备份仍为 patches/refactor-baseline-20261020-205208-847，已确认删除的包装类、编译器和三份原 JSON 均在清单中。Git 索引 SHA256 仍为 89602C8DB0AA3AF6443438B2021EBD5C5555CFF728B488FBC25E299B170B016A，没有将既有暂存内容提交。

