# 2026-10-21 代码、统一审查与验证

本记录为单工程合并之前的多项目阶段证据。后续 539 个文件已迁入根 src，Gradle 与运行配置已合并；下述成功结果不得作为迁移后验证。后续备份已发现暂存内容变化，下文索引校验仅描述对应检查时点，不代表当前索引。

## 执行顺序

按本次用户指示，先完成授权范围内的代码修改和局部格式处理，再统一检查正确性，最后运行已授权的 IDEA 编译、针对性检查与 Data。格式处理关闭导入优化和成员重排；审查发现的三个问题先完成修正及格式处理，再复查和验证。

本记录覆盖本次重构，不改变后续任务的默认验证授权。游戏、GL、故障注入没有执行；单 JAR 和发布元数据任务仍待此前提出的明确许可。

## 统一审查

IDEA 全量分析覆盖 541 个 Java 文件，complete=true，全部 analyzed，0 错误、537 条警告。警告主要为既有空值注解约定、Mixin 跨模块清单关联、扩展入口未使用、借用原版资源的关闭提示及写法建议，不将这些数量解释为运行证明。

源码审查后修正：

- DROP 分支只保存收据，不再替换背包栈对象或广播背包变化。
- 登录对账清理与 DELIVERED 账本及 attempt 完全匹配的遗留玩家收据，完成邮箱确认和已加载实体释放，不再发物品。
- 事务队列同时作为受停服状态保护的主线程执行器，提供给协调器、恢复器与运行时回调。关闭后不执行尚未开始的状态修改回调；观察结果结束为失败，WAL 中的未完意图留待下次恢复。移除绕过队列的未使用 cardPoolService 公开取值入口。

修正后的六个 Java 文件再次完成 IDEA 分析，0 错误、14 条警告；这是针对性复查结果，不冒充重新统计全项目警告数量。随后 IDEA 增量编译成功，问题列表为空。

八份 Gradle 脚本及根 Mixin 清单没有 IDEA 错误。原 TOML 模板的两处未展开宏有解析提示；现有展开文件中的两个 dependencies.gachaaddiction 表语法正常，IDEA 无语法错误。没有修改模板或生成文件来掩盖提示。

## 实际执行

| 入口 | 结果 | 覆盖与限制 |
| --- | --- | --- |
| IDEA build_project | isSuccess=true | 当前多模块 Java 增量编译，问题列表为空 |
| WAL Regression Checks | exitCode=0 | IO 回调读取快照、NBT 提交副本、三种持久化方式及重新打开后的顺序 |
| Persistence Format Regression Checks | exitCode=0 | 当前格式、账本 4 元数据和账本 3 无收据状态、掉落快照及稳定领取身份；不是真实世界中断测试 |
| Transaction Queue Regression Checks | exitCode=0 | 七组顺序、重入、失败、观察者取消、关闭与关闭后回调检查 |
| Player Save Checkpoint Regression Checks | exitCode=0 | 收据格式、身份、枚举清理隔离、临时玩家／单人 NBT 保存快照；不调用真实世界保存 |
| Card Pool Fixture Regression Checks | exitCode=0 | 现有三份夹具的显式计划、真实 1/3 轮参数和请求覆盖约束 |
| 标准 Data | exitCode=0 | zh_cn/en_us/ru_ru、物品模型、声音提供器完成；written=0 |

Data 完整日志：C:/Users/wang_/AppData/Local/JetBrains/IntelliJIdea2026.2/tmp/ij_run__Data_14803221468222511009.log。

与检查前备份逐项 SHA256 比较，9 个非缓存生成文件无新增、缺失或内容差异。Data 管理的缓存不参与该比较，没有手工编辑生成结果。原 Git 索引 SHA256 仍为 89602C8DB0AA3AF6443438B2021EBD5C5555CFF728B488FBC25E299B170B016A。

## Data 能证明和不能证明的内容

日志确认本模组的 ItemEntityMixin、ServerLevelAccessor、Hopper 保护、矿车容器 Mixin 等发生了转换。它只证明这些类在本次 Data 路径中的转换，不证明拾取、漏斗、实体存储、重启恢复或视觉场景已经运行通过；另外两个实体存储 Accessor 的实际世界路径没有执行。

源码示例 ZIP 迁出 assets 后，日志不再出现其非法命名空间警告。仍保留第三方 refmap、可选 RefinedStorage 类、union URL 警告，以及 RarityCore 自带旧 KJS 插件加载 dev.latvian.mods.kubejs.KubeJSPlugin 失败。错误来源为 org.yanbwe.raritycore.kubejs.RarityCoreKubeJSPlugin；没有升级依赖、改第三方 JAR 或增加旧包名兼容类。

Data 模式不执行 KJS 启动脚本，因此不能把 Data 退出 0 当作 GachaDrawItems.define/create 的实际脚本调用证明；之前未执行的隔离探针也不计为通过。

## 备份与剩余边界

本阶段完整工作树备份 patches/refactor-baseline-20261021-002540-410 保存 702 个文件，原 HEAD 和索引未变；它记录统一审查前的代码状态。较早的 000340-657 备份保留实际掉落／领取接入之前的状态。

源示例包另有 patches/example-pack-20261021-001601-284/before.zip，迁至 log/examples/Example resource package.zip。清理只移除了 80 个实际为空的 Java 目录，保留声明的源码根、隐藏目录、链接、构建产物、缓存、运行目录和存档。

尚未完成单 JAR/POM/module 实际产物验收，也未完成只包含本次重构差异的 Git 分批提交。两者不因上述检查通过而自动标为完成。
