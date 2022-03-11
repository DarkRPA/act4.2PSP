package psp.tematres.chatdam.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import psp.tematres.chatdam.util.Message;
//TODO: revisar, optimizar y documentar el código (JavaDoc)
public class ChatClient {
	private final int SERVER_PORT=9999;
	private final String SERVER_ADDRESS="localhost";
	private UdpChatClient localClient;
	private UdpChatClient udpChatClient;
	private List<UdpChatClient> udpChatClients;
	private Socket socket;
	private ObjectOutputStream fSalida;
	private ObjectInputStream fEntrada;
	
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
						.equals(this.localClient.getNickName())
						&& e.getUdpPort()!=this.localClient.getUdpPort()).forEach(e->System.out.println(e.getNickName()
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
		sc.close();
}

public void getUdpClients() throws IOException, ClassNotFoundException{
	//Tenemos los flujos, unicamente debemos de comprobar el tipo de mensaje, no debe de tener mucha
	//mas dificultad
	Message mensaje = new Message(null, null, "getLista");
	this.fSalida.writeObject(mensaje);

	List<UdpChatClient> clientes = (List<UdpChatClient>) this.fEntrada.readObject();
	this.udpChatClients = clientes;
}

public boolean connect(String nickName) {
	String hostAddress;
	try {
		hostAddress =InetAddress.getLocalHost().getHostAddress();

		this.localClient=new UdpChatClient(nickName,
				hostAddress);

		//preguntamos al servidor, conexión TCP, por la lista de clientes para el chat
		this.socket = new Socket(SERVER_ADDRESS,SERVER_PORT);
		//después de la conexión al servidor obtengo el puerto TCP en el client
		this.localClient.setUdpPort(this.socket.getLocalPort());
		if(this.socket==null)return false;
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

public void leer(){
	
	Thread hiloTemp = new Thread(() -> {
		//Aqui debemos de escuchar por peticiones externas
		try {
			DatagramSocket socketUdp = new DatagramSocket(this.localClient.getUdpPort());

			while(!ChatClient.this.socket.isClosed()){
				byte[] buffer = new byte[1000];
				//Mientras no se haya cerrado el socket leemos
				DatagramPacket paqueteRespuesta = new DatagramPacket(buffer, buffer.length);
				socketUdp.receive(paqueteRespuesta);
				
				ByteArrayInputStream inputStream = new ByteArrayInputStream(paqueteRespuesta.getData());
				ObjectInputStream lector = new ObjectInputStream(inputStream);

				Message mensaje = null;
				try {
					mensaje = (Message)lector.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("Mensaje de "+mensaje.getUdpChatClientFrom().getNickName()+": "+mensaje.gerMessage());
			}
			//Ahora debemos de leer todos los mensajes que podamos recibir
		} catch (IOException e) {
			e.printStackTrace();
		}

	});

	hiloTemp.start();
}

public void chatWith(UdpChatClient udpChatClientTo) {
	DatagramSocket socketUDP=null;
	try {
		socketUDP = new DatagramSocket();
		Scanner sc = new Scanner(System.in);
		String mensajePeticion="";
		InetAddress hostServidor = 
				InetAddress.getByName(udpChatClientTo.getHostAddress());
		int puertoServidor = udpChatClientTo.getUdpPort();

		// Construimos el DatagramPacket que contendrá la 
		//respuesta
		while(!mensajePeticion.equals("*")) {
			mensajePeticion = sc.nextLine();
			//Construimos un datagrama para enviar el mensaje al 
			//servidor

			//Vamos a intentar pasar el mensaje como tal para tener informacion de todo en general
			ByteArrayOutputStream conversor = new ByteArrayOutputStream(1000);
			ObjectOutputStream escritor = new ObjectOutputStream(conversor);

			Message mensaje = new Message(this.localClient, this.udpChatClient, mensajePeticion);
			escritor.writeObject(mensaje);

			byte[] resultado = conversor.toByteArray();

			DatagramPacket peticion = new DatagramPacket(resultado, resultado.length, hostServidor,puertoServidor);

			// Enviamos el datagrama
			socketUDP.send(peticion);

			//DatagramPacket respuesta = new DatagramPacket(mensajeRespuesta, mensajeRespuesta.toString().length());

			//socketUDP.receive(respuesta);
			// Mostramos la respuesta del cliente a la salida 
			//estandar
			//System.out.println("Respuesta: " + new String(respuesta.getData()));
		}
		sc.close();

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
