# raziel

![MBARI logo](src/docs/images/logo-mbari-3b.png)

Raziel is a configuration server, the keeper of secrets. It can be used to lookup the configuration of a M3/VARS installation.

## Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

## Useful Commands

1. `stage` - Build runnable project in `target/universal`
2. `Universal / packageBin` - Build zip files of runnable project in `target/universal`
3. `laikaSite` - Build documentation, including API docs to `target/docs/site`
4. `compile` then `scalafmtAll` - Will convert all syntax to new-style, indent based Scala 3.
5. `Docker / stage` - Create a Dockerfile for review in `target/docker/stage`
6. `Docker / publishLocal` and `Docker / publish` for pushing docker images.

## Libraries

- [circe](https://circe.github.io/circe/) for JSON handling
- [Methanol](https://github.com/mizosoft/methanol) with [Java's HttpClient](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpClient.html) for HTTP client
- [munit](https://github.com/scalameta/munit) for testing
- [picocli](https://picocli.info/) for command line arg parsing
- [Scalatra](https://scalatra.org/) - Microservice framework
- [slf4j](http://www.slf4j.org/) with [logback](http://logback.qos.ch/) for logging
- [ZIO](https://zio.dev/) for effects

## Notes

Documentation can be added as markdown files in `docs` and will be included automatically when you run `laikaSite`.
