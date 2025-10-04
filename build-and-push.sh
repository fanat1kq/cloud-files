#!/bin/bash

# Переменные
DOCKER_USERNAME="ваш-username"
IMAGE_NAME="filestorageapp"
VERSION="1.0.0"

echo "1. Сборка Docker образа..."
docker build -t $DOCKER_USERNAME/$IMAGE_NAME:$VERSION -t $DOCKER_USERNAME/$IMAGE_NAME:latest .

echo "2. Логин в Docker Hub..."
docker login

echo "3. Загрузка образа на Docker Hub..."
docker push $DOCKER_USERNAME/$IMAGE_NAME:$VERSION
docker push $DOCKER_USERNAME/$IMAGE_NAME:latest

echo "4. Готово! Образ доступен по ссылке:"
echo "https://hub.docker.com/r/$DOCKER_USERNAME/$IMAGE_NAME"