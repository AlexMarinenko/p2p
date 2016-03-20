/**
 * The MIT License (MIT)
 *
 * Copyright (c) 20.03.16 Alex S. Marinenko
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

import ru.asmsoft.p2p.entity.Node;

import java.util.Collection;

/**
 * Nodes repository interface.
 */
public interface INodeRepository {

    /**
     * Register a new node or update node timeout
     * @param address IP address of the node
     */
    void registerNode(String address);

    Node unregisterNode(String address);

    /**
     * Get node by it's IP address
     * @param address IP address to search by
     * @return either found Node or null
     */
    Node getNodeByAddress(String address);

    /**
     * Get registered nodes
     * @return collection of registered nodes
     */
    Collection<Node> getNodes();

    /**
     * If repo is ready to go
     * @return if the repository is ready and filled up
     */
    boolean isInitialized();
}
