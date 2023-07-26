package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;
import uk.dansiviter.jule.annotations.Log.Lifecycle;

@Log(lifecycle = Lifecycle.CDI)
interface GoodCdi {
}
