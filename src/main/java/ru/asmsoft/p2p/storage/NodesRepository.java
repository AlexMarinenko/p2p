/**
 * The MIT License (MIT)
 *
 * Copyright (c) 20.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
 *
 *
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

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.asmsoft.p2p.entity.Node;
import ru.asmsoft.p2p.NodeConfiguration;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * In-memory Nodes interface implementation.
 * ConcurentHashMap based storage.
 */
@Repository
public class NodesRepository implements INodeRepository, RemovalListener<String, Node> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, Node> nodes;
    private boolean isInitialised;

    @Autowired
    NodeConfiguration config;

    @PostConstruct
    public void init(){

        Cache<String, Node> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getHeartbeatPeriodExpired(), TimeUnit.MILLISECONDS)
                .removalListener(this)
                .build();
        nodes = cache.asMap();

        // Initialization delay (Heartbeat period * 2)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.debug("NodesRepository is ready.");
                isInitialised = true;
            }
        }, config.getHeartbeatPeriod() * 2);

    }

    @Override
    public void registerNode(String address){

        Node node = nodes.get(address);

        if (node == null ){
            logger.info("Nodes: {}, Registering new node: {}", nodes.size(), address);
            node = new Node(address);
        }
        nodes.put(address, node);

    }

    @Override
    public Node unregisterNode(String address){
        return nodes.remove(address);
    }

    @Override
    public Node getNodeByAddress(String address){
        return nodes.get(address);
    }

    @Override
    public Collection<Node> getNodes(){
        return nodes.values();
    }

    @Override
    public boolean isInitialized() {
        return isInitialised;
    }

    @Override
    public void onRemoval(RemovalNotification<String, Node> removalNotification) {
        if (removalNotification.getCause() == RemovalCause.EXPIRED) {
            logger.info("Nodes: {}, Expire node: {}", nodes.size(), removalNotification);
        }
    }
}
