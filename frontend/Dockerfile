# syntax=docker/dockerfile:1.6

# ── Build stage ─────────────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# Copy lockfile first for deterministic `npm ci` (will fail loudly if
# package.json and package-lock.json are out of sync, which is what we want).
COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

# ── Serve stage ─────────────────────────────────────────────────────────────
FROM nginx:alpine

# curl is needed for the HEALTHCHECK below; nginx:alpine ships with wget but
# not curl. Use wget for consistency with the rest of the platform.
RUN apk add --no-cache wget

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
