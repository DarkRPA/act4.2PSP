package psp.tematres.chatdam.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import psp.tematres.chatdam.util.Message;
//TODO: revisar, optimizar y documentar el código (JavaDoc)
public class ChatClient {
	private final int SERVER_PORT=9999;
	private final String SERVER_ADDRESS="localhost";
	private UdpChatClient udpChatClient;
	private ArrayList<UdpChatClient> udpChatClients;
	private Socket socket;
	private ObjectOutputStream fSalida;
	private ObjectInputStream fEntrada;
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String nickName="";

		ChatClient chatClient = new ChatClient();

		System.out.print("Introduzca su nombre:");
		nickName = sc.next();

		if(chatClient.connect(nickName)) {
			//se obtiene la lista de clientes UDP para chatear
			try {
				chatClient.fSalida = new ObjectOutputStream(chatClient.socket.getOutputStream());
				chatClient.fSalida.writeObject(chatClient.udpChatClient);

				chatClient.fEntrada = new ObjectInputStream(chatClient.socket.getInputStream());
				chatClient.udpChatClients = (ArrayList<UdpChatClient>) chatClient.fEntrada.readObject();

				//chatClient.getUdpClients();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			chatClient.menu();
			System.out.println("Gracias por usar el servicio!");
		}
	}
	private void menu() {
		Scanner sc = new Scanner(System.in);
		int option=0;
		while(option!=4) {
			//TODO: definir opciones de menú para: 1. listar usuarios con los que chatear
			//2. seleccionar usuario con el chatear, 3. chatear con usuario seleccionado 
			//y 4. salir (finaliza el programa)
			switch(option) {
			case 1:
				try {
					this.getUdpClients();
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				System.out.println("> Usuarios en el chat");
				//se muestra la lista de clientes de chat con los que conversar
				this.udpChatClients.stream().filter(e->!e.getNickName()
						.equals(this.udpChatClient.getNickName())
						&& e.getUdpPort()!=this.udpChatClient.getUdpPort()).forEach(e->System.out.println(e.getNickName()
								+ this.udpChatClients.indexOf(e) + 1));
				break;
			case 2:
				
				if(this.udpChatClients.size()>1) { 
					System.out.print("Introduzca el nombre del usuario con el conversar:");
					String nickname = sc.next();
					this.udpChatClient = this.udpChatClients.stream().
							filter(e->e.getNickName().equals(nickname)).collect(Collectors.toList()).get(0);
				}else {
					System.out.println("No hay usuarios en el chat");
				}
				break;
			case 3:
				if(this.udpChatClient!=null) {
					//ya tenemos el cliente UDP con el que hablar
					//comienza la conversación
					this.chatWith(this.udpChatClient);
				}else {
					System.out.println("El usuario seleccionado no existe, inténtelo de nuevo!");
				}
				break;
			case 4:
				//TODO: terminar la sesión de chat
				System.out.println("Hasta luego");
					try {
						this.socket.close();
					} catch (IOException e1) {
					}
					System.exit(0);
				break;
			}
			option = sc.nextInt();
		}
}

public void getUdpClients() throws IOException, ClassNotFoundException{
	//Tenemos los flujos, unicamente debemos de comprobar el tipo de mensaje, no debe de tener mucha
	//mas dificultad
	Message mensaje = new Message(null, null, "getLista");
	this.fSalida.writeObject(mensaje);

	ArrayList<UdpChatClient> clientes = (ArrayList<UdpChatClient>) this.fEntrada.readObject();
	this.udpChatClients = clientes;
}

public boolean connect(String nickName) {
	String hostAddress;
	try {
		hostAddress =InetAddress.getLocalHost().getHostAddress();

		this.udpChatClient=new UdpChatClient(nickName,
				hostAddress);

		//preguntamos al servidor, conexión TCP, por la lista de clientes para el chat
		this.socket = new Socket(SERVER_ADDRESS,SERVER_PORT);
		//después de la conexión al servidor obtengo el puerto TCP en el client
		this.udpChatClient.setUdpPort(this.socket.getLocalPort());
		if(this.socket==null)return false;
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}
public void chatWith(UdpChatClient udpChatClientTo) {
	DatagramSocket socketUDP=null;
	try {
		socketUDP = new DatagramSocket();
		Scanner sc = new Scanner(System.in);
		String mensajePeticion="";
		byte[] mensajeRespuesta = new byte[1000];
		InetAddress hostServidor = 
				InetAddress.getByName(udpChatClientTo.getHostAddress());
		int puertoServidor = udpChatClientTo.getUdpPort();

		// Construimos el DatagramPacket que contendrá la 
		//respuesta
		while(!mensajeRespuesta.equals("*") || !mensajePeticion.equals("*")) {
			mensajePeticion = sc.next();
			//Construimos un datagrama para enviar el mensaje al 
			//servidor
			DatagramPacket peticion =
					new DatagramPacket(mensajePeticion.getBytes(), 
							mensajePeticion.length(), 
							hostServidor,puertoServidor);

			// Enviamos el datagrama
			socketUDP.send(peticion);

			DatagramPacket respuesta =
					new DatagramPacket(mensajeRespuesta, 
							mensajeRespuesta.toString().length());

			socketUDP.receive(respuesta);
			// Mostramos la respuesta del cliente a la salida 
			//estandar
			System.out.println("Respuesta: " + new 
					String(respuesta.getData()));
		}
	} catch (SocketException e) {
		System.out.println("Socket: " + e.getMessage());
	} catch (IOException e) {
		System.out.println("IO: " + e.getMessage());
	}finally {
		// Cerramos el socket
		if(socketUDP!=null)
			socketUDP.close();
	}
}
}
