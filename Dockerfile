FROM kbase/kb_jre:latest as build
RUN apt-get -y update && apt-get -y install ant git openjdk-8-jdk make
RUN cd / && git clone https://github.com/kbase/jars

COPY . /tmp/data_import_export

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

RUN cd /tmp && ln -s /jars && \
  cd /tmp/data_import_export && \
  ant war

#ADD . /src
#RUN cd /src && ant build
#RUN find /src

FROM kbase/kb_jre:latest

# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH=develop

COPY deployment/ /kb/deployment/
COPY --from=build /tmp/data_import_export/jettybase/ /kb/deployment/jettybase/
COPY --from=build /tmp/data_import_export/dist/KBaseDataImport.war /kb/deployment/jettybase/webapps/root.war

# The BUILD_DATE value seem to bust the docker cache when the timestamp changes, move to
# the end
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/kbase/data_import_export.git" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.schema-version="1.0.0-rc1" \
      us.kbase.vcs-branch=$BRANCH \
      maintainer="Steve Chan sychan@lbl.gov"

WORKDIR /kb/deployment/jettybase
ENV KB_DEPLOYMENT_CONFIG=/kb/deployment/conf/deployment.cfg
ENV PATH=/bin:/usr/bin:/kb/deployment/bin
ENV JETTY_HOME=/usr/local/jetty

RUN mkdir -p /kb/deployment/jettybase/logs

RUN chmod -R a+rwx /kb/deployment/conf /kb/deployment/jettybase/
ENTRYPOINT [ "/kb/deployment/bin/dockerize" ]

# Here are some default params passed to dockerize. They would typically
# be overidden by docker-compose at startup
CMD [  "-template", "/kb/deployment/conf/.templates/deployment.cfg.templ:/kb/deployment/conf/deployment.cfg", \
       "java", "-DSTOP.PORT=8079", "-DSTOP.KEY=foo", "-Djetty.home=/usr/local/jetty", \
       "-jar", "/usr/local/jetty/start.jar" ]

