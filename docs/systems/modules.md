# 单工程职责与依赖边界

2026-10-21：api/core/client/compat/data 源码已合并至根 src，settings.gradle 只保留根项目，内部 project(...) 依赖及聚合输出配置已移除。职责继续通过 Java 包和客户端／可选依赖加载边界表达，见[单工程合并计划](single-project-merge-plan.md)。原换行已恢复，IDEA 已识别单工程，541 个 Java 文件诊断 0 错误，IDEA 编译、五项检查及 Data 均完成；准确结果及限制见[单工程验证](single-project-verification-20261021.md)。下文多项目结构与验证仅为迁移前历史记录，不代表当前物理结构。

当前根 src/main/java 共 536 个 Java 文件，src/test/java 共 5 个检查文件。原 api 的共享契约、cardpool／persistence 等服务端职责、client 界面与渲染、compat 可选适配及 data 生成器继续位于各自包中，不再各自声明 Gradle project。兼容库 compileOnly 声明与测试 modding dependencies 已并入根 build.gradle；资源、模板和生成输出保留原路径。

用户明确许可后，单工程 JAR、POM 和 module 已通过 IDEA 离线生成并逐项核对：源码类、Mixin、673 个编译类和 52 个处理后资源无缺失／差异，元数据没有子项目依赖。准确产物哈希与限制见单工程验证记录。下文待授权、未生成等描述仅为旧多项目阶段的历史边界。

## 合并前的多项目历史

此前五个 Gradle 子项目完成过 IDEA 编译及标准 Data。迁移前 main Java 文件分布为 api 9、core 396、client 51、compat 72、data 6，根项目两个构造入口。以下依赖与装配说明仅记录该历史结构，已被单工程配置取代；最终 JAR 与发布装配验收仍未完成。

| 项目 | 职责 | 项目依赖 |
| --- | --- | --- |
| api | 共享标识、数据模型与扩展契约 | 无内部项目依赖 |
| core | 服务端抽奖、事务、持久化、配置、公共网络与注册 | api |
| client | 原生界面、渲染、客户端载荷处理、客户端契约 | core |
| compat | 可选库与第三方模组适配 | client |
| data | 语言、模型与声音生成源 | core |
| 根项目 | 模组构造与兼容装配、运行配置、资源和最终 JAR | 所有子项目 |

根项目为同一个 mod 声明各模块的 main sourceSet，并将输出装入原 mod ID 对应的单个 JAR。子项目使用相同 Minecraft、NeoForge、Parchment 与 Java 版本，避免同名但不同版本的类型。

根项目的内部模块依赖归 embeddedModules，仅扩展 compileClasspath 与 runtimeClasspath，不进入 components.java 的 apiElements/runtimeElements 发布依赖。第三方 compileOnly 归兼容模块，根两个入口只通过稳定签名调用适配入口；根项目保留原第三方运行开关、版本、Jade/JEI 和 MixinSquared 装配。重复 Modrinth、JitPack 仓库及 Architectury 声明已清除，保留先出现的仓库范围和版本；资源打包排除 .cache 内容，但不删除缓存。

IDEA 已识别 Module Assembly Check 配置，任务为 :jar、:generatePomFileForMavenJavaPublication、:generateMetadataFileForMavenJavaPublication，使用 --offline，不执行 publish。执行请求被自动审批拒绝，理由是现有编译、Data、针对性检查授权不覆盖 Gradle 产物生成；三项任务尚未运行，最终 JAR 内容和 POM/module 依赖仍待实际核对。

语言注册表 GachaTranslations 位于 core 的 registry/language，运行时翻译键可以使用它；仅负责生成输出的 Provider 位于 data。core 的 test sourceSet 显式接入 ModDevGradle 的 Minecraft 依赖，用于不启动游戏的 WAL 回归检查。

公共数据包不持有界面实现；客户端构造时安装客户端处理器。可选稀有度与容器信息通过公共接口注册，原生界面通过客户端扩展接口使用 LDLib2 与 ColorTooltips。可选 Mixin 按加载列表选择，客户端 Mixin 列在 client 清单中。

模块配置、源码位置、编译依赖和最终包内容必须一起验收；仅生成空 Gradle 子项目不能视为模块拆分完成。
