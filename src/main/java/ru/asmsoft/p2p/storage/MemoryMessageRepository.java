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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.asmsoft.p2p.storage.entity.P2PMessage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * In-memory message repository implementation
 */

@Repository
public class MemoryMessageRepository implements IMessageRepository{

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private long version = 0L;

    private boolean isSynchronized;

    private Map<Long, List<P2PMessage>> changesets = new ConcurrentHashMap<Long, List<P2PMessage>>();

    private Collection<P2PMessage> messages = new CopyOnWriteArrayList<P2PMessage>();

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
        messages.add(message);
    }

    @Override
    public void syncDb(long dbVersion, Collection<P2PMessage> messages) {
        messages.clear();
        messages.addAll(messages);
        version = dbVersion;
    }

    @Override
    public long getDbVersion() {
        return version;
    }

    @Override
    public void updateDbVersion(long version) {
        this.version = version;
    }

    @Override
    public void registerChangeset(long dbVersion, List<P2PMessage> messages) {
        logger.info("Register changeset: {} size: {}", dbVersion, messages.size());
        changesets.put(dbVersion, messages);
    }

    @Override
    public void applyChangeset(long dbVersion) throws NoChangesetFoundException {

        List<P2PMessage> changeset = changesets.remove(dbVersion);

        if (changeset == null){
            throw new NoChangesetFoundException(dbVersion);
        }

        messages.addAll(changeset);
        version = dbVersion;

    }

    @Override
    public Collection<P2PMessage> cancelChangeset(long dbVersion) throws NoChangesetFoundException {

        List<P2PMessage> changeset = changesets.remove(dbVersion);

        if (changeset == null){
            throw new NoChangesetFoundException(dbVersion);
        }

        return changeset;

    }

    @Override
    public Collection<P2PMessage> getMessage() {
        return messages;
    }

}
