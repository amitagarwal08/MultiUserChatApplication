package com.amit;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String username;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        this.outputStream = clientSocket.getOutputStream();
        this.inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            String tokens[] = line.trim().split(" ");
            String cmd = tokens[0];
            if("login".equalsIgnoreCase(cmd)){
                handleLogin(tokens);
            }
            else if("logoff".equalsIgnoreCase(cmd)){
                handleLogoff();
                break;
            }
            else if("msg".equalsIgnoreCase(cmd)){
                String msgTokens[] = line.trim().split(" ",3);
                handleMessage(msgTokens);
            }
        }
    }

    private void handleMessage(String[] msgTokens) throws IOException {
        String toUsername = msgTokens[1];
        String msgBody = msgTokens[2];
        ArrayList<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){
            if(toUsername.equalsIgnoreCase(worker.getUsername())){
                String msg = "Message " + username + " " + msgBody + "\n";
                worker.send(msg);
            }
        }
    }

    private void send(String msg) throws IOException {
        //System.out.println(msg);
        outputStream.write(msg.getBytes());
    }

    private String getUsername() {
        return username;
    }

    private void handleLogoff() throws IOException {
        if(username!=null) {
            server.removeWorkerList(this);

            ArrayList<ServerWorker> workerList = server.getWorkerList();

            for (ServerWorker worker : workerList) {
                String msg = "Offline " + username + "\n";
                worker.send(msg);
            }

            System.out.println("Successfully logged off : " + username);
            clientSocket.close();
        }

    }

    private void handleLogin(String[] tokens) throws IOException {
        if(tokens.length==3) {
            String username = tokens[1];
            String password = tokens[2];
            if ((username.equals("amit") && password.equals("amit")) || (username.equals("guest") && password.equals("guest"))) {
                this.username = username;
                System.out.println("Successfully logged in as " + username);
                String cmd = "Login successful\n";
                outputStream.write(cmd.getBytes());

                ArrayList<ServerWorker> workerList = server.getWorkerList();
                //to tell online users about user status
                for (ServerWorker worker : workerList) {
                    if(!username.equalsIgnoreCase(worker.getUsername())) {
                        if(worker.getUsername()!=null) {
                            String msg = "Online " + username + "\n";
                            worker.send(msg);
                        }
                    }
                }

                //to tell current user who is online
                for(ServerWorker worker: workerList){
                    if(!username.equalsIgnoreCase(worker.getUsername())){
                        if(worker.getUsername()!=null) {
                            String msg = "Online " + worker.getUsername() + "\n";
                            send(msg);
                        }
                    }
                }
            }
            else{
                String cmd = "Login failed\n";
                outputStream.write(cmd.getBytes());
                System.err.println("Login failed for "+username);
            }
        }
        else{
            String msg = "Please provide valid input\n";
            outputStream.write(msg.getBytes());
        }
    }
}
