package org.wlf.java_websocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * @author wlf(Andy)
 * @datetime 2016-02-16 09:28 GMT+8
 * @email 411086563@qq.com
 */
public class ChatServer extends WebSocketServer {

	public ChatServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
	}

	public ChatServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {

		sendToAll(conn.getRemoteSocketAddress().getAddress().getHostAddress()
				+ " 进入房间 ！");

		System.out.println(conn.getRemoteSocketAddress().getAddress()
				.getHostAddress()
				+ " 进入房间 ！");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {

		sendToAll(conn.getRemoteSocketAddress().getAddress().getHostAddress()
				+ " 离开房间 ！");

		System.out.println(conn.getRemoteSocketAddress().getAddress()
				.getHostAddress()
				+ " 离开房间 ！");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {

		sendToAll("["
				+ conn.getRemoteSocketAddress().getAddress().getHostAddress()
				+ "]" + message);

		System.out.println("["
				+ conn.getRemoteSocketAddress().getAddress().getHostAddress()
				+ "]" + message);
	}

	@Override
	public void onError(WebSocket conn, Exception e) {
		e.printStackTrace();
		if (conn != null) {
			conn.close();
		}
	}

	// 发送给所有的聊天者
	private void sendToAll(String text) {
		Collection<WebSocket> conns = connections();
		synchronized (conns) {
			for (WebSocket client : conns) {
				client.send(text);
			}
		}
	}

	// 测试
	public static void main(String[] args) throws InterruptedException,
			IOException {

		int port = 8887;

		ChatServer server = new ChatServer(port);
		server.start();

		System.out.println("房间已开启，等待客户端接入，端口号: " + server.getPort());

		BufferedReader webSocketIn = new BufferedReader(new InputStreamReader(
				System.in));

		while (true) {
			String stringIn = webSocketIn.readLine();
			server.sendToAll(stringIn);
		}
	}
}
