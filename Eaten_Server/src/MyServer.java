import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

//created by Haotian Zhu, modified by Xiaowen Jiang
//the server main class

public class MyServer {
	 //create a socket list to store client sockets
    public static ArrayList<Socket> socketList = new ArrayList<Socket>(); 
    //create a hashmap to map username to client socket
    public static HashMap<String , Socket> userList=new HashMap<String, Socket>();
    
    public static void main(String[] args) throws IOException, SQLException  
    {  
    	System.out.println("Server is running!");
		
		
    	//port name
        ServerSocket ss = new ServerSocket(6012);
        System.out.println("Port number is: 6012");
        System.out.println(getIpAddress());
        while(true)  
        {  
            //waiting for connecting
            Socket s = ss.accept();  
            System.out.println("New Device Connected!");
            socketList.add(s);  
            //create a new thread for a client 
            new Thread(new SocketThread(s)).start();  
        }  
    }
    
    //to get current IPAddress of server
    static private String getIpAddress() {
  	  String ip = "";
  	  try {
  	   Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
  	     .getNetworkInterfaces();
  	   while (enumNetworkInterfaces.hasMoreElements()) {
  	    NetworkInterface networkInterface = enumNetworkInterfaces
  	      .nextElement();
  	    Enumeration<InetAddress> enumInetAddress = networkInterface
  	      .getInetAddresses();
  	    while (enumInetAddress.hasMoreElements()) {
  	     InetAddress inetAddress = enumInetAddress.nextElement();

  	     if (inetAddress.isSiteLocalAddress()) {
  	      ip += "IPAddress: " 
  	        + inetAddress.getHostAddress() + "\n";
  	     }
  	     
  	    }

  	   }

  	  } catch (SocketException e) {
  	   // TODO Auto-generated catch block
  	   e.printStackTrace();
  	   ip += "Something Wrong! " + e.toString() + "\n";
  	  }
  	  return ip;
  	 }
}
