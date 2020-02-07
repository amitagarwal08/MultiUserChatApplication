package com.amit;

public class ChatServer {
    public static void main(String[] args) throws InterruptedException{
        // TODO code application logic here
        int port = 8878;
        Server server = new Server(port);
        server.start();
    }

}
