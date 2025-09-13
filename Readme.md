### Для локального запуска нужно прокинуть тунель при помощи 

```ssh

brew install localtunnel

./gradlew clean build

java -jar build/libs/ai-tg-assistant-0.0.1-SNAPSHOT.jar

lt --port 8080  

curl -X POST "https://api.telegram.org/bot$1/deleteWebhook"

curl "https://api.telegram.org/bot$1/getWebhookInfo"

curl -X POST "https://api.telegram.org/bot$1/setWebhook" \ 
  -d "url=https://*.loca.lt/tg/webhook"


Чтоб остнаноить узнай PID процесса
ps -ef | grep ai-assistant
Заверши процесс
kill <PID>
или
kill -9 <PID>


```



```
1) Быстрая верификация вне кода

Проверь токен/формат полей простым запросом (должно прийти 200 OK):

curl -sS -X POST "https://api.telegram.org/bot<TOKEN>/sendMessage" \
  -H "Content-Type: application/json" \
  -d '{"chat_id": <CHAT_ID>, "text": "ping", "parse_mode": "HTML"}'
```


### 0) Подготовка сервера

```
ssh root@
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

# 5) чтобы не писать sudo
sudo usermod -aG docker $USER
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

### Склонируй свой репозиторий:
```
cd /opt
git clone https://github.com/nikolazmailz/ai-assistant.git
```

### Сгенерируем самоподписанный cert (на 1 год; CN — публичный IP):
```declarative

mkdir -p deploy/nginx/certs
openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 365 \
-keyout deploy/nginx/certs/server.key \
-out deploy/nginx/certs/server.crt \
-subj "/CN=79.143.31.222"

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
### Сгенерируй и вставь секрет:
```
SECRET=$(openssl rand -hex 16)
sed -i "s|TG_WEBHOOK_SECRET=|TG_WEBHOOK_SECRET=$SECRET|g" .env
```

### Генерим самоподписанный сертификат
```
IP="<ПУБЛИЧНЫЙ_IP_СЕРВЕРА>"
openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 365 \
  -keyout deploy/nginx/certs/server.key \
  -out deploy/nginx/certs/server.crt \
  -subj "/CN=${IP}"
```

### Сборка и запуск
```
docker compose build
docker compose up -d
docker compose ps

curl -k https://<IP>/healthz   
```

### Установка вебхука c загрузкой сертификата
```
curl -s -X POST "https://api.telegram.org/bot${TG_BOT_TOKEN}/deleteWebhook"

curl -s "https://api.telegram.org/bot${TG_BOT_TOKEN}/getWebhookInfo"

source .env
curl -s -F "url=https://79.143.31.222/tg/webhook" \
       -F "secret_token=${TG_WEBHOOK_SECRET}" \
       -F "certificate=@deploy/nginx/certs/server.crt" \
       "https://api.telegram.org/bot${TG_BOT_TOKEN}/setWebhook"

curl -s "https://api.telegram.org/bot${TG_BOT_TOKEN}/getWebhookInfo"

 -F "secret_token=${TG_WEBHOOK_SECRET}" \
```
