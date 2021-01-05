package me.kec.mp.bare;

import io.helidon.microprofile.server.Server;

public class Main {
    public static void main(String[] args) {
        System.setProperty("oracle.jdbc.fanEnabled", "false");
        Server.create().start();
    }
}
