FROM bitnami/java:18 AS jre-18-build

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules \
          java.base,java.compiler,java.desktop,java.management,java.naming,java.rmi,java.scripting,java.sql,java.logging,jdk.unsupported \
#         --strip-debug \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

FROM bitnami/minideb

RUN mkdir -p /opt/app/lib \
    && useradd --no-create-home fogserver \
    && chown -R 1000:1000 /opt/app \
    && mkdir /logs \
    && chown -R 1000:1000 /logs

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"

COPY --from=jre-18-build /javaruntime $JAVA_HOME

#USER sensor-proxy

CMD [ \
     "java"\
#    ,"--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"\
    ,"-jar", "/opt/app/server.jar"\
#    ,"-Dio.netty.tryReflectionSetAccessible=true"\
    ]

COPY target/lib /opt/app/lib
COPY target/server*.jar /opt/app/server.jar
