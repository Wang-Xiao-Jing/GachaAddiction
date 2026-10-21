# 2026-10-21 单工程合并验证

## 迁移与格式

五个原子项目的 539 个 Java 源码／测试文件已移入根 src，包名与正文保持不变。530 个文件迁移后字节完全一致；9 个文件由获用户许可的 IDEA 终端脚本恢复 396 个 CR 和 3 个 LF，脚本逐文件核对恢复结果与迁移前备份完全一致，退出码 0。修复前备份为 patches/line-ending-repair-20261021-083330-591。

IDEA 只读模块列表当前仅有 gachaaddiction_1_21_1、gachaaddiction_1_21_1.main 和 gachaaddiction_1_21_1.test。本轮没有执行同步或重载。settings.gradle 不再声明子项目；根 build.gradle 统一源码、依赖、modding test 依赖和 Mod sourceSet；检查配置指向根 test，Data 指向根 main。

## 统一正确性检查

IDEA 批量分析 541 个 Java 文件，complete=true、全部 analyzed，无超时、失败、跳过或遗漏，errorCount=0。warningCount=497，但至少一个文件达到单文件上限，因此警告数为下限；该数量不表示无运行风险。根 build.gradle 没有错误，有 8 条类型推断弱警告；settings.gradle 与根 Mixin 清单无错误或警告。

## 实际执行验证

| 入口 | 结果 |
| --- | --- |
| IDEA build_project，非 rebuild | isSuccess=true，仅 RarityUtil.java 的 Rarity.color() 既有弃用警告 |
| WAL Regression Checks | exitCode=0 |
| Persistence Format Regression Checks | exitCode=0 |
| Transaction Queue Regression Checks | exitCode=0 |
| Player Save Checkpoint Regression Checks | exitCode=0 |
| Card Pool Fixture Regression Checks | exitCode=0 |
| 标准 Data | exitCode=0，语言、物品模型和声音提供器完成，written=0 |
| Artifact Assembly Check，--offline | exitCode=0，BUILD SUCCESSFUL；生成 JAR、POM 和 module 元数据，未执行 publish |

Data 日志为 C:/Users/wang_/AppData/Local/JetBrains/IntelliJIdea2026.2/tmp/ij_run__Data_14755390161030352519.log。日志确认本模组只从根 out/production/classes 和 resources 加载。与 Data 前备份逐项 SHA256 对照，9 个非缓存生成文件无新增、缺失或内容变化。

日志仍包含第三方 RarityCore 的旧 KJS 插件 org.yanbwe.raritycore.kubejs.RarityCoreKubeJSPlugin 加载 dev.latvian.mods.kubejs.KubeJSPlugin 失败，以及可选 RefinedStorage BaseScreen 缺失警告。没有改第三方依赖或宣称日志无错误。Data 不证明 KJS 启动脚本调用、实际世界保存／恢复、拾取或视觉效果；未运行游戏或故障注入。

## 备份、产物与提交

Data 前工作树备份 patches/refactor-baseline-20261021-083949-412 保存 699 个文件，当时 HEAD 为 786f920b346f2780c7b1887debeee9d407042ae1，索引 SHA256 为 98866C83D30D0ABF62BFF9145E2595FD4117498DFBE5C8A94EDFEF7DE8862972。它不同于最初基线；提交前另有 patches/refactor-baseline-20261021-085231-082，保存 700 个文件及该索引。原暂存内容的快照继续保留。

用户随后明确许可产物检查，IDEA Artifact Assembly Check 已成功执行 :jar、:generatePomFileForMavenJavaPublication 和 :generateMetadataFileForMavenJavaPublication。产物为 build/libs/gachaaddiction-1.1.4.jar，2,325,705 字节，SHA256 为 11bfc95c34ffb278a82ab72d4595b3e3eabd10053677d6652fe738ea9ff3a6c9；module 元数据中的哈希与大小一致。未执行发布。

JAR 共 836 个条目：536 个生产 Java 源码对应类均存在，Mixin 清单对应类无缺失，无重复条目、回归检查类或 .cache 内容。唯一模组元数据和 MixinSquared 0.3.7-beta.2 嵌入包存在。673 个编译类与 52 个处理后资源逐项 SHA256 比较均与 JAR 内对应文件一致。POM 与 module 无本模组内部子项目依赖；保留既有第三方运行依赖，不以此次合并改变发布策略。Gradle 报告既有弃用及未经检查操作提示，没有为此扩大修改范围。

原 HEAD 只有 152 个文件／105 个 Java 文件，初始状态有 498 项暂存；例如 GachaTransactionId 和 GachaPlayerRewardDeliveryHandler 初始均为 AM，且前者在 HEAD 不存在。用户随后明确许可先提交原有工作，再分批提交重构。因此用原始备份和原索引路径重建隔离基线，逐文件 SHA256 核对 609 个原有工作文件；缓存、IDE 工作区和本地辅助脚本不进入基线。正常 Git 过滤规则和原可执行位保留，不把已有源码冒充重构新增。

基线提交为 2bf06e9b08cee7d857c410372516d048f7be2e7d，代码／资源／检查配置提交为 138047eb6b62382d7ed047a1fd62a60c9f4efd5c。后者相对基线涉及 361 个路径；共享契约、调用方、持久化修复及配套检查保持在同一代码批次，避免只提交部分调用更新。七个可复用 IDEA 检查／Data 配置随代码纳入版本管理，本地备份脚本不提交。文档与规范另成收尾批次，提交 ID 以 Git 日志为准。

提交使用隔离目录与临时索引，不回写当前源码；原始及提交前索引均已备份。临时索引与工作树核对一致后，仅将实际索引对齐最终 HEAD，不执行 checkout、reset、clean、发布或推送。提交记录和保护边界见 refactor-completion.md。
