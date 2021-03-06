/**
 * Copyright 2014 Ricardo Padilha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dsys.snio.api.channel;

import net.dsys.snio.api.buffer.MessageBufferConsumer;
import net.dsys.snio.api.buffer.MessageBufferProducer;
import net.dsys.snio.api.io.AsyncChannel;

/**
 * @author Ricardo Padilha
 */
public interface MessageChannel<T> extends AsyncChannel {

	/**
	 * @return the buffer to send and receive messages
	 */
	MessageBufferConsumer<T> getInputBuffer();

	/**
	 * @return the buffer to send and receive messages
	 */
	MessageBufferProducer<T> getOutputBuffer();

	/**
	 * Provide an action to be performed when a connection is closed.
	 */
	MessageChannel<T> onClose(CloseListener<T> listener);

}
