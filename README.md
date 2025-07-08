# Client Service - Banking Client Management System

## Описание

Client Service - это веб-приложение для управления клиентами банка, разработанное на Java 21 с использованием Spring Boot 3.5.3. Приложение предоставляет веб-интерфейс и REST API для создания, чтения, обновления и удаления клиентских данных.

## Основные возможности

- **Управление клиентами**: CRUD операции для клиентских данных
- **Веб-интерфейс**: Современный UI на Thymeleaf с Bootstrap 5
- **REST API**: Полноценный API для интеграции с внешними системами
- **Мониторинг**: Встроенная система мониторинга с real-time метриками
- **Health Check**: Проверка состояния сервиса и всех компонентов
- **Работа без БД**: Приложение может работать в ограниченном режиме без подключения к БД

## Технологический стек

- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Data JPA / Hibernate**
- **PostgreSQL 17**
- **Thymeleaf**
- **Bootstrap 5**
- **Maven**
- **Apache Tomcat 10**

## Требования

- JDK 21 или выше
- Maven 3.8+
- PostgreSQL 17
- Apache Tomcat 10 (для deployment)

## Структура проекта

```
src/
├── main/
│   ├── java/com/bank/clientservice/
│   │   ├── config/          # Конфигурационные классы
│   │   ├── controller/      # REST и MVC контроллеры
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA сущности
│   │   ├── exception/       # Обработка исключений
│   │   ├── filter/          # Servlet фильтры
│   │   ├── mapper/          # MapStruct маппреры
│   │   ├── repository/      # JPA репозитории
│   │   └── service/         # Бизнес-логика
│   └── resources/
│       ├── templates/       # Thymeleaf шаблоны
│       ├── static/          # Статические ресурсы
│       └── application.properties
└── test/
```

## Установка и настройка

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd clientservice
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE clientdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    CONNECTION LIMIT = -1;
```

### 3. Конфигурация приложения

Отредактируйте `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/clientdb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 4. Сборка приложения

```bash
mvn clean package
```

Это создаст WAR файл в директории `target/clientservice.war`

## Запуск приложения

### Локальный запуск (для разработки)

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: http://localhost:8080/clientservice

### Деплой в Tomcat

1. Убедитесь, что Tomcat 10 установлен и запущен
2. Скопируйте `target/clientservice.war` в директорию `webapps` Tomcat
3. Приложение будет доступно по адресу: http://your-server:8080/clientservice

## API Endpoints

### Клиенты

- `GET /api/v1/clients` - Получить список всех клиентов
- `GET /api/v1/clients/{id}` - Получить клиента по ID
- `GET /api/v1/clients/by-number/{clientNumber}` - Получить клиента по номеру
- `POST /api/v1/clients` - Создать нового клиента
- `PUT /api/v1/clients/{id}` - Обновить данные клиента
- `DELETE /api/v1/clients/{id}` - Удалить клиента
- `GET /api/v1/clients/countries` - Получить список стран
- `GET /api/v1/clients/health` - Health check

### Мониторинг

- `GET /api/v1/monitoring/system-info` - Информация о системе
- `GET /api/v1/monitoring/health-history` - История health checks
- `GET /api/v1/monitoring/metrics/summary` - Сводка метрик
- `GET /api/v1/monitoring/alerts` - Активные алерты

## Веб-интерфейс

- `/clients` - Список клиентов
- `/clients/new` - Создание нового клиента
- `/clients/{id}` - Просмотр информации о клиенте
- `/clients/{id}/edit` - Редактирование клиента
- `/clients/health` - Health check страница
- `/monitoring/dashboard` - Dashboard мониторинга

## Поля клиента

- **Фамилия** (обязательное)
- **Имя** (обязательное)
- **Отчество**
- **Дата рождения** (обязательное)
- **Номер клиента** (генерируется автоматически)
- **Гражданство** (обязательное, выбор из списка)

## Работа без базы данных

Приложение может работать в ограниченном режиме без подключения к базе данных:

- Health check будет показывать статус "DEGRADED"
- Веб-интерфейс отобразит предупреждение о недоступности БД
- API endpoints вернут HTTP 503 Service Unavailable
- Мониторинг продолжит работать, но без сохранения метрик

## Мониторинг и метрики

Система мониторинга собирает следующие метрики:

- **CPU использование** (процесс и система)
- **Память** (heap usage, total, free)
- **Потоки** (активные, пиковые, daemon)
- **Garbage Collection** статистика
- **API метрики** (количество запросов, время отклика)
- **Бизнес-метрики** (новые клиенты, операции)

### Real-time обновления

Dashboard поддерживает real-time обновления через Server-Sent Events (SSE).

## Безопасность

- Приложение не требует авторизации (по ТЗ)
- Все входные данные валидируются
- SQL-инъекции предотвращаются через JPA
- XSS защита через Thymeleaf

## Логирование

Приложение использует SLF4J с Logback для логирования. Уровни логирования:

- `DEBUG` - для com.bank.clientservice
- `INFO` - для Spring Framework
- `WARN` - для предупреждений
- `ERROR` - для ошибок

## Troubleshooting

### База данных недоступна

Если приложение не может подключиться к БД:
1. Проверьте, что PostgreSQL запущен
2. Проверьте настройки подключения в application.properties
3. Убедитесь, что порт 5432 не заблокирован firewall
4. Проверьте логи для деталей ошибки

### Out of Memory

Увеличьте heap size при запуске:
```bash
java -Xmx2g -Xms1g -jar clientservice.war
```

### Медленная работа

1. Проверьте метрики CPU и памяти в dashboard
2. Проверьте количество активных подключений к БД
3. Включите SQL логирование для анализа запросов

## Контакты

Для вопросов и поддержки обращайтесь к команде разработки.

## Лицензия

Proprietary - Bank Internal Use Only