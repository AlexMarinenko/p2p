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

package ru.asmsoft.p2p.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("nodeConfiguration")
public class PropertyBasedNodeConfiguration implements INodeConfiguration{

    @Value("${p2p.heartbeatPeriod}")
    long heartbeatPeriod;

    @Value("${p2p.heartbeatExpired}")
    long heartbeatPeriodExpired;

    @Value("${p2p.broadcastAddress}")
    String broadcastAddress;

    @Value("${p2p.listenerAddress}")
    String listenerAddress;

    @Value("${p2p.port}")
    int port;

    @PostConstruct
    public void init(){
        int i = 0;
    }

    @Override
    public long getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    @Override
    public long getHeartbeatPeriodExpired() {
        return heartbeatPeriodExpired;
    }

    @Override
    public String getBroadcastAddress(){
        return broadcastAddress;
    }

    @Override
    public String getListenerAddress(){
        return listenerAddress;
    }

    @Override
    public int getPort() {
        return port;
    }



}
