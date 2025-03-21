FROM mysql:8.0
ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=world
ENV MYSQL_USER=user
ENV MYSQL_PASSWORD=pass
COPY dump-hibernate-final.sql /docker-entrypoint-initdb.d/
RUN chmod 644 /docker-entrypoint-initdb.d/dump-hibernate-final.sql
