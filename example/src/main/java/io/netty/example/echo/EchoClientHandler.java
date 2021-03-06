/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.echo;

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.channel.ChannelBufferHolder;
import io.netty.channel.ChannelBufferHolders;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInboundHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter<Byte> {

    private static final Logger logger = Logger.getLogger(
            EchoClientHandler.class.getName());

    private final ChannelBuffer firstMessage;

    /**
     * Creates a client-side handler.
     */
    public EchoClientHandler(int firstMessageSize) {
        if (firstMessageSize <= 0) {
            throw new IllegalArgumentException("firstMessageSize: " + firstMessageSize);
        }
        firstMessage = ChannelBuffers.buffer(firstMessageSize);
        for (int i = 0; i < firstMessage.capacity(); i ++) {
            firstMessage.writeByte((byte) i);
        }
    }

    @Override
    public ChannelBufferHolder<Byte> newInboundBuffer(ChannelInboundHandlerContext<Byte> ctx) {
        return ChannelBufferHolders.byteBuffer();
    }

    @Override
    public void channelActive(ChannelInboundHandlerContext<Byte> ctx) {
        ctx.write(firstMessage);
    }

    @Override
    public void inboundBufferUpdated(ChannelInboundHandlerContext<Byte> ctx) {
        ChannelBuffer in = ctx.in().byteBuffer();
        ChannelBuffer out = ctx.out().byteBuffer();
        out.discardReadBytes();
        out.writeBytes(in);
        in.discardReadBytes();
        ctx.flush();
    }

    @Override
    public void exceptionCaught(
            ChannelInboundHandlerContext<Byte> ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }
}
