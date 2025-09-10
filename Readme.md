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
