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
/**
 * Clase para la configuración de la parte del cliente de un chat
 * @author Ionut Razvan Neda & Daniel Caparros Duran
 * @version 1.0
 */
public class ChatClient {
	private final int SERVER_PORT=9999;
	private final String SERVER_ADDRESS="localhost";
	private UdpChatClient localClient;
	private UdpChatClient udpChatClient;
	private List<UdpChatClient> udpChatClients;
	private Socket socket;
	private ObjectOutputStream fSalida;
	private ObjectInputStream fEntrada;
	public static void main(String[] args) {
		//Creamos un Scanner para pedir el Nickname al usuario
		Scanner sc = new Scanner(System.in);
		String nickName="";
		//Creamos un objeto del cliente
		ChatClient chatClient = new ChatClient();
		//Pedimos que introduzca un nombre válido al usuario
		System.out.print("Introduzca su nombre:");
		boolean acabado = false;
		while(!acabado){
			String nombre = sc.next();
			if(chatClient.connect(nombre)){
				acabado = true;
				continue;
			}
			//En caso de ya estar repetido el nombre lo indicamos
			System.out.println("El nombre ya está repetido, por favor elija otro nombre");
		}
		//Si el nombre se ha introducido de manera correcta el usuario ya puede acceder
		System.out.println("Ya puede usar el programa, use 1 para listar los usuarios 2 para elegir un usuario 3 para hablar con ese usuario y 4 para terminar la aplicacion");
		chatClient.leer();
		chatClient.menu();

		sc.close();
	}
	private void menu() {
		Scanner sc = new Scanner(System.in);
		int option=0;
		while(option!=-1) {
			//1. listar usuarios con los que chatear
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
				//terminar la sesión de chat
				System.out.println("Hasta luego");
				//Debemos de enviar al servidor que se va a salir
				try {
					Message mensajeCerrar = new Message(this.localClient, null, "desconectar");
					this.fSalida.writeObject(mensajeCerrar);
					this.fSalida.reset();
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
	this.fSalida.reset();

	if(this.fEntrada == null){
		this.fEntrada = new ObjectInputStream(this.socket.getInputStream());
	}

	List<UdpChatClient> clientes = (List<UdpChatClient>) this.fEntrada.readObject();
	this.udpChatClients = clientes;
}

public boolean connect(String nickName) {
	String hostAddress;
	try {
		hostAddress =InetAddress.getLocalHost().getHostAddress();

		this.localClient=new UdpChatClient(nickName, hostAddress);

		if(this.socket == null){
			//Establecemos la conexion aunque el cliente no aparecerá en ningún lado pues aún no envió su información
			this.socket = new Socket(SERVER_ADDRESS,SERVER_PORT);			
			this.fSalida = new ObjectOutputStream(this.socket.getOutputStream());
		}
		//this.fEntrada = new ObjectInputStream(this.socket.getInputStream());
		this.getUdpClients();
		int cantIguales = (int) this.udpChatClients.stream().filter(e -> e.getNickName().equals(nickName)).count();
		if(cantIguales > 0){
			return false;
		}

		this.fSalida.writeObject(this.localClient);
		//preguntamos al servidor, conexión TCP, por la lista de clientes para el chat
		//después de la conexión al servidor obtengo el puerto TCP en el client
		this.localClient.setUdpPort(this.socket.getLocalPort());
	} catch (IOException | ClassNotFoundException e) {
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
