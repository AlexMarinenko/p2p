/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 20.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
 * <p>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.asmsoft.p2p.storage;

import ru.asmsoft.p2p.entity.P2PMessage;

import java.util.Collection;

public interface IMessageRepository {

    /**
     * Is the repository synchronized
     * @return synchronization status
     */
    boolean isSynchronized();

    void setSynchronized(boolean isSynchronized);

    /**
     * Register new message
     * @param message the message to register
     */
    void registerMessage(P2PMessage message);

    /**
     * Synchronize the whole DB
     * @param messages messages to synchronize with
     */
    void syncDb(Collection<P2PMessage> messages);

    /**
     * Get current database version
     * @return database version
     */
    long getDbVersion();
}
