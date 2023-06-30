FROM openjdk:17
WORKDIR /src
COPY target/SkypeTrashMusicBot.jar SkypeTrashMusicBot.jar
CMD ["java", "-jar", "SkypeTrashMusicBot.jar"]