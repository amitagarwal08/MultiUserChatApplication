package com.amit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        int port = 8878;
        Server server = new Server(port);
        server.start();
    }

}

