package com.muc;

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
    private  ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListener = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverPort=serverPort;
        this.serverName=serverName;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8808);
        //every user will have userListener for online and offline
        client.addUserListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online: " + login);
            }
            @Override
            public void offline(String login) {
                System.out.println("Offline: " + login);
            }
        });
        //every user will have messageListener
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String messageFromLogin, String messageBody) {
                System.out.println("Message From "+ messageFromLogin + "--> " + messageBody);
            }
        });
        if(!client.connect()){
            System.out.println("Connection not established \n");
        }
        else{
            System.out.println("Connection established \n");
            if( client.login("guest","guest") ) {
                System.out.println("Login Successful \n");
                client.sendMessage("secondguest","what's up");
            }
            else
                System.err.println("Login failed \n");
        }
        //client.logoff();
    }

    private void sendMessage(String sendTo,String message) throws IOException {
        serverOut.write(("Message " + sendTo +" " + message).getBytes() );
    }

    private void logoff() throws IOException {
        String cmd = "logoff";
        serverOut.write(cmd.getBytes());
    }

    private boolean login(String user, String password) throws IOException {
        String cmd = "login "+ user +" "+password +"\n";
        serverOut.write(cmd.getBytes());
        String response = bufferedIn.readLine();
        if("Valid user and password ".equalsIgnoreCase(response)) {
            //once the user login now get event information from server like online, offline, message
            startReadingMessage();
            return true;
        }
        return false;
    }

    private void startReadingMessage() {
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    readMessageLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }
    //will read all events
    private void readMessageLoop() throws IOException {
        String line;
        while((line=bufferedIn.readLine())!=null){
            String[] tokens = line.split(" ");
            String cmd = tokens[0];
            if("Online".equalsIgnoreCase(cmd))
                handleOnline(tokens);
            else if("Offline".equalsIgnoreCase(cmd))
                handleOffline(tokens);
            else if("Message".equalsIgnoreCase(cmd)){
                String[] tokensMessage = line.split(" ",3);
                handleMessage(tokensMessage);
            }
        }
    }

    private void handleMessage(String[] tokensMessage) {
        String login =tokensMessage[1];
        String messageBody = tokensMessage[2];
        for(MessageListener listener : messageListener){
            listener.onMessage(login,messageBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login =  tokens[1];
        for(UserStatusListener listener : userStatusListeners ){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login =  tokens[1];
        for(UserStatusListener listener : userStatusListeners ){
            listener.online(login);
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println(socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    public void addUserListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }
    public void addMessageListener(MessageListener listener){
        messageListener.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListener.remove(listener);
    }
}
