package com.amit;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient{
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private ChatClient client;
//    public ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
//    public ArrayList<MessageListener> messageListeners = new ArrayList<>();
    public UserStatusListener userStatusListeners;
    public MessageListener messageListeners;

    public ChatClient(String serverName, int serverPort) throws IOException {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8878);

        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String username) {
                System.out.println("ONLINE : " + username);
            }

            @Override
            public void offline(String username) {
                System.out.println("OFFLINE : "+username);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromUser, String msgBody) {
                System.out.println("You got a message from " + fromUser + "===> " + msgBody);
            }
        });

        if(client.connect()){
            System.out.println("Connect successful");
            if(client.login("amit","amit")){
                System.out.println("Login successful");
            }
            else{
                System.err.println("Login failed");
            }
        }
        else{
            System.err.println("Connect failed");
        }
    }

    public boolean login(String username, String password) throws IOException {
        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response : " + response);

        if("Login successful".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }
        return false;
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
        while ((line = bufferedIn.readLine()) != null){
            String tokens[] = line.trim().split(" ");
            String cmd = tokens[0];
            if("online".equalsIgnoreCase(cmd)){
                String anotherUser = tokens[1];
                //System.out.println("ONLINE : " + anotherUser);
                handleOnline(tokens);
            }
            else if("offline".equalsIgnoreCase(cmd)){
                String anotherUser = tokens[1];
                System.out.println("OFFLINE : " + anotherUser);
                handleOffline(tokens);
            }
            else if("Message".equalsIgnoreCase(cmd)){
                String msgTokens[] = line.trim().split(" ",3);
                handleMessage(msgTokens);
            }
        }
    }

    private void handleMessage(String msgTokens[]) {
        String fromUser = msgTokens[1];
        String msgBody = msgTokens[2];
        //for(MessageListener listener : messageListeners){
            messageListeners.onMessage(fromUser,msgBody);
        //}
    }

    private void handleOffline(String[] tokens) {
        String anotherUser = tokens[1];
        //for(UserStatusListener listener : userStatusListeners) {
            userStatusListeners.offline(anotherUser);
        //}
    }

    private void handleOnline(String[] tokens) {
        String anotherUser = tokens[1];
        //for(UserStatusListener listener : userStatusListeners) {
            userStatusListeners.online(anotherUser);
        //}
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
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
        //userStatusListeners.add(listener);
        userStatusListeners = listener;
    }

    public void addMessageListener(MessageListener listener){
        //messageListeners.add(listener);
        messageListeners = listener;
    }

    public void msg(String toUser, String msgBody) throws IOException {
        String msg = "msg " + toUser + " " + msgBody + "\n";
        serverOut.write(msg.getBytes());
    }
}
