package baccarat_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketGetHandler extends Thread {
    protected Socket socket;
    DataOutputStream out;

    public SocketGetHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run()
    {
        InputStream inp = null;
        BufferedReader brinp = null;
        
        try
        {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            return;
        }
        
        String line;
        
        while(true)
        {
            try
            {
                line = brinp.readLine();
                
                if ((line == null) || line.equalsIgnoreCase("QUIT"))
                {
                	System.out.println("Client closed.\n");
                    socket.close();
                    return;
                }
                else
                {
                	System.out.println("Message recived : "+line+"\n");
                }
                
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
        }
    }
}