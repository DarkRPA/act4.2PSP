package psp.tematres.chatdam.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import psp.tematres.chatdam.client.UdpChatClient;
import psp.tematres.chatdam.util.Message;

//TODO: revisar, optimizar y documentar el código (JavaDoc)
public class ThreadChatServer extends Thread{
	private static ArrayList<UdpChatClient> udpClients=new ArrayList<UdpChatClient>();
	private ChatServer serverSocket;
	private ObjectInputStream fEntrada;
	private ObjectOutputStream fSalida;
	private Socket socket;

	public ThreadChatServer(ChatServer serverSocket, Socket socket) {
		this.serverSocket = serverSocket;
		this.socket = socket;
		try {
			this.fEntrada = new ObjectInputStream(socket.getInputStream());
			this.fSalida = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		//hay que leer los datos de identificación del cliente UDP
		//esperamos los datos del cliente UDP
		try {
			long count=0;
			Object peticion;
			boolean conexionCerrada = false;
			while(!conexionCerrada){
				peticion = fEntrada.readObject();
				if(peticion instanceof Message){
					//Debemos de comprobar el contenido del mensaje
					Message mensaje = (Message) peticion;
					if(mensaje.getUdpChatClientFrom() == null || mensaje.getUdpChatClientTo() == null){
						//Es un mensaje mal formado o un mensaje de comando
						switch (mensaje.gerMessage()) {
							case "getLista":
								//Quiere la lista por lo que se la devolvemos
								this.fSalida.writeObject(ThreadChatServer.udpClients);
								break;
						
							default:
								break;
						}
					}
				}else if(peticion instanceof UdpChatClient){
					//Es el cliente enseñando su saludo inicial
					UdpChatClient clienteReal = (UdpChatClient) peticion;
					if((count = ThreadChatServer.udpClients.stream().filter(e-> e.getNickName().equals(clienteReal.getNickName())).count()) == 0){
						//No es repetido
						ThreadChatServer.udpClients.add(clienteReal);
						this.fSalida.writeObject(ThreadChatServer.udpClients);
					}
				}
			}
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
	}
}
