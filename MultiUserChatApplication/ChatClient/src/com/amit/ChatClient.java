package com.amit;

import jdk.jfr.MemoryAddress;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8898);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE : "+login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE : "+login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + "===> " + msgBody);
            }
        });
        if(!client.connect()) {
            System.err.println("Connect failed");
        }else{
            System.out.println("Connect successful");
            if(client.login("guest","guest")){
                System.out.println("Login successful");

                client.msg("jim","Hello");
            }else{
                System.err.println("Login failed");
            }
            //client.logoff();
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());

    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response line is : "+ response);

        if("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        }else{
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    readMessageLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void readMessageLoop() throws IOException {
        String line;
        try{
            while((line=bufferedIn.readLine())!=null){
                String tokens[] = line.trim().split(" ");
                if(tokens!=null && tokens.length>0) {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    }
                    else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }
                    else if("msg".equalsIgnoreCase(cmd)){
                        String tokensMsg[] = line.trim().split(" ",3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            try{
                socket.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private void handleMessage(String []tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners){
            listener.onMessage(login,msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port is : " + socket.getLocalPort());
            this.serverIn = socket.getInputStream();
            this.serverOut = socket.getOutputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
