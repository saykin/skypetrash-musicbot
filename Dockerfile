FROM openjdk:17
ARG API_TOKEN
ENV API_TOKEN=$API_TOKEN
ARG YOUTUBE_TOKEN
ENV YOUTUBE_TOKEN=$YOUTUBE_TOKEN
WORKDIR /src
COPY target/SkypeTrashMusicBot.jar SkypeTrashMusicBot.jar
CMD ["java", "-jar", "SkypeTrashMusicBot.jar"]