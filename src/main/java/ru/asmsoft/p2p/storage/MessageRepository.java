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

import org.springframework.stereotype.Repository;
import ru.asmsoft.p2p.entity.P2PMessage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * In-memory message repository implementation
 */

@Repository
public class MessageRepository implements IMessageRepository{

    private long version = 0L;

    private boolean isSynchronized;

    private Collection<P2PMessage> messages = new ArrayList<P2PMessage>();

    @Override
    public boolean isSynchronized() {
        return isSynchronized;
    }

    @Override
    public void setSynchronized(boolean isSynchronized){
        this.isSynchronized = isSynchronized;
    }

    @Override
    public void registerMessage(P2PMessage message) {

    }

    @Override
    public void syncDb(Collection<P2PMessage> messages) {

    }

    @Override
    public long getDbVersion() {
        return version;
    }
}
