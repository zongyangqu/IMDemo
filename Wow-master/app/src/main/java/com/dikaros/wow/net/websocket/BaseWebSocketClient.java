package com.dikaros.wow.net.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * WebSocket基类
 * Created by Dikaros on 2016/5/14.
 */
public class BaseWebSocketClient extends WebSocketClient {

    /**
     * 显示构造方法，传入链接地址即可
     * @param uri
     * @throws URISyntaxException
     */
    public BaseWebSocketClient(String uri) throws URISyntaxException {
        super(new URI(uri), new Draft_17());

    }

    public interface WebSocketClientListener{
        public void onOpen();
        public void onMessage(String message);
        public void onClose(boolean remote,String reason);
    }
    //监听器接口
    private WebSocketClientListener webSocketListener;

    public BaseWebSocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public BaseWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        webSocketListener.onOpen();
    }

    @Override
    public void onMessage(String message) {
        webSocketListener.onMessage(message);

    }


    @Override
    public void onFragment(Framedata fragment) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        webSocketListener.onClose(remote,reason);
        close(code);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
//		System.out.println(ex.toString());
    }


    /**
     * @param webSocketListener 要设置的 webSocketListener
     */
    public void setWebSocketListener(WebSocketClientListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

}
