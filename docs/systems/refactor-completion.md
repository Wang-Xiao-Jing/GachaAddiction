# 2026-10-21 重构收尾

本次范围为 GachaAddiction 1.21.1 的代码重构、B01～B04 修复、资源与文档整理，以及用户许可的检查和提交。最终为一个 Gradle project；api、client、compat、data 等仅保留 Java 包职责。没有修改 Minecraft、NeoForge、Java 或依赖版本来绕过问题。

| 范围 | 当前证据 |
| --- | --- |
| 基线与原有工作保护 | 原始 625 文件备份、各删除前备份及提交前 700 文件备份；609 个原有工作文件独立提交 2bf06e9 |
| 契约、网络与事件 | api-contracts.md、events.md；45 个普通接口语义改名，公共／客户端处理拆分，协议 4、资源 ID 与 Codec 顺序保持 |
| 单工程 | single-project-merge-plan.md、modules.md；539 个迁移文件核对，IDEA 仅有根及 main/test，根 Gradle 无子项目声明或依赖 |
| 会话、道具、工作区与展示 | draw-items.md、presentation-and-announcements.md；原状态所有权保留，道具加载、查询与创建职责拆分 |
| UI 与渲染 | reward-summary-view.md、scene-rendering.md；汇总组合、来源缓存、箱盖覆盖和目标所有权；源码时序比较记录保留 |
| B01／B04 | 矿车两个确切注入签名与 NBT 方向、可选及客户端 Mixin 边界修正；清单对应类已核对在 JAR 中 |
| B02 | transaction-queue.md；队列、回调和关闭保护，WAL 与七组队列针对性检查均退出 0 |
| B03 | delivery-durability.md、persistence-formats.md；背包／掉落／领取收据、同步检查点和恢复已接入，临时 NBT／格式检查退出 0 |
| 清理与兼容策略 | api-contracts.md、persistence-formats.md、historical-documents.md；旧接口／格式按 D01=B 清理，删除有备份，不删除存档或缓存 |
| 示例与 Data | card-pool-execution-plans.md、single-project-verification-20261021.md；实际示例 ID 和真实 1/3 轮保留，三份夹具检查通过，Data 生成输出 9 文件无变化 |
| 统一检查与产物 | single-project-verification-20261021.md；541 Java 全覆盖 0 错误、IDEA 编译和五项检查通过；JAR/POM/module 成功生成并核对 |
| 提交隔离 | 基线 2bf06e9，代码／资源／检查配置 138047e，文档与规范独立收尾；完整 ID 与提交前快照见验证记录及 Git 日志 |

代码批次之后没有继续改动生产代码。文档收尾只更新当前入口、历史范围说明、实际证据和提交记录，不因提交重复运行已经完成的构建、Data 或检查。最终以 Git 提交链、索引／工作树差异和提交前快照比较核对收尾，不用一条绿色诊断替代全部需求证据。

游戏、视觉、实际世界保存／拾取／重启、故障注入、可选依赖缺失矩阵、KJS 启动脚本和性能测量均未执行；这些未获授权的运行场景继续保留在原验收清单，不标记通过。Data 仍报告第三方 RarityCore 旧 KJS 插件错误；Java／Gradle 的既有警告及历史 Markdown 路径提示保留，没有声称全项目无警告或运行场景全部通过。

本地 patches 中保留原始索引、提交前索引、完整文件备份及提交日志；build、out、.gradle、run、存档和用户资产均不作清理。未推送、发布或启动游戏。
