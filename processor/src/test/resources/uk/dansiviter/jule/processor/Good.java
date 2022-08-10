package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;

@Log
interface Good {
	@Message(value = "hello {0}", once = true)
  void foo(String world);
}