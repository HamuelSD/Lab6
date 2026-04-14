FROM maven:3.9.14-eclipse-temurin-25-alpine

# Install real, native Chromium and the matching driver via apk (Alpine's package manager)
RUN apk add --no-cache chromium chromium-chromedriver

# Trick Selenium into using Chromium by linking it to the google-chrome path
RUN ln -s /usr/bin/chromium /usr/bin/google-chrome

WORKDIR /tests
COPY pom.xml .
COPY src ./src

CMD ["mvn", "clean", "test"]