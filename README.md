[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dansiviter/juli/Build?style=flat-square)](https://github.com/dansiviter/juli/actions/workflows/build.yaml) [![Known Vulnerabilities](https://snyk.io/test/github/dansiviter/juli/badge.svg?style=flat-square)](https://snyk.io/test/github/dansiviter/juli) [![Sonar Coverage](https://img.shields.io/sonar/coverage/dansiviter_juli?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/dashboard?id=dansiviter_juli) ![Maven Central](https://img.shields.io/maven-central/v/uk.dansiviter.juli/juli-project?style=flat-square) ![Java 11+](https://img.shields.io/badge/-Java%2011%2B-informational?style=flat-square)


# Java Util Logging Improver (JULI) #

Although rarely the preferred framework `java.util.logging` (JUL) is embedded into Java so aids minimal applications but is not the easiest to use. This library is here to make life a little easier:
* Minimal layer to improve:
  * Type-safety to reduce errors,
  * Improve maintenance overhead by reducing complexity,
  * Simplify unit testing (especially with CDI),
  * Reduce improve performance by deferring array initialisation and auto-boxing until actually needed,
  * Automatic unwrapping of `java.util.function.*Supplier` and `java.util.Optional` parameters.
* Asynchronous handlers to mitigate logging impact on critical path,
* Fallback handlers to mitigate loss of logs to aid debugging.


## Log Wrapper ##

The wrapper uses [Annotation Processing](https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/annotation/processing/package-summary.html) to generate an implementation that includes many logging best practices without a developer needing to worry about these.

> :information_source: [JBoss Logging Tools](https://github.com/jboss-logging/jboss-logging-tools) was the inspiration but designed to be even simpler, leaner and not dependent on JBoss Log Manager.

Lets get started... first import dependencies:

```xml
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>core</artifactId>
  <version>${juli.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>processor</artifactId>
  <version>${juli.version}</version>
  <scope>provided</scope> <!-- only needed during compilation -->
  <optional>true</optional>
</dependency>
```

Define a logger interface:
```java
package com.foo;
...

@Log
public interface MyLog {
  @Message("Hello {0}")  // <- defaults to `Info` level
  void hello(String name);

  @Message(value = "Oh no! {0}", level = Level.ERROR)  // <- 'Error' level
  void error(String name, Throwable t);  // <- Throwables must be last parameter

  @Message("Hello {0}")
  void unwrap(Supplier<String> name);  // <- value extracted on the calling thread if #isLoggable passes

  @Message("Number {0}")
  void number(int value);  // <- primitives only auto-boxed if the log level is consumed. w00t!

  @Message("Another umber {0}")
  void numberUnwrap(IntSuppler value);  // <- primitive suppliers work too!
}
```

This will generate a class `com.foo.MyLog$log` which actually does the logging. For Maven this can be seen in the `target/generated-sources/annotations/` folder for reference.

To get an instance use `uk.dansiviter.juli.LogProducer`:
```java
public class MyClass {
  private final static MyLog LOG = LogProducer.log(MyLog.class);

  public void myMethod() {
    LOG.hello("foo");
  }
}
```

> :information_source: The log levels are reduced to just `ERROR`, `WARN`, `INFO`, `DEBUG` and `TRACE` as, frankly, that's all you really need.

## CDI ##

This can perform automatic injection of dependencies via CDI:

```xml
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>cdi</artifactId>
  <version>${juli.version}</version>
</dependency>
```

```java
@ApplicationScoped
public class MyClass {
  @Inject
  private MyLog log;

  public void myMethod() {
    log.hello("foo");
  }
}
```

This will inject a `@Dependent` scoped instance of the logger to prevent proxying.

> :information_source: Testing can be done easily by injecting a mock of `MyLog` which helps validating behaviour which is especially pertinent for warning and error messages.


## Asynchronous Handlers ##

JUL handlers are all synchronous which puts IO directly within the path of execution; this is a bottleneck. To address this, this library has a very simple `uk.dansiviter.juli.AsyncHandler` implementation that uses `java.util.concurrent.Flow` to asynchronously process log events. Using this can dramatically improve the performance of 'chatty' logging at the expense of a little memory and CPU. There is only one concrete implementation as the moment which is `uk.dansiviter.juli.AsyncConsoleHandler` which can be used as a direct replacement for `java.util.logging.ConsoleHandler`.

> :warning: If the buffer saturates, then the much of the performance benefits of the asynchronous handler can be lost. However, once the pressure is reduced this will return, due to this it _should_ still outperform a synchronous implementation.

> :information_source: `com.lmax:disruptor` has been trialed as an alternative to using Java 9 `Flow` but its bulk (~90KB) and no significant out-of-the-box performance improvement is has been discounted (see [#6](../../issues/6)).


## Fallback Handler ##

Some handlers have external factors that may prevent a log record being flushed. This might include a remote log aggregator where a network failure prevents the message being delivered. To prevent loss of log records the `uk.dansiviter.juli.FallbackHandler` can be used to manage this case. For example:

```
handlers=uk.dansiviter.juli.FallbackHandler
uk.dansiviter.juli.FallbackHandler.delegate=com.acme.MyRemoteHandler
uk.dansiviter.juli.FallbackHandler.fallback=java.util.logging.ConsoleHandler
```
With the above configuration, if either `MyRemoteHandler` `delegate` cannot be created or `Handler#publish(...)` fails the `fallback` handler will be used instead. If no `fallback` is defined then `AsyncConsoleHandler` is used.
