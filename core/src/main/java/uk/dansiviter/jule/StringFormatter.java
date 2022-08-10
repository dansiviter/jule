/*
 * Copyright 2021 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.dansiviter.jule;

import java.util.MissingResourceException;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Formatter that uses {@link String#format(String, Object...)}.
 */
public class StringFormatter extends SimpleFormatter {
	@Override
	public String formatMessage(LogRecord r) {
		var format = r.getMessage();
		var bundle = r.getResourceBundle();
		if (bundle != null) {
			try {
				format = bundle.getString(format);
			} catch (MissingResourceException ex) {
				// nothing to see here
			}
		}
		try {
			var parameters = r.getParameters();
			if (parameters == null || parameters.length == 0) {
				return format;
			}
			return String.format(format, parameters);
		} catch (RuntimeException ex) {
			return format;
		}
	}
}
