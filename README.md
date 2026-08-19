# Java 博客全栈项目

一个可直接容器化部署的前后端分离博客系统。后端采用 Spring Boot 4、
MyBatis-Plus、MySQL 和 Redis，前端采用 Vue 3、TypeScript 和 Element Plus。

## 项目亮点

- 使用 Spring Security 统一实现无状态 Token 认证和接口访问控制
- 用户、文章和评论均有资源所有权校验，用户注销采用软删除
- BCrypt 存储密码，登录 Token 使用 256 位安全随机数并保存在 Redis
- 使用 Flyway 管理数据库版本，空数据库会在启动时自动建表和升级
- Redis 缓存文章详情并维护热门排行；排行服务异常时可降级
- Spring Boot Actuator 提供容器存活和就绪探针
- Testcontainers 为测试自动创建临时 MySQL 8.4 和 Redis 7.4
- 多阶段 Docker 构建、非 root 后端进程、内部数据库网络和持久化卷
- Caddy 托管 Vue 单页应用、反向代理 `/api`，绑定域名后自动申请 HTTPS
- GitHub Actions 自动执行后端测试和前端生产构建

## 技术栈

- Java 17、Spring Boot 4.1、Spring Security
- MyBatis-Plus 3.5、Flyway、MySQL 8.4
- Redis 7.4、Maven、JUnit 5、Testcontainers
- Vue 3、TypeScript、Vite、Pinia、Element Plus
- Docker Compose、Caddy

## 一键启动完整系统

要求已启动 Docker Desktop（Windows/macOS）或 Docker Engine + Compose（Linux）。

```bash
cp .env.example .env
```

编辑 `.env`，至少把 `DB_PASSWORD`、`MYSQL_ROOT_PASSWORD` 和
`REDIS_PASSWORD` 改成三个不同的强密码。真实 `.env` 已被 Git 忽略。

```bash
docker compose config
docker compose up -d --build
docker compose ps
```

首次启动时 Flyway 会自动创建并升级数据库，无需手工执行 `schema.sql`。
默认访问地址为 `http://localhost`。

常用运维命令：

```bash
docker compose logs -f app web
docker compose restart app
docker compose up -d --build
docker compose down
```

`docker compose down` 不会删除数据库卷；不要随意添加 `-v`，否则会删除
MySQL、Redis 和 Caddy 的持久化数据。

## 部署到云服务器

建议使用 Ubuntu LTS，并在云厂商安全组和系统防火墙中开放 TCP 22、80、443。
服务器只需安装 Git、Docker Engine 和 Docker Compose 插件：

```bash
git clone <你的仓库地址> blog
cd blog
cp .env.example .env
# 编辑 .env 并设置强密码
docker compose up -d --build
docker compose ps
```

无域名时保留 `SITE_ADDRESS=:80`。有域名时，先把域名 A/AAAA 记录解析到
服务器公网 IP，再将 `.env` 改为：

```dotenv
SITE_ADDRESS=blog.example.com
```

重新执行 `docker compose up -d` 后，Caddy 会自动申请并续期 HTTPS 证书。
MySQL 和 Redis 没有暴露公网端口，只能从 Compose 内部网络访问。

生产容器统一使用 `Asia/Shanghai`：MySQL 默认会话时区为 `+08:00`，
Java 通过 `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Shanghai` 固定业务时区。
不要只在前端给时间加 8 小时；否则换浏览器、换部署地区或重复格式化时会再次出错。

代码通过 Git 部署后，后续更新可在服务器执行：

```bash
cd /opt/blog
bash scripts/deploy.sh
```

脚本会使用 `git pull --ff-only` 拉取 `master` 分支，校验 Compose 配置，
重新构建并更新容器。它不会删除 `.env`，也不会执行 `docker compose down -v`，
因此 MySQL、Redis 和 Caddy 的命名数据卷会继续保留。服务器目录如果存在手工修改，
脚本会停止，避免拉取代码时意外覆盖修改。

部署前应额外配置定时数据库备份，并把备份复制到服务器以外的位置。例如：

```bash
docker compose exec -T mysql sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction blog' \
  > blog-backup.sql
```

## 本地开发

本机准备 MySQL 8 和 Redis 7，创建空数据库 `blog`，然后配置环境变量。
Flyway 会自动建表。

PowerShell 示例：

```powershell
$env:DB_PASSWORD = "你的数据库密码"
$env:REDIS_PASSWORD = "你的Redis密码"
.\mvnw.cmd "-Dspring-boot.run.profiles=local" spring-boot:run
```

后端默认端口为 `18080`。`local` 配置会打印 MyBatis SQL，生产配置不会打印
SQL 和密码哈希。

前端开发：

```bash
cd frontend
corepack enable
pnpm install --frozen-lockfile
pnpm dev
```

前端开发服务器默认运行在 `http://localhost:5173`，并把 `/api` 代理到后端。

## 测试与构建

后端测试要求 Docker 正在运行；测试不会连接或修改本机业务数据库：

```powershell
.\mvnw.cmd test
```

前端检查和生产构建：

```bash
cd frontend
pnpm build
```

## 主要配置

| 环境变量 | 用途 | 默认值 |
| --- | --- | --- |
| `DB_URL` | JDBC 地址 | 本机 `blog` 数据库 |
| `DB_USERNAME` | 应用数据库用户 | `root`（Compose 中为 `blog_app`） |
| `DB_PASSWORD` | 应用数据库密码 | 必填 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 | `localhost:6379` |
| `REDIS_PASSWORD` | Redis 密码 | 本地可空，生产必填 |
| `SITE_ADDRESS` | Caddy 监听地址或域名 | `:80` |
| `JAVA_OPTS` | JVM 容器参数 | 最大使用容器内存的 75% |

## 核心接口

- `POST /api/auth/register`、`POST /api/auth/login`、`POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/articles`、`GET /api/articles`、`GET /api/articles/mine`
- `GET /api/articles/{id}`、`PUT /api/articles/{id}`、`DELETE /api/articles/{id}`
- `GET /api/articles/hot`
- `POST /api/articles/{id}/comments`、`GET /api/articles/{id}/comments`
- `GET /actuator/health/readiness`

## 仍可继续扩展

- OpenAPI/Swagger 在线接口文档
- 图片上传到对象存储、文章分类/标签、点赞与收藏
- 热门排行的时间衰减和定时重算
- 管理员角色、审计日志、限流和登录失败保护
- 云端监控告警、自动备份与滚动发布
