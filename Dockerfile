FROM gradle:7.6-jdk17 AS build
WORKDIR /StickerBasketball
COPY build.gradle .
COPY src ./src
RUN gradle build -x test --no-daemon

FROM openjdk:17
COPY --from=build /StickerBasketball/build/libs/*.jar /StickerBasketball/
CMD ["java", "-jar", "/StickerBasketball/StickerBasketball-0.0.1-SNAPSHOT.jar"]