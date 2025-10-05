# AI Assistant Telegram Bot (ai-assistant) 

Ассистент на **Kotlin + Spring Boot (WebFlux)**, работающий через Telegram **Webhook**.  
Деплой на VPS с **Docker + Nginx (TLS)**. Вариант без домена — используем самоподписанный сертификат.

## 📦 Стек
- Kotlin + Spring Boot 3.x (WebFlux)
- Docker + Docker Compose
- Nginx (reverse proxy + TLS)
- Telegram Bot API
- OpenSSL (генерация самоподписанных сертификатов)

## 🚀 Быстрый старт

## Для локального запуска

### Клонировать репозиторий

```bash

cd /opt
git clone <URL_ТВОЕГО_REPO> ai-assistant
cd ai-assistant
```

### Сборка: 
``./gradlew clean build``

### Запуск (локально):
``java -jar build/libs/ai-assistant-0.0.1-SNAPSHOT.jar``

Чтоб остнаноить узнай PID процесса:
```bash

ps -ef | grep ai-assistant
Заверши процесс
kill <PID>
или
kill -9 <PID>

```

### прокинуть тунель при помощи localtunnel
```bash

brew install localtunnel
lt --port 8080  
```
out example: ``your url is: https://better-swans-fly.loca.lt``

Удалить предыдущий Webhook и добавить новый:
```bash

curl -X POST "https://api.telegram.org/bot$TG_BOT_TOKEN/deleteWebhook"

curl -X POST "https://api.telegram.org/bot$TG_BOT_TOKEN/setWebhook" \
  -d "secret_token=${TG_WEBHOOK_SECRET}" \
  -d "url=https://ninety-lies-punch.loca.lt/tg/webhook"
```
где /tg/webhook это uri контроллера который будет принимать сообщения от тг-бота


## Для запуска на сервере

### 0) Подготовка сервера

```bash 

ssh root@ip/host
apt update && apt upgrade -y

# 0) удалить дистрибутивный docker.io, если стоял
sudo apt-get remove -y docker.io docker-doc docker-compose
sudo apt-get purge -y docker.io
sudo apt-get autoremove -y
# данные в /var/lib/docker останутся (не трогаем)

# 1) подготовка ключа и репозитория Docker
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu jammy stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update

# 2) установка актуальных пакетов Docker + compose v2
sudo apt-get install -y docker-ce docker-ce-cli containerd.io \
  docker-buildx-plugin docker-compose-plugin

# 3) автозапуск и проверка демона
sudo systemctl enable --now docker
sudo systemctl status docker --no-pager

# 4) проверить версии
docker --version
docker compose version

# выйди и зайди в сессию (или: newgrp docker), затем проверь:
docker run --rm hello-world
```

### Открой порты:
```
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw enable
```

### Клонировать репозиторий

```bash

cd /opt
git clone <URL_ТВОЕГО_REPO> ai-assistant
cd ai-assistant
```

В проекте надо добавить:
- Dockerfile
- docker-compose.yaml
- deploy/nginx/nginx.conf

### Сгенерируем самоподписанный cert (на 1 год; CN — публичный IP):
```bash

mkdir -p deploy/nginx/certs
openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 365 \
-keyout deploy/nginx/certs/server.key \
-out deploy/nginx/certs/server.crt \
-subj "/CN=$IP"

```

### Сгенерируй и вставь секрет:
```bash

TG_WEBHOOK_SECRET=$(openssl rand -hex 16)
```

### Создадим файл переменных .env (НЕ коммитим):
```
cat > .env << 'EOF'
TZ=Europe/Moscow
TG_BOT_TOKEN=REPLACE_WITH_YOUR_TOKEN
# Секрет для заголовка X-Telegram-Bot-Api-Secret-Token
TG_WEBHOOK_SECRET=
EOF
```

### Сборка и запуск
```bash 

docker compose build --no-cache
docker compose build
docker compose up -d
docker compose ps

curl -k https://<IP>/healthz   
```

### Установка вебхука c загрузкой сертификата
```bash

curl -s -X POST "https://api.telegram.org/bot${TG_BOT_TOKEN}/deleteWebhook"

curl -s "https://api.telegram.org/bot${TG_BOT_TOKEN}/getWebhookInfo"

source .env
curl -s -F "url=https://79.143.31.222/tg/webhook" \
       -F "secret_token=${TG_WEBHOOK_SECRET}" \
       -F "certificate=@deploy/nginx/certs/server.crt" \
       "https://api.telegram.org/bot${TG_BOT_TOKEN}/setWebhook"

curl -s "https://api.telegram.org/bot${TG_BOT_TOKEN}/getWebhookInfo"

docker logs --tail 200 ai-assistant-app
docker logs --tail 200 ai-assistant-nginx

```



Быстрая верификация вне кода 
Проверь токен/формат полей простым запросом (должно прийти 200 OK):
```bash

curl -sS -X POST "https://api.telegram.org/bot$TG_BOT_TOKEN/sendMessage" \
  -H "Content-Type: application/json" \
  -d '{"chat_id": <CHAT_ID>, "text": "ping", "parse_mode": "HTML"}'
```



## БД

### Создать изолированную сеть

```bash

docker network create app-net
# 51bb17b7d0ba3d6ef4d9b43b1cc4956e8009cdfe1704340869fb5a583e8f3d3f
docker network ls | grep app-net

```

### Создать тома для данных и бэкапов

```bash

docker volume create pgdata
docker volume create pgbackups
docker volume ls | grep pg

```

### Подготовить файл с секретами (в домашней/рабочей директории)

```bash

cd /opt/ai-assistant
printf "POSTGRES_USER=app\nPOSTGRES_PASSWORD=СЛОЖНЫЙ_ПАРОЛЬ\nPOSTGRES_DB=ai_assistant\nTZ=Europe/Moscow\n" > .env
chmod 600 .env
source .env
```

### Запустить PostgreSQL 16 в app-net без публикации порта
```bash

docker run -d --name postgres16 \
  --env-file /opt/ai-assistant/.env \
  --network ai-assistant_app-net \
  -v pgdata:/var/lib/postgresql/data \
  -v pgbackups:/backups \
  -e POSTGRES_INITDB_ARGS="--encoding=UTF8 --locale-provider=icu --icu-locale=ru-RU" \
  --health-cmd="pg_isready -U $POSTGRES_USER -d $POSTGRES_DB || exit 1" \
  --health-interval=10s --health-timeout=5s --health-retries=5 \
  postgres:16
  
docker logs --tail 100 postgres16

docker rm -f postgres16
docker volume rm pgdata
docker volume create pgdata


docker network ls
docker volume ls 
docker volume rm pgdata
docker network rm ai-assistant_default


```

### Проверка статуса контейнера 
```bash

docker ps --filter "name=postgres16"
docker logs -f postgres16 | sed -n '1,200p'
```