package com.md_5.irc;

import java.net.InetSocketAddress;

public class Test {

    public static void main(String[] args) throws Exception {
        IrcBot bot = new IrcBot("MyBot");
        bot.connect(new InetSocketAddress("irc.esper.net", 6667));
    }
}
