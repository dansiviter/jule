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
package uk.dansiviter.logging;

/**
 * This library has two purposes:
 * <ul>
 * <li>To define a very thin wrapper on {@link java.util.logging.Logger} to reduce the boilerplate code and
 * implement best practices,</li>
 * <li>To provide specialist {@link java.util.logging.Handler} implementations to potentially improve performance
 * within your application.</li>
 * </ul>
 */
