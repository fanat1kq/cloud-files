# CloudStorage

Веб-приложение для облачного хранения файлов.

## 📋 ТЗ проекта
**Техническое задание:** https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/

## 🎨 Frontend
**Готовый фронтенд:** https://github.com/zhukovsd/cloud-storage-frontend/

## 🚀 Основные возможности

- **Загрузка файлов** с проверкой размера и доступного места
- **Управление директориями** (создание, навигация)
- **Поиск файлов** по имени
- **Скачивание** отдельных файлов и архивов папок
- **Перемещение ресурсов** между папками
- **Аутентификация и авторизация** пользователей
- **Защита от переполнения диска** с резервированием места

## 🏗️ Архитектура

### Backend
- **Java 21** + **Spring Boot 3**
- **Spring Security** для аутентификации
- **MinIO** в качестве объектного хранилища
- **PostgreSQL** для метаданных
- **Redis** для сессий
- **Liquibase** для миграций БД

### Frontend
- **React**




## ⚙️ Настройка и запуск



1. **Клонируйте репозиторий:**
```bash
git clone https://github.com/fanat1kq/cloud-files.git
cd cloud-files
```

2. **Запустите инфраструктуру:**
```bash
1 Заполнить .env.dev или env.docker в соответствии с вашими желаемым профилем по примеру env.example
2 
 -  Если своя БД и приложение через идею
docker-compose -f docker-compose.dev.yml up -d
 -  Если все через docker
docker-compose -f docker-compose.dev.yml up -d
```


Приложение будет доступно по адресу: `http://{HOST}`

## 📝 API Endpoints

### Аутентификация
- `POST /api/auth/sign-up` - Регистрация
- `POST /api/auth/sign-in` - Вход
- `POST /api/auth/sign-out` - Выход
- `GET /api/user/me` - Текущий пользователь

### Управление файлами
- `GET /api/directory` - Содержимое папки
- `POST /api/directory` - Создать папку
- `GET /api/resource` - Информация о файле
- `POST /api/resource` - Загрузить файл(ы)
- `DELETE /api/resource` - Удалить ресурс
- `GET /api/resource/move` - Переместить ресурс
- `GET /api/resource/download` - Скачать файл/папку
- `GET /api/resource/search` - Поиск файлов

Деплой [176.109.106.218:8082](http://176.109.106.218:8082)