  Сборка и запуск программы
Для корректной работы, необходимо наличие Docker с встроенной Docker Compose.
На проекте используется Java 21.
Убедись, что выбранные порты не заняты
  
1. Клонирование репозитория и переход в папку docker
```bash
git clone https://github.com/NadezhdaShpak/hibernate-final
cd hibernate-final/docker
```
3. Запуск приложения происходит с помощью файла Compose.yaml
Происходит Запуск MySQL сервера на порту 3305:3306, Redis сервера на порту 6379:6379 и проекта как докер-контейнеров
MYSQL_ROOT_PASSWORD=root (можно изменить в файле docker/Dockerfile)
```bash
docker compose up --build --exit-code-from benchmark
```   

Тесты прогоняются с помощью Benchmark. После окончания работы программы, все контейнеры останавливаются.

