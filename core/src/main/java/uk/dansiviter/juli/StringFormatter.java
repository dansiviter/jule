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
package uk.dansiviter.juli;

import java.util.MissingResourceException;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Formatter that uses {@link String#format(String, Object...)}.
 */
public class StringFormatter extends SimpleFormatter {
	@Override
	public String formatMessage(LogRecord record) {
		var format = record.getMessage();
		var catalog = record.getResourceBundle();
		if (catalog != null) {
			try {
				format = catalog.getString(format);
			} catch (MissingResourceException ex) {
				// Drop through.  Use record message as format
			}
		}
		// Do the formatting.
		try {
			var parameters = record.getParameters();
			if (parameters == null || parameters.length == 0) {
				// No parameters.  Just return format string.
				return format;
			}
			return String.format(format, parameters);
		} catch (Exception ex) {
			// Formatting failed: use localized format string.
			return format;
		}
	}
}
