package psp.tematres.chatdam.client;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * Clase para la configuración del cliente
 * @author Ionut Razvan Neda & Daniel Caparros Duran
 * @version 1.0
 */
public class UdpChatClient implements Serializable {
	private static final long serialVersionUID = -4244532618185868835L;
	private String nickName;
	private String hostAddress;
	private int udpPort;
	private LocalDateTime lastPost;
	public UdpChatClient(String nickName, String hostAddress) {
		this.nickName = nickName;
		this.hostAddress = hostAddress;
		this.lastPost = LocalDateTime.now();
	}
	public String getNickName() {
		return nickName;
	}
	public String getHostAddress() {
		return hostAddress;
	}
	public int getUdpPort() {
		return udpPort;
	}
	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}
	public LocalDateTime getLastPost() {
		return lastPost;
	}
	public void setLastPost(LocalDateTime lastPost) {
		this.lastPost = lastPost;
	}	
}
