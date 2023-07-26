[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/dansiviter/jule/deploy.yaml)](https://github.com/dansiviter/jule/actions/workflows/build.yaml) [![Known Vulnerabilities](https://snyk.io/test/github/dansiviter/jule/badge.svg?style=flat-square)](https://snyk.io/test/github/dansiviter/jule) [![Sonar Coverage](https://img.shields.io/sonar/coverage/dansiviter_jule?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/dashboard?id=dansiviter_jule) [![Maven Central](https://img.shields.io/maven-central/v/uk.dansiviter.jule/jule-project?style=flat-square)](https://search.maven.org/artifact/uk.dansiviter.jule/jule-project) ![Java 11+](https://img.shields.io/badge/-Java%2011%2B-informational?style=flat-square)


# Java Util Logging Enhancer (JULE) #

Although rarely the preferred framework `java.util.logging` (JUL) is embedded into Java so aids minimal applications but is not the easiest to use. This library is here to make life a little easier:
* Minimal (~21KB for v0.3.0) layer to improve:
  * Type-safety to reduce errors,
  * Improve maintenance overhead by reducing complexity,
  * Simplify unit testing (especially with CDI),
  * Reduce performance impact by deferring array initialisation and auto-boxing until actually needed,
  * Automatic unwrapping of `java.util.function.*Supplier` and `java.util.Optional` parameters.
* Asynchronous handlers to mitigate logging impact on critical path,
* Fallback handlers to mitigate loss of logs to aid debugging.


## Log Wrapper ##

The wrapper uses [Annotation Processing](https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/annotation/processing/package-summary.html) to generate an implementation that includes many logging best practices without a developer needing to worry about these.

> :information_source: [JBoss Logging Tools](https://github.com/jboss-logging/jboss-logging-tools) was the inspiration but designed to be even simpler, leaner and not dependent on JBoss Log Manager.

Lets get started... first import dependencies:

```xml
<dependency>
  <groupId>uk.dansiviter.jule</groupId>
  <artifactId>jule</artifactId>
  <version>${jule.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.jule</groupId>
  <artifactId>jule-processor</artifactId>
  <version>${jule.version}</version>
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
  @Message("Hello %s")  // Uses java.util.Formatter and defaults to `INFO` level
  void hello(String name);

  @Message(value = "Oh no! %s", level =.ERROR)  // <- 'ERROR' level
  void error(String name, Throwable t);  // <- Throwables must be last parameter

  @Message("Hello %s")
  void unwrap(Supplier<String> name);  // <- value extracted on the calling thread if #isLoggable passes

  @Message("Number %d")
  void number(int value);  // <- primitives only auto-boxed if the log level is consumed. w00t!

  @Message("Another number %d")
  void numberUnwrap(IntSuppler value);  // <- primitive suppliers work too!
}
```

This will generate a class `com.foo.MyLog$impl` which actually does the logging. For Maven this can be seen in the `target/generated-sources/annotations/` folder for reference.

To get an instance use `uk.dansiviter.jule.LogFactory`:
```java
public class MyClass {
  private final static MyLog LOG = LogFactory.log(MyLog.class);

  public void myMethod() {
    LOG.hello("foo");
  }
}
```

> :information_source: The log levels are reduced to just `ERROR`, `WARN`, `INFO`, `DEBUG` and `TRACE` as, frankly, that's all you really need.


## CDI ##

If you wish for CDI to manage the logger and and make it available for injection, just use:

```java
@Log(lifecycle = Lifecycle.CDI)
interface MyLog {
  ...
}
```

This will generage a `@Dependent` logger implementation.

Then just inject:
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

> :information_source: Testing can be done easily by injecting a mock of `MyLog` which helps validating behaviour which is especially pertinent for `WARN` and `ERROR` messages.


## Asynchronous Handlers ##

JUL handlers are all synchronous which puts IO directly within the path of execution; this is a bottleneck. To address this, this library has a very simple `uk.dansiviter.jule.AsyncHandler` implementation that uses `java.util.concurrent.Flow` to asynchronously process log events. Using this can significantly improve the execution performance of methods that have 'chatty' logging at the expense of a little memory and CPU. There is only one concrete implementation as the moment which is `uk.dansiviter.jule.AsyncConsoleHandler` which can be used as a direct replacement for `java.util.logging.ConsoleHandler`.

> :warning: If the buffer saturates, then the much of the performance benefits of the asynchronous handler can be lost. However, once the back-pressure is reduced this will return, due to this it _should_ still outperform a synchronous implementation.

> :information_source: `com.lmax:disruptor` has been trialed as an alternative to using Java 9 `Flow` but its bulk (~90KB) and no significant out-of-the-box performance improvement it has been discounted (see [#6](../../issues/6)).


## Fallback Handler ##

Some handlers have external factors that may prevent a log record being flushed. This might include a remote log aggregator where a network failure prevents the message being delivered. To prevent loss of log records the `uk.dansiviter.jule.FallbackHandler` can be used to manage this case. For example:

```
handlers=uk.dansiviter.jule.FallbackHandler
uk.dansiviter.jule.FallbackHandler.delegate=com.acme.MyRemoteHandler
uk.dansiviter.jule.FallbackHandler.fallback=java.util.logging.ConsoleHandler
```
With the above configuration, if either `MyRemoteHandler` `delegate` cannot be created or `Handler#publish(...)` fails the `fallback` handler will be used instead. If no `fallback` is defined then `AsyncConsoleHandler` is used.

## FAQ ##

### Does this work in an IDE? ###

This uses vanilla Annotation Processing so there is no reason for it not to work. It's known to work fine with VSCode (with Java Tools Pack) out of the box and Eclipse and IntelliJ when Annotation Processing is enabled.
