# Ys影视

基于 [FongMi TV](https://github.com/FongMi/TV)（CatVod 系）开源魔改的安卓影音应用，支持 **Android TV 大屏** 与 **手机** 两种形态，通过外部配置灵活扩展点播 / 直播内容。

当前版本：**0.0.1**（versionCode 随提交自动递增，见[版本规则](#版本规则)）

[讨论群组](https://t.me/fongmi_official) | [发布频道](https://t.me/fongmi_release)

---

## 目录

- [功能特性](#功能特性)
- [项目架构](#项目架构)
- [设置页结构](#设置页结构)
- [版本规则](#版本规则)
- [打包与签名](#打包与签名)
- [配置说明](#配置说明)
- [延伸阅读](#延伸阅读)

---

## 功能特性

### 播放器
- **ExoPlayer（Media3）+ FFmpeg 软解**，硬解 / 软解自动降级
- **IJK 播放器**：已从 webhtv 移植 ijkplayer 0.8.8，可在播放器引擎中切换
- 渲染支持 SurfaceView / TextureView
- DRM：Widevine、PlayReady、ClearKey，支持 `#KODIPROP` 声明
- 弹幕：DanmakuFlameMaster，与播放时间轴同步，支持远程推送
- 字幕：SRT / SSA / ASS 外挂、系统 CaptioningManager、远程即时注入，独立**字幕设置**页
- 倍速、多缩放比例、画中画、背景音频、片头 / 片尾自动跳过

### 点播
- 多站点分类浏览、Filter 筛选（年份 / 地区 / 类型）
- 多站点并行搜索，关键字自动繁转简
- 播放失败自动换源：解析器 → 线路 → 搜索其他站 → 下一站点
- 观看记录（保留 60 天）、收藏、无痕模式

### 直播
- 支持 M3U、TXT（`#genre#` 分组）、JSON 三种直播源格式
- EPG：XMLTV 格式（支持 `.gz`），每 6 小时自动刷新
- 追看 / 时移、频道收藏、隐藏分组密码保护

### 网络与扩展
- 爬虫引擎：Java JAR（DexClassLoader）、JavaScript（QuickJS）、Python（Chaquopy）
- DoH、代理（HTTP/HTTPS/SOCKS4/5）、Hosts 覆盖、CORS 注入、广告拦截
- WebView 嗅探（regex 拦截媒体 URL，UA 伪装）
- DLNA 投放：手机版 DMC 投放，电视版 DMR 接收
- Android Auto：车机端浏览播放记录与直播频道
- 远程控制：本地 HTTP 服务器（9978–9998 端口）

---

## 项目架构

| 项目       | 值                             |
|----------|-------------------------------|
| 应用名     | Ys影视                        |
| package  | `com.fongmi.android.ys`        |
| minSdk   | 24（Android 7.0）              |
| abi      | `arm64-v8a`、`armeabi-v7a`     |
| flavor   | `leanback`（电视版）、`mobile`（手机版） |

```
FongMiTV/
├── app/            主应用（leanback / mobile 两套 UI，main 为共用逻辑）
├── catvod/         爬虫抽象层（Spider 接口、OkHttp 网络栈）
├── quickjs/        QuickJS JavaScript 引擎
├── chaquo/         Chaquopy Python 引擎
└── docs/           配置与接口文档
```

---

## 设置页结构

主设置页入口：

| 入口         | 内容                                       |
|------------|------------------------------------------|
| 播放器      | 播放相关参数（预加载、解码等）                 |
| 弹幕        | 弹幕开关与加载设置                           |
| 字幕设置    | 字幕样式、加载、偏移等（0.0.1 新增）           |
| **个性设置** | **图片尺寸、无痕模式**（0.0.1 新增）          |
| 外观与语言  | 语言切换、主题色（0.0.1 新增）                 |
| 主题色 / DoH / 缓存 / 备份恢复 / 版本 | 主设置页内直接操作    |

---

## 版本规则

- `versionName`：`0.0.1` 起步，发布新版本时手动递增（如 `0.0.2`、`0.1.0`）
- `versionCode`：构建时自动取 **git 提交数**（`git rev-list --count HEAD`），每次提交 / 更新自动变化，无需维护
- 因此**本地与 CI 打包同一提交的 APK 完全一致**，新版本可直接覆盖安装

---

## 打包与签名

### 签名（无需重新签名）

`app/ys.jks` 为内置签名密钥，仓库内自带，本地与 CI 打包均使用同一签名：

- 打包产物**已签名、可直接安装**，无需重新签名
- 新版本可直接覆盖升级，无需卸载重装

> 如需换成自己的密钥，在 `local.properties` 中配置 `storeFile` / `keyAlias` / `storePassword` / `keyPassword` 即可覆盖内置签名。

### 本地打包

```bash
# 需先在 local.properties 配置 sdk.dir（该文件已不再入库，需自行创建）
./gradlew assembleMobileArm64_v8aRelease      # 手机版 arm64
./gradlew assembleMobileArmeabi_v7aRelease     # 手机版 armv7
./gradlew assembleLeanbackArm64_v8aRelease     # 电视版 arm64
./gradlew assembleLeanbackArmeabi_v7aRelease   # 电视版 armv7
```

产物位于 `app/build/outputs/apk/`，文件名格式 `Ys影视-{mode}-{abi}.apk`。

### CI 打包

推送到 `trae` 分支自动触发 [trae.yml](.github/workflows/trae.yml) 工作流：4 个 ABI 全量打包，直接发布到 GitHub Release（预发布），产物为裸 APK，无需解压。

---

## 配置说明

Vod 配置为应用主入口，通过 URL 或本地路径加载，顶层字段：

- 点播站点（`sites`）、解析规则（`parses`）
- 直播来源（`lives`）
- 网络设置（`doh`、`proxy`、`hosts`、`ads`）

---

## 延伸阅读

| 文件                          | 说明                   |
|-----------------------------|----------------------|
| [CONFIG.md](docs/CONFIG.md) | Vod / Live 完整配置字段说明  |
| [SPIDER.md](docs/SPIDER.md) | Spider 所有方法规格与返回格式   |
| [LOCAL.md](docs/LOCAL.md)   | 本地 HTTP API 所有端点完整说明 |
| [LIVE.md](docs/LIVE.md)     | 直播来源格式完整说明           |
