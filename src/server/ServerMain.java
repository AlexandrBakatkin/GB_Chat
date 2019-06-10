package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;

public class ServerMain {
    private Vector<ClientHandler> clients;

    public ServerMain () throws SQLException {
        ServerSocket server = null;
        Socket socket = null;
        clients = new Vector<>();

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Server start. Wait...");

            while(true){
                socket = server.accept();
                System.out.println("Client connected...");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnet();
        }
    }

    public void broadcastMsg(ClientHandler from, String msg){
        for (ClientHandler o: clients) {
            if (!o.checkBlackList(from.getNick())){o.sendMsg(msg);}
        }

        //Записываем общие сообщения в базу данных
        AuthService.saveMessageDB(msg, from.getNick());
    }

    public boolean isLoginFree (String nick){
        if (clients.isEmpty()) {return true;}
        for (ClientHandler o: clients) {
            if (o.getNick().equals(nick)) {
                return false;
            }
        } return true;
    }

    public ClientHandler findClient (String nick){
        for (ClientHandler user: clients) {
            if (user.getNick().equals(nick)){
                return user;
            }
        } return null;
    }

    public void loginUser (ClientHandler client){
        clients.add(client);
        broadcastClientList();
    }

    public void logoutUser (ClientHandler client){
        clients.remove(client);
        broadcastClientList();
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientlist ");

        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }

        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public void sendPrivateMessage(ClientHandler from, ClientHandler to, String message) {
        if (!to.checkBlackList(from.getNick())) {
            to.sendMsg(message);
        }
    }
}