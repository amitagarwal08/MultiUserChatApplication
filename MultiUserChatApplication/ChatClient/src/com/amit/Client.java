package com.amit;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread{

    private final int serverPort;
    private final String serverName;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        if (!connect()) {
            System.err.println("Connect failed");
        } else {
            System.out.println("Connect successful");
            try {
                login("jim", "jim");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Socket port is : " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void login(String login, String password) throws IOException {
        String msg = "login " + login + " " + password;
        serverOut.write(msg.getBytes());

        String response;
        while((response= bufferedIn.readLine())!=null) {
            System.out.println("Response line is : " + response);
        }
        socket.close();
    }
}
