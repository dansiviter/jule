package uk.dansiviter.jule.processor;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import java.lang.Override;
import java.lang.String;
import javax.annotation.processing.Generated;
import uk.dansiviter.jule.BaseJulLogger;
import uk.dansiviter.jule.annotations.Logger;

@Generated(
    value = "uk.dansiviter.jule.processor.LoggerProcessor",
    comments = "https://jule.dansiviter.uk",
    date = "2023-02-01T01:02:03.000004Z"
)
@Dependent
public final class GoodCdiImpl implements BaseJulLogger, GoodCdi {
  private final Logger logger;

  private final java.util.logging.Logger delegate;

  @Inject
  public GoodCdiImpl(InjectionPoint ip) {
    this(ip.getMember().getDeclaringClass().getName());
  }

  public GoodCdiImpl(String name) {
    this.logger = GoodCdi.class.getAnnotation(Logger.class);
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
}
