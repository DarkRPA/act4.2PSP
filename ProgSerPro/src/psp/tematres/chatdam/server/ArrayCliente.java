package psp.tematres.chatdam.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import psp.tematres.chatdam.client.UdpChatClient;
/**
 * Clase para crear un array de todos los clientes del chat,
 * esta será la encargada de gestionar los usuarios
 * @author Ionut Razvan Neda & Daniel Caparros Duran
 * @version 1.0
 */
public class ArrayCliente implements Serializable{
    private static final long serialVersionUID = -8371543L;
    private ArrayList<UdpChatClient> clientes;

    public ArrayCliente(){
        this.clientes = new ArrayList<>();
    }

    public synchronized ArrayList<UdpChatClient> getClientes(){
        return this.clientes;
    }

    public void setClientes(ArrayList<UdpChatClient> clientes){
        this.clientes = clientes;
    }

    public synchronized void addCliente(UdpChatClient cliente){
        this.clientes.add(cliente);
    }

    public synchronized void removeCliente(UdpChatClient cliente){
        Iterator<UdpChatClient> iterador = this.clientes.iterator();

        while(iterador.hasNext()){
            UdpChatClient clienteActual = iterador.next();
            if(clienteActual.getNickName().equals(cliente.getNickName())){
                iterador.remove();
            }
        }
    }
    
}
