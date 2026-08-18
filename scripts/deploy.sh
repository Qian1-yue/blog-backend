#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEPLOY_BRANCH="${DEPLOY_BRANCH:-master}"

cd "$APP_DIR"

if [[ ! -d .git ]]; then
  echo "错误：$APP_DIR 不是 Git 仓库。" >&2
  exit 1
fi

if [[ ! -f .env ]]; then
  echo "错误：缺少 $APP_DIR/.env，停止部署以避免使用空密码或错误配置。" >&2
  exit 1
fi

if [[ -n "$(git status --porcelain --untracked-files=no)" ]]; then
  echo "错误：服务器存在未提交的代码修改，请先处理后再部署。" >&2
  git status --short
  exit 1
fi

echo "[1/4] 拉取 origin/$DEPLOY_BRANCH"
git fetch --prune origin
git checkout "$DEPLOY_BRANCH"
git pull --ff-only origin "$DEPLOY_BRANCH"

echo "[2/4] 校验 Docker Compose 配置"
sudo docker compose config --quiet

echo "[3/4] 构建并更新容器"
sudo docker compose up -d --build --remove-orphans

echo "[4/4] 显示服务状态"
sudo docker compose ps

echo "部署完成。若需排查，请运行：sudo docker compose logs --tail=200 app web"
