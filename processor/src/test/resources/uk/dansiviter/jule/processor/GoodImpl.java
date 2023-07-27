package uk.dansiviter.jule.processor;

import java.lang.Override;
import java.lang.String;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.Generated;
import uk.dansiviter.jule.BaseJulLogger;
import uk.dansiviter.jule.annotations.Logger;
import uk.dansiviter.jule.annotations.Message;

@Generated(
    value = "uk.dansiviter.jule.processor.LoggerProcessor",
    comments = "https://jule.dansiviter.uk",
    date = "2023-02-01T01:02:03.000004Z"
)
public final class GoodImpl implements BaseJulLogger, Good {
  private static final AtomicBoolean ONCE__foo = new AtomicBoolean();

  private final Logger logger;

  private final java.util.logging.Logger delegate;

  public GoodImpl(String name) {
    this.logger = Good.class.getAnnotation(Logger.class);
    this.delegate = delegate(name);
  }

  /**
   * @returns the annotation instance.
   */
  @Override
  public final Logger logger() {
    return this.logger;
  }

  /**
   * @returns the delegate logger.
   */
  @Override
  public final java.util.logging.Logger delegate() {
    return this.delegate;
  }

  @Override
  public void foo(String world) {
    if (!isLoggable(Message.Level.INFO)) {
      return;
    }
    if (ONCE__foo.getAndSet(true)) {
      return;
    }
    logp(Message.Level.INFO, "hello %s", world);
  }
}
