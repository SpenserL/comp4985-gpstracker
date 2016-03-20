package main;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
	
	static final int LIST_PORT = 4985;
	
	String retMsg;
	private ServerSocket listenSock;
	
	public ServerThread() throws IOException {
		listenSock = new ServerSocket(LIST_PORT);
		listenSock.setSoTimeout(20000); // 20 sec
	}
	
	public void run() {
		while (true) {
			try {
				// listen for and accept new connections
				System.out.println("\nListening on port: " + listenSock.getLocalPort());
				Socket clientSock = listenSock.accept();
				System.out.println("\nConnection from : " + clientSock.getRemoteSocketAddress());
				
				// get incoming data string
				DataInputStream in = new DataInputStream(clientSock.getInputStream());
				retMsg = in.readUTF();
				System.out.println("\n" + retMsg + "\n");
				
//				if (Server.writeToDB()) {
//					System.out.println("Success!");
//				} else {
//					System.out.println("Failed...");
//				}
				
				// echo retMsg back to client
				// DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
				// out.writeUTF(retMsg + clientSock.getLocalSocketAddress());
				clientSock.close();
			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
}
