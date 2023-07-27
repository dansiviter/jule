package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Logger;
import uk.dansiviter.jule.annotations.Logger.Lifecycle;

@Logger(lifecycle = Lifecycle.CDI)
interface GoodCdi {
}
