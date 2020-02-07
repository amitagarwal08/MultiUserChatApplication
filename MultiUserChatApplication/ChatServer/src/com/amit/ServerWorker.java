package com.amit;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Amit Agarwal
 */
public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login;
    private OutputStream outputStream;
    private final HashSet<String> topicSet = new HashSet<>();

    ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run(){
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line ;
        while((line = reader.readLine()) != null){
            String tokens[] = line.trim().split(" ");
            if(tokens!=null && tokens.length>0){
                String cmd = tokens[0];
                if("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)){
                    handleLogoff();
                    break;
                }
                else if("login".equalsIgnoreCase(cmd)){
                    handleLoginRequest(outputStream,tokens);
                }
                else if("msg".equalsIgnoreCase(cmd)){
                    tokens = line.split(" ",3);
                    handleMessage(tokens);
                }
                else if("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);
                }
                else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }
                else{
                    String msg = "unknown command : " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }

            }
        }
        clientSocket.close();

    }

    public String getLogin(){
        return login;
    }

    private void handleLoginRequest(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length==3){
            String login = tokens[1];
            String password = tokens[2];
            if((login.equalsIgnoreCase("guest") && password.equalsIgnoreCase("guest")) || (login.equalsIgnoreCase("jim") && password.equalsIgnoreCase("jim"))){
                String msg = "Ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully : "+login);

                List<ServerWorker> workerList = server.getWorkerList();
                //send current user about other online logins
                for(ServerWorker worker : workerList){
                    if(!login.equals(worker.getLogin())){
                        if(worker.getLogin() != null){
                            String msg1 = "Online "+worker.getLogin()+"\n";
                            send(msg1);
                        }
                    }
                }

                //send other users about current login user
                String msg2 = "Online "+ login + "\n";
                for(ServerWorker worker : workerList){
                    if(!login.equals(worker.getLogin())){
                        worker.send(msg2);
                    }
                }
            }
            else{
                String msg = "Error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for "+login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if(login != null){
            outputStream.write(msg.getBytes());
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        //send other online users current user's status
        String msg2 = "Offline "+ login + "\n";
        for(ServerWorker worker : workerList){
            if(!login.equals(worker.getLogin())){
                worker.send(msg2);
            }
        }

        clientSocket.close();
    }

    //format : "msg" login body...
    //format : "msg" #topic body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0)=='#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){
            if(isTopic){
                if(worker.isMemeberOfTopic(sendTo)){
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
            else{
                if(sendTo.equalsIgnoreCase(worker.getLogin())){
                    String outMsg = "msg "+ login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    private void handleJoin(String[] tokens) {
        if(tokens.length>1){
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    public boolean isMemeberOfTopic(String topic){
        return topicSet.contains(topic);
    }

    private void handleLeave(String[] tokens) {
        if(tokens.length>1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }
}