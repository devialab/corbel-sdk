FROM devialab/sbt-build

COPY project /app/project
COPY build.sbt /app/build.sbt
RUN sbt copyResources

COPY . /app