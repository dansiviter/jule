package uk.dansiviter.juli.processor;

import uk.dansiviter.juli.annotations.Log;
import uk.dansiviter.juli.annotations.Message;

@Log
interface Good {
	@Message(value = "hello {0}", once = true)
  void foo(String world);
}
