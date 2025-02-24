#Добавляем образ OpenJDK 17
FROM openjdk:17-jdk-slim

#Устанавливаем рабочую директорию в контейнере (Images)
WORKDIR /app

#Указываем путь к Jar-файлу нашего проекта
COPY target/Application_Preproject_5.1-0.0.1-SNAPSHOT.jar application.jar

#Укажем порт
EXPOSE 8080

#Запуск приложения
ENTRYPOINT ["java", "-jar", "application.jar"]

