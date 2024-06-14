package baccarat_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketSendHandler {
    protected Socket socket;
    DataOutputStream out;
    InputStream inp;
    BufferedReader brinp;

    public SocketSendHandler(Socket clientSocket){
        this.socket = clientSocket;
        
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
        
    }

    public void sendGameInfo(String msg) throws IOException
    {
        out.writeBytes(msg + "\n");
        out.flush();
    }
}