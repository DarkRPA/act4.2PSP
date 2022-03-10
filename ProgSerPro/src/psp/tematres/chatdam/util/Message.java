package psp.tematres.chatdam.util;

import java.io.Serializable;

import psp.tematres.chatdam.client.UdpChatClient;
//TODO: revisar, optimizar y documentar el código (JavaDoc)
public class Message implements Serializable{
	private static final long serialVersionUID = -1222334L;
	private UdpChatClient udpChatClientFrom,udpChatClientTo;
	private String message;
	public Message(UdpChatClient udpChatClientFrom,
			UdpChatClient udpChatClientTo,
			String message) {
		this.udpChatClientFrom=udpChatClientFrom;
		this.udpChatClientTo=udpChatClientTo;
		this.message = message;
	}
	public String gerMessage() {
		return this.message;
	}
	public UdpChatClient getUdpChatClientFrom() {
		return this.udpChatClientFrom;
	}
	public UdpChatClient getUdpChatClientTo() {
		return this.udpChatClientTo;
	}
}
