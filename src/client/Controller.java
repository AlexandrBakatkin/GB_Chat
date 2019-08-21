package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    Button authButton;

    @FXML
    ListView<String> clientList;

    private boolean isAuthorized;

    Socket socket;
    DataInputStream in;
    DataOutput out;
    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;

        if(!isAuthorized){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setManaged(false);
            bottomPanel.setVisible(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setManaged(true);
            bottomPanel.setVisible(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        textField.clear();
        textField.requestFocus();
    }

    public void connect () {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while (true){
                            String str = in.readUTF();
                            if(str.startsWith("/authok")){
                                setAuthorized(true);
                                break;
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                        while (true){
                            String str = in.readUTF();
                            if (str.equals("/ServerClosed")){
                                setAuthorized(false);
                                break;
                            }
                            textArea.appendText(str + "\n");
                            if(str.startsWith("/clientlist")) {
                                String[] tokens = str.split(" ");

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth (){
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth" + " " + login.getText() + " " + password.getText());
            login.clear();
            password.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}