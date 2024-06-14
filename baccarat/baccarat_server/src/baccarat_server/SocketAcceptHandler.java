package baccarat_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketAcceptHandler extends Thread{
	private ServerSocket serverSocket;
    private Socket clientSocket;
    private List<SocketGetHandler> handlerGet;
    private List<SocketSendHandler> handlerSend;

    public void start(int port, List<SocketGetHandler> handlerGet_, List<SocketSendHandler> handlerSend_) throws IOException
    {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started sucsessfully!\nWaiting for client to accept...");
        
        handlerGet = handlerGet_;
        handlerSend = handlerSend_;
        
        while(true)
        {
            clientSocket = serverSocket.accept();
            System.out.println("New user found...");
            handlerGet.add(new SocketGetHandler(clientSocket));
            handlerGet.get(handlerGet.size()-1).start();
            
            handlerSend.add(new SocketSendHandler(clientSocket));
        }
    }
    
    /*
    public void sendMessageToAll(String msg) throws IOException
    {
    	System.out.println("Trying to send a message to all...\n");
    	for(int i = 0; i < handler.size(); i++)
    	{
    		System.out.println("i : "+i);
    		handler.get(i).sendMessage(msg);
    	}
    }
    */
    
}
