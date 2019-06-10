package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private ServerMain server;
    private String nick;
    private ArrayList<String> blackList;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, ServerMain server) {

        try {
            this.socket = socket;
            this.server = server;
            this.blackList = new ArrayList<>();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        sendMsg("Вы подключились к серверу");

                        for (String o: AuthService.loadHistoryDB()
                             ) {
                            sendMsg(o);
                        }

                        while (true){
                            String str = in.readUTF();
                            if (str.startsWith("/auth")){
                                if (checkUser(str, server)) {
                                    String[] blackNicks = AuthService.getBlackListbyNick(ClientHandler.this.getNick());
                                    if (blackNicks != null){
                                        for (int i = 0; i < blackNicks.length; i++) {
                                            blackList.add(blackNicks[i]);
                                        }
                                    }

                                    break;
                                }
                            }
                        }

                        while (true){
                            String str = in.readUTF();

                                if (str.equals("/end")) {
                                    sendMsg("/ServerClosed");
                                    StringBuffer sb = new StringBuffer();
                                    for (String nick: blackList
                                         ) {
                                        sb.append(nick + " ");
                                    }
                                    sb.deleteCharAt(sb.length() - 1);
                                    sb.toString();
                                    AuthService.saveBlackList(sb.toString(), nick);
                                    logout();
                                    break;
                                }

                            if (str.startsWith("/w")){
                                String [] message = str.split(" ");
                                String temp = "/w " + nick + " ";
                                String prvtMsg = str.replaceFirst(temp, "");
                                sendPrivateMessage(message[1], prvtMsg);
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + ": " + str);
                            }

                            if (str.startsWith("/blacklist")){
                                String [] strSplit = str.split(" ");
                                blackList.add(strSplit[1]);
                                sendMsg("Вы добавили пользователя " + strSplit[1] + " в черный список.");
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    private boolean checkUser(String str, ServerMain server) {
        String [] token = str.split(" ");
        String newNick = AuthService.getNickByLoginAndPass(token[1], token [2]);

        if (newNick != null){

            if (server.isLoginFree(newNick)) {
                server.broadcastMsg(ClientHandler.this, newNick + ": вошел в чат");
                sendMsg("/authok");
                nick = newNick;
                server.loginUser(ClientHandler.this);
                return true;
            } else {
                sendMsg("Логин уже занят");
                sendMsg("/ServerClosed");
            }

        } else {
            sendMsg("Логин/пароль неверные");
        }
        return false;
    }

    private void logout() {
        server.logoutUser(this);
    }

    public void sendMsg(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPrvtMsg(String nick, String str){
        if (server.findClient(nick)== null){
            sendMsg("Клиент с таким логином не найден");
            return;
        }
        if (!blackList.contains(nick)){
        server.findClient(nick).sendMsg("Сообщeние от " + nick + ": " + str);
        } else {
            sendMsg("Клиент с таким ником в Вашем черном списке");
        }
    }

    public void sendPrivateMessage(String nick, String str){
        if(server.findClient(nick) != null){
        server.sendPrivateMessage(ClientHandler.this, server.findClient(nick), str);
        } else {
            sendMsg("Клиент с таким логином не найден");
        }
    }

    public boolean checkBlackList (String nick){
        return blackList.contains(nick);
    }
}