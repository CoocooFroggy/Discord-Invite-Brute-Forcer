FROM gradle:7.4.2-jdk17 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle $APP_HOME
  
COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

COPY . .
RUN gradle shadowJar

FROM eclipse-temurin:17
ENV ARTIFACT_NAME='Invite-Brute-Forcer-1.0-all.jar'
ENV APP_HOME=/app/
    
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
WORKDIR $APP_HOME

ENTRYPOINT ["java", "-jar", "Invite-Brute-Forcer-1.0-all.jar"]
