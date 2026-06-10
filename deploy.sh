#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

FRONTEND_REPO="https://github.com/528085390/ugc-fronted.git"
FRONTEND_DIR="$ROOT_DIR/../bbs-frontend"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ---------- 1. Check & Install Docker ----------
install_docker() {
    log_info "Installing Docker..."
    curl -fsSL https://get.docker.com | bash -s docker
    sudo systemctl enable docker
    sudo systemctl start docker
}

if ! command -v docker &>/dev/null; then
    install_docker
else
    log_info "Docker already installed: $(docker --version)"
fi

# ---------- 2. Check & Install Docker Compose ----------
if ! docker compose version &>/dev/null; then
    log_info "Installing Docker Compose..."
    sudo curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi
log_info "Docker Compose: $(docker compose version)"

# ---------- 3. Configure Docker Mirror (China) ----------
if [ ! -f /etc/docker/daemon.json ]; then
    log_info "Configuring Docker mirror..."
    sudo mkdir -p /etc/docker
    cat <<'EOF' | sudo tee /etc/docker/daemon.json >/dev/null
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com",
    "https://docker.nju.edu.cn"
  ]
}
EOF
    sudo systemctl daemon-reload
    sudo systemctl restart docker
    log_info "Docker mirror configured, daemon restarted."
else
    log_info "Docker daemon.json already exists, skip mirror config."
fi

# ---------- 4. Clone / pull frontend repo ----------
if [ ! -d "$FRONTEND_DIR" ]; then
    log_info "Cloning frontend repo..."
    git clone "$FRONTEND_REPO" "$FRONTEND_DIR"
else
    log_info "Updating frontend repo..."
    cd "$FRONTEND_DIR" && git pull && cd "$ROOT_DIR"
fi

# ---------- 5. Check Java ----------
if ! command -v java &>/dev/null; then
    log_error "Java not found. Installing Java 17..."
    sudo apt-get update -qq && sudo apt-get install -y -qq openjdk-17-jdk
fi

java_version=$(java -version 2>&1 | head -1 | grep -oP '\d+' | head -1)
log_info "Java version: $(java -version 2>&1 | head -1)"

# ---------- 6. Build JARs ----------
log_info "Building all services with Maven..."
chmod +x mvnw
./mvnw clean package -DskipTests -q

# Verify JARs exist and are executable
log_info "Verifying JARs..."
SERVICES=("gateway" "auth-service" "user-service" "permission-service" "section-service" "post-service" "comment-service" "interaction-service" "notification-service" "file-service" "search-service")
for svc in "${SERVICES[@]}"; do
    jar_file=$(find "bbs-$svc/target" -maxdepth 1 -name "*.jar" ! -name "*.jar.original" 2>/dev/null | head -1)
    if [ -z "$jar_file" ]; then
        log_error "No JAR found for bbs-$svc. Build may have failed."
        exit 1
    fi
    if ! unzip -p "$jar_file" META-INF/MANIFEST.MF 2>/dev/null | grep -q "Spring-Boot-Version"; then
        log_error "bbs-$svc JAR is not a Spring Boot executable (no Spring-Boot-Version in manifest): $jar_file"
        log_error "Try running: ./mvnw clean package -DskipTests -U"
        exit 1
    fi
    log_info "  bbs-$svc: OK ($(basename "$jar_file"))"
done

# ---------- 7. Pull base images ----------
log_info "Pre-pulling base images (with retry)..."
IMAGES=(
    "nacos/nacos-server:v2.3.2"
    "mysql:8.0"
    "redis:7.2"
    "rabbitmq:3.13-management"
    "nginx:alpine"
)

for img in "${IMAGES[@]}"; do
    for i in $(seq 1 3); do
        if docker pull "$img" >/dev/null 2>&1; then
            log_info "  Pulled: $img"
            break
        else
            log_warn "  Retry $i/3: $img"
            sleep 3
        fi
    done
done

# ---------- 8. Start all containers ----------
log_info "Starting all services via Docker Compose..."
docker compose up -d --build

# ---------- 9. Wait for health ----------
log_info "Waiting for services to be healthy..."
sleep 20

SERVICES=("nacos" "mysql" "gateway" "auth-service" "user-service" "permission-service" "section-service" "post-service" "comment-service" "interaction-service" "notification-service" "file-service" "search-service")
for svc in "${SERVICES[@]}"; do
    for i in $(seq 1 30); do
        status=$(docker inspect --format='{{.State.Health.Status}}' "bbs-$svc" 2>/dev/null || echo "starting")
        if [ "$status" = "healthy" ]; then
            log_info "  bbs-$svc: healthy"
            break
        fi
        if [ "$status" = "unhealthy" ]; then
            log_warn "  bbs-$svc: unhealthy, showing last logs:"
            docker logs --tail 10 "bbs-$svc" 2>/dev/null || true
            break
        fi
        sleep 5
    done
    [ $i -eq 30 ] && log_warn "  bbs-$svc: not healthy after 150s, continuing..."
done

# ---------- 10. Build & deploy frontend ----------
if [ -d "$FRONTEND_DIR" ]; then
    log_info "Building and deploying frontend..."
    cd "$FRONTEND_DIR"
    if ! command -v npm &>/dev/null; then
        log_info "Installing Node.js..."
        curl -fsSL https://deb.nodesource.com/setup_20.x | sudo bash -
        sudo apt-get install -y nodejs
    fi
    npm install && npm run build

    docker network inspect bbs-net >/dev/null 2>&1 || docker network create bbs-net
    docker compose -f docker-compose.yml up -d
    cd "$ROOT_DIR"
else
    log_warn "Frontend repo not found at $FRONTEND_DIR, skipping."
fi

# ---------- 11. Summary ----------
echo ""
log_info "=== Deployment Summary ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""
log_info "Nacos Console:  http://<server-ip>:8848/nacos"
log_info "API Gateway:    http://<server-ip>:8888"
log_info "Nginx:          http://<server-ip>"
echo ""
log_info "Deployment complete!"
