package com.md_5.irc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;

public class IrcBot {

    protected String nick;
    @Setter
    protected String login;
    @Setter
    protected String realName = "My First IRC Bot";
    //
    @Setter
    protected Charset charset = Charset.defaultCharset();
    protected Channel connection;
    //
    protected boolean connected;
    protected Queue<String> loginMessageQueue = new LinkedList<String>();
    @Getter
    @Setter
    protected boolean verbose = true;

    public IrcBot(String nick) {
        this.nick = nick;
        this.login = nick;
    }

    /**
     * Connect to the given server. This method will not block.
     */
    public final void connect(InetSocketAddress remote) {
        connection = new Bootstrap().remoteAddress(remote).channel(new NioSocketChannel()).handler(new Initializer()).group(new NioEventLoopGroup()).connect().channel();
    }

    /**
     * Process a line from the server
     */
    private void handleLine(String line) {
        if (verbose) {
            System.out.println(">>> " + line);
        }
        if (line.startsWith("PING ")) {
            sendRaw("PONG " + line.substring(5), true);
            return;
        }
        String[] split = line.split(" ", 4);
        String source = split[0].substring(1);
        String operation = split[1];
        String target = split[2];
        String data = (split[3].startsWith(":")) ? split[3].substring(1) : split[3];
    }

    /**
     * Sends a line to the server, waiting if necessary until connected.
     */
    public final void sendRaw(String line) {
        sendRaw(line, false);
    }

    /**
     * Send a raw line straight to the server. If forced the line will be sent
     * regardless of connected state.
     */
    private void sendRaw(String line, boolean force) {
        if (!connected && !force) {
            loginMessageQueue.add(line);
        } else {
            if (verbose) {
                System.out.println("<<< " + line);
            }
            connection.write(line + "\r\n");
        }
    }

    /**
     * Handler to bootstrap the client connection.
     */
    private class Initializer extends ChannelInitializer<Channel> {

        @Override
        public void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()), new StringDecoder(charset), new StringEncoder(charset), new Handler());
        }
    }

    /**
     * The main connection handler and 'event loop' for this bot.
     */
    private class Handler extends ChannelInboundMessageHandlerAdapter<String> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            sendRaw("NICK " + nick, true);
            sendRaw("USER " + login + " 8 * :" + realName, true);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
            handleLine(msg);
        }
    }
}
