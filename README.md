![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dansiviter/logging/Java%20CI?style=flat-square)

# Logger Wrap #

Log4J? SLF4J? Commons Logger? Flogger? How about another?!

Although rarely the preferred framework `java.util.Logger` is embedded into Java so aids minimal applications but is not the easiest to use. This library thinly wraps it to make life a little easier:
* Type safe logging,
* Simpler unit testing, especially with CDI,
* Defer array initialisation and auto-boxing until actually needed,
* Automatic unwrapping of `java.util.function.Supplier` and `java.util.Optional` parameters.

> :information_source: [JBoss Logging Tools](https://github.com/jboss-logging/jboss-logging-tools) was the inspiration but designed to be even simpler, leaner and not dependent on JBoss Log Manager.

First import dependencies:

```xml
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>core</artifactId>
  <version>{dsLogger.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>processor</artifactId>
  <version>{dsLogger.version}</version>
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
  @Message("Hello {0}")
  void hello(String name);

  @Message(value = "Oh no! {0}", level = Level.ERROR)
  void error(String name, Throwable t);  // <- Throwables must be last parameter
}
```

This will generate a class `com.foo.MyLog$log` which actually does the logging.

To get an instance use `uk.dansiviter.logging.LogProducer`:
```java
public class MyClass {
  private final static MyLog LOG = LogProducer.log(MyLog.class);

  public void myMethod() {
    LOG.hello("foo");
  }
}
```

## CDI ##

This can perform automatic injection of dependencies via CDI:

```xml
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>cdi</artifactId>
  <version>{dsLogger.version}</version>
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


## Asynchronous Handlers ##

JUL handlers are all synchronous which puts IO directly within the path of execution; this is a bottleneck. To address this, this library has a very simple `uk.dansiviter.logging.AsyncHandler` implementation that uses `java.util.concurrent.Flow` to asynchronously process log events. Using this can dramatically improve the performance of 'chatty' logging at the expense of a little memory and CPU. There is only one concrete implementation as the moment which is `uk.dansiviter.logging.AsyncConsoleHandler` which can be used as a direct replacement for `java.util.logging.ConsoleHandler`.

> :warning: If the buffer saturates, then the much of the performance benefits of the asynchronous handler can be lost. However, it _should_ still outperform a synchronous implementation.
