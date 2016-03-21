/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 21.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
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

package ru.asmsoft.p2p.outgoing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.asmsoft.p2p.configuration.INodeConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.*;

@Component("outgoingChannelAdapter")
public class OutgoingChannelAdapter {

    @Autowired
    INodeConfiguration configuration;

    DatagramSocket clientSocket;

    @PostConstruct
    public void init() throws SocketException {
        clientSocket = new DatagramSocket();
    }


    @PreDestroy
    public void shutdown(){
        clientSocket.close();
    }

    public void send(Message<String> message) throws IOException {

        int port = (Integer) message.getHeaders().get("ip_port");
        String ip = (String) message.getHeaders().get("ip_address");
        byte[] bytesToSend = message.getPayload().getBytes();

        InetAddress IPAddress = InetAddress.getByName(ip);

        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, IPAddress, port);
        clientSocket.send(sendPacket);

    }
}
