  Сборка и запуск необходимых образов
Для корректной работы, необходимо наличие Docker с встроенной Docker Compose.
Для проеута используется Java 21.
Убедись, что выбранные порты не заняты
  
1. Клонирование репозитория и переход в папку docker
```bash
git clone https://github.com/NadezhdaShpak/hibernate-final
cd hibernate-final\docker
```
3. Запуск MySQL сервер на порту 3305:3306, Redis сервер на порту 6379:6379 и проект как докер-контейнеры
MYSQL_ROOT_PASSWORD=root (можно изменить в файле docker/Dockerfile)
```bash
docker compose up --build -d
```   
Запуск приложения происходит с помощью файла Compose.yaml
При первом запуске сборка может занять время.

После успешного выполнения команды логи можно найти в контейнере hibernate-final,
а так же в директории docker/logs/hibernate-final.log
