package server;

import com.mollin.yapi.command.YeelightCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    private int port;

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public TCPServer(int port) {
        this.port = port;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((in.readLine()) != null) {
//                    System.out.println(inputLine);
            }
        } catch (Exception e) {
            System.out.println("Server couldn't start");
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            in.close();
            out.close();
            socket.close();
            serverSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendCommand(YeelightCommand jsonCommand) {
        //System.out.println(jsonCommand.toJson());
        if (out != null) {
            out.println(jsonCommand.toJson() + "\r\n");
        }
    }
}
