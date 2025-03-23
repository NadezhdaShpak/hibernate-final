  Сборка и запуск необходимых образов:
Запуск MySQL сервер как докер-контейнер и разворачивание дампа с помощью Dockerfile
  docker build -t mysql-with-dump . ; docker run -d --name mysql-container -p 3305:3306 mysql-with-dump 
Запустить Redis сервер как докер-контейнер
  docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest 

  Сборка приложения
Перед запуском приложения выполни его сборку. Для генерации JAR-файла воспользуйся Maven, запустив команду:
  mvn clean install
После успешного выполнения команды собранный JAR-файл можно найти в директории target/
