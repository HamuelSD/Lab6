# /end2end-tests/Dockerfile

# 1. Use the official Maven image to compile and run tests
# Because of container "Layering" this also brings in Ubuntu Linux
FROM maven:3.9.14-eclipse-temurin-25

# 2. Install Chromium browser (required for Selenium to click around)
# apt-get works because the full Ubuntu Linux environment the FROM statement imports
RUN apt-get update && apt-get install -y wget

RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
        && apt-get install -y ./google-chrome-stable_current_amd64.deb \
        && rm google-chrome-stable_current_amd64.deb

# 3. Set the working directory
WORKDIR /tests

# 4. Copy your Maven file and test source code
COPY pom.xml .
COPY src ./src

# 5. When the container starts, run the Maven test phase
CMD ["mvn", "clean", "test"]