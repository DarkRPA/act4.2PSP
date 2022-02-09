package psp.tematres.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * Servidor basado en sockets para el puerto 6789. Espera la conexión
 * de un cliente, obtiene el flujo de lectura y escritura del socket
 * abierto. Después escucha en la entrada de lectura a que el cliente
 * le escriba algo. Toma la información recibida del cliente y la
 * convierte a mayúsculas y lo devuelve al cliente
 * @author rafa
 * @version 1.0
 */
public class TcpServer {

	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(6789);

		while(true) {
			//el servidor queda a la espera de una conexión
			Socket connectionSocket = welcomeSocket.accept();
			//se ha recibido una conexión del lado del cliente
			//se obtiene el flujo de lectura para escuchar al cliente
			BufferedReader inFromClient =
					new BufferedReader(new 
							InputStreamReader(connectionSocket.
									getInputStream()));
			//se obtiene el flujo de escritura para hablar con el 
			//cliente
			DataOutputStream outToClient = new 
					DataOutputStream(connectionSocket.
							getOutputStream());
			//se escucha al cliente
			clientSentence = inFromClient.readLine();
			//el cliente ha hablado y la información se convierte a mayúsculas
			//y se responde al cliente
			System.out.println("Received: " + clientSentence);
			capitalizedSentence = clientSentence.toUpperCase() + 
					"\n";
			//envío de la respuesta al cliente
			outToClient.writeBytes(capitalizedSentence);
		}
	}
}


