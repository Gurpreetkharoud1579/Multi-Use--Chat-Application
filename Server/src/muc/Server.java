package muc;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int port;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {

        try {
            //create the serversocket for given port
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
//                Socket clientSocket = serverSocket.accept();
//                //for multiple co-login to port using multithreading in serverworker
//                ServerWorker worker = new ServerWorker(this,clientSocket);
//                //adding current client worker to worker list
//                workerList.add(worker);
//                worker.start();
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //when some worker logoff it is removed for worker list
    public void remove(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
