package psp.tematres.chatdam.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import psp.tematres.chatdam.client.UdpChatClient;

/**
 * Configuramos el chat por la parte del servidor
 * @author Ionut Razvan Neda & Daniel Caparros Duran
 * @version 1.0
 */
public class ChatServer {
	private ArrayList<UdpChatClient> udpChatClients;
	private ServerSocket serverSocket;
	private boolean stopServer=false;

	public static void main(String[] args) throws IOException  {
		ChatServer chat = new ChatServer(9999);
		chat.start();
	}
	//Configuramos el servidor
	private ChatServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
	}
	public ArrayList<UdpChatClient> getUdpChatClients(){
		return this.udpChatClients;
	}
	public void setUdpChatClients(ArrayList<UdpChatClient> udpChatClients) {
		this.udpChatClients = udpChatClients;
	}
	//Iniciamos el servidor
	public void start() {
		System.out.println("Servidor...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!stopServer) {
					try {
						Socket socket = serverSocket.accept();
						ThreadChatServer hilo = new ThreadChatServer(ChatServer.this,socket);
						hilo.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
