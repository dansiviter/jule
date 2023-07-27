package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Logger;
import uk.dansiviter.jule.annotations.Message;

@Logger
interface Good {
	@Message(value = "hello %s", once = true)
  void foo(String world);
}
