package com.amit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener{

    private final ChatClient client;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userList), BorderLayout.CENTER);

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>1){
                    String toUser = userList.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client,toUser);

                    JFrame f = new JFrame("Message "+toUser);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,600);
                    f.getContentPane().add(messagePane,BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8878);


        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("Active user list");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,600);
        frame.getContentPane().add(userListPane,BorderLayout.CENTER);
        frame.setVisible(true);

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

    @Override
    public void online(String username) {
        userListModel.addElement(username);
    }

    @Override
    public void offline(String username) {
        userListModel.removeElement(username);
    }
}
