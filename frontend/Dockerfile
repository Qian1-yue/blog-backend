FROM node:22-alpine AS builder

WORKDIR /workspace

RUN corepack enable && corepack prepare pnpm@11.19.0 --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,id=pnpm-store,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

FROM caddy:2-alpine

COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=builder /workspace/dist /srv

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -O - http://localhost:8081/healthz | grep -q 'ok' || exit 1
