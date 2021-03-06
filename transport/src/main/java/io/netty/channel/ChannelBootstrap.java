package io.netty.channel;

import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ChannelBootstrap {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelBootstrap.class);

    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<ChannelOption<?>, Object>();
    private EventLoop eventLoop;
    private Channel channel;
    private ChannelHandler initializer;
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;

    public ChannelBootstrap eventLoop(EventLoop eventLoop) {
        if (eventLoop == null) {
            throw new NullPointerException("eventLoop");
        }
        this.eventLoop = eventLoop;
        return this;
    }

    public ChannelBootstrap channel(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        return this;
    }

    public <T> ChannelBootstrap option(ChannelOption<T> option, T value) {
        if (option == null) {
            throw new NullPointerException("option");
        }
        if (value == null) {
            options.remove(option);
        } else {
            options.put(option, value);
        }
        return this;
    }

    public ChannelBootstrap initializer(ChannelHandler initializer) {
        if (initializer == null) {
            throw new NullPointerException("initializer");
        }
        this.initializer = initializer;
        return this;
    }

    public ChannelBootstrap localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public ChannelBootstrap localAddress(int port) {
        localAddress = new InetSocketAddress(port);
        return this;
    }

    public ChannelBootstrap localAddress(String host, int port) {
        localAddress = new InetSocketAddress(host, port);
        return this;
    }

    public ChannelBootstrap remoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public ChannelBootstrap remoteAddress(String host, int port) {
        remoteAddress = new InetSocketAddress(host, port);
        return this;
    }

    public ChannelFuture bind() {
        validate();
        return bind(channel.newFuture());
    }

    public ChannelFuture bind(ChannelFuture future) {
        validate();
        if (localAddress == null) {
            throw new IllegalStateException("localAddress not set");
        }

        try {
            init();
        } catch (Throwable t) {
            future.setFailure(t);
            return future;
        }

        return channel.bind(localAddress, future).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    public ChannelFuture connect() {
        validate();
        return connect(channel.newFuture());
    }

    public ChannelFuture connect(ChannelFuture future) {
        validate();
        if (remoteAddress == null) {
            throw new IllegalStateException("remoteAddress not set");
        }

        try {
            init();
        } catch (Throwable t) {
            future.setFailure(t);
            return future;
        }

        if (localAddress == null) {
            channel.connect(remoteAddress, future);
        } else {
            channel.connect(remoteAddress, localAddress, future);
        }
        return future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    private void init() throws Exception {
        if (channel.isActive()) {
            throw new IllegalStateException("channel already active:: " + channel);
        }
        if (channel.isRegistered()) {
            throw new IllegalStateException("channel already registered: " + channel);
        }
        if (!channel.isOpen()) {
            throw new ClosedChannelException();
        }

        ChannelPipeline p = channel.pipeline();
        p.addLast(DefaultChannelPipeline.generateName(initializer), initializer);

        for (Entry<ChannelOption<?>, Object> e: options.entrySet()) {
            try {
                if (!channel.config().setOption((ChannelOption<Object>) e.getKey(), e.getValue())) {
                    logger.warn("Unknown channel option: " + e);
                }
            } catch (Throwable t) {
                logger.warn("Failed to set a channel option: " + channel, t);
            }
        }

        eventLoop.register(channel).syncUninterruptibly();
    }

    public void shutdown() {
        if (eventLoop != null) {
            eventLoop.shutdown();
        }
    }

    private void validate() {
        if (eventLoop == null) {
            throw new IllegalStateException("eventLoop not set");
        }
        if (channel == null) {
            throw new IllegalStateException("channel not set");
        }
        if (initializer == null) {
            throw new IllegalStateException("initializer not set");
        }
    }
}
