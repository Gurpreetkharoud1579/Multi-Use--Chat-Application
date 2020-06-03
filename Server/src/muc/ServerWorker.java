package muc;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login;
    private OutputStream outputStream;
    private HashSet<String>  topicSet = new HashSet<>(); //every user have topics

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
//        try {
//            handleClientSocket();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            handleClientSocket();
        } catch (IOException e) {
            if(e.getMessage().equalsIgnoreCase("Connection reset")){
                System.out.println("Client disconnected..Waiting for another connection");
            }else{
                e.printStackTrace();
            }
        }

    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream  = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while( (line=reader.readLine())!=null){
            String[] tokens = line.split(" ");
            String cmd = tokens[0];
            if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                handleLogoff();
                break;
            } else if("login".equalsIgnoreCase(cmd)){
                handleLogin(tokens,outputStream);

            }else if("Message".equalsIgnoreCase(cmd)){
                String[] tokensMessage = line.split(" ",3);
                handleMessage(tokensMessage);
            }else if("join".equalsIgnoreCase(cmd)){
                handleJoin(tokens);
            }else if("leave".equalsIgnoreCase(cmd)){
                handleLeave(tokens);
            }
            else{
                String message = "Error login " + cmd + "\n";
                outputStream.write(message.getBytes());
                System.err.println("Login failed " +login);
            }

        }
        System.out.println("connection closed"+login);
        clientSocket.close();
    }
    // to remove topic from topic set
    private void handleLeave(String[] tokens) {

        if(tokens.length>1){
            String topic =tokens[1];
            if(topicSet.contains(topic)){
                topicSet.remove(topic);
            }
        }
    }

    private boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }
    //to add topic to topicSet
    private void handleJoin(String[] tokens) {
        if(tokens.length>1){
            String topic = tokens[1];
            topicSet.add(topic); // means this user joined topic
        }
    }

    // "Message" "username" "message" (tokenMessage body)
    // "Message"  "#topic"  "message"(send message to topic body)
    private void handleMessage(String[] tokensMessage) throws IOException {
        if(tokensMessage.length==3) {
            String sendTo = tokensMessage[1];              //destination user
            String message = tokensMessage[2];
            //check if sendTo is topic or single person
            boolean isTopic = sendTo.charAt(0)=='#';
            List<ServerWorker> workerList = server.getWorkerList();
            //iterate over every user to check destination user
            for(ServerWorker worker:workerList){
                if(isTopic){
                    if(worker.isMemberOfTopic(sendTo)){
                        String msg = "Message" + sendTo +": from  " + login + " " + message;
                        worker.send(msg);
                    }

                }
                else {
                    if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                        String msg = "Message " + login + " " + message;
                        worker.send(msg);
                    }
                }
            }

        }
    }

    private void handleLogoff() throws IOException {
        server.remove(this);
        List<ServerWorker> workerList = server.getWorkerList();
        //send other login current login
        String onlinemessage = "Offline " + login+"\n";
        for(ServerWorker worker:workerList){
            if(!login.equals(worker.getLogin()))
                worker.send(onlinemessage);
        }
        clientSocket.close();
    }

    public String getLogin(){
        return login;
    }

    private void handleLogin(String[] tokens, OutputStream outputStream) throws IOException {
        if(tokens.length==3){
            String login = tokens[1];
            String password =  tokens[2];
            if(login.equalsIgnoreCase("guest") && password.equalsIgnoreCase("guest") ||
            login.equalsIgnoreCase("secondguest") && password.equalsIgnoreCase("secondguest") ){
                String message = "Valid user and password \n";
                outputStream.write(message.getBytes());
                this.login=login;
                System.out.println("User login successfully " + login );
                List<ServerWorker> workerList = server.getWorkerList();
                //send current login all other logins
                for(ServerWorker worker:workerList){
                    if(worker.getLogin()!=null && !login.equals(worker.getLogin())) {
                        String msg = "Online " + worker.getLogin() + "\n";
                        send(msg);
                    }
                }
                //send other login current login
                String onlinemessage = "Online " + login+"\n";
                for(ServerWorker worker:workerList){
                    if(!login.equals(worker.getLogin()))
                        worker.send(onlinemessage);
                }
            }
            else{
                String message = "Unvalid user or password\n";
                System.err.println("Unvalid user or password");
                outputStream.write(message.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if(login!=null)
            outputStream.write((msg+"\n").getBytes());
    }
}
