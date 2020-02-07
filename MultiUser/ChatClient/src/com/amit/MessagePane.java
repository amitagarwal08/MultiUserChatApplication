package com.amit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener{
    private final ChatClient client;
    private final String toUser;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String toUser) {
        this.client = client;
        this.toUser = toUser;

        this.client.addMessageListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList),BorderLayout.CENTER);
        add(inputField,BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msgBody = inputField.getText();
                inputField.setText("");
                listModel.addElement("You:" + msgBody);
                try {
                    client.msg(toUser,msgBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    public void onMessage(String fromUser, String msgBody) {
        if(toUser.equalsIgnoreCase(fromUser)) {
            String line = fromUser + ":" + msgBody;
            System.out.println("Msg line is "+ line);
            listModel.addElement(line);
        }
    }
}
