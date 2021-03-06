package es.ucm.fdi.iw.control;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * A simple text socket handler.
 * 
 * @author mfreire
 */
public class IwSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LogManager.getLogger(RootController.class);
	
	/**
	 * A map from (logged-in) users to their open sockets. 
	 */
	private static final Map<String, SocketHolder> users = new ConcurrentHashMap<>();
	
	/** 
	 * An internal class to avoid concurrency problems with websockets
	 */
	public static class SocketHolder {
		// see https://stackoverflow.com/a/48616726/15472
		private volatile WebSocketSession socket;
		public SocketHolder(WebSocketSession socket) {
			this.socket = socket;
		}
		public WebSocketSession getSocket() {
			return socket;
		}
	}
	
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
    	String userName = session.getPrincipal().getName();
    	log.info("received message: " 
    			+ message.getPayload() + " from " 
    			+ userName);

    	// do something with message; for example, re-send it
    	String payload = message.getPayload().substring(0, message.getPayload().length() - 2);
    	
    	// Pattern pattern = Pattern.compile("\\s([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6})$", Pattern.CASE_INSENSITIVE);
    	// Matcher matcher = pattern.matcher(payload);
    	// String destination = matcher.group();
    	
    	if (users.containsKey(userName)) {
    		for (Map.Entry<String, IwSocketHandler.SocketHolder> user : users.entrySet()) {
    			sendText(user.getKey(), userName + " : " + payload);
    		}
    		String groupIdString = message.getPayload().substring(message.getPayload().length() - 1, message.getPayload().length());
    		
    		Long groupId = Long.getLong(groupIdString);
    		
    	}
    
    	
    }
    
    /**
     * Attempts to send text to a user.
     * 
     * @param userName to send text to. 
     * Must be logged in, and have an (open) websocket
     * @param text to send
     * @return true if sent, false if userName has no active websocket, or
     *    sending failed somehow.
     */
    public boolean sendText(String userName, String text) {
    	SocketHolder sh = users.get(userName);
    	if (sh == null) {
    		log.info("No such user, not sending message: {}", userName);
    		return false;
    	}
    	
    	try {
    		synchronized (sh) {
    			// no two threads can send on the same socket at once
    			sh.getSocket().sendMessage(new TextMessage(text));
    		}
		} catch (Exception e) {
			log.warn("Could not send out message to " + userName + ":", e);
			return false;
		}
    	return true;
    }
        
    /**
     * Called when socket first connected.
     * 
     * Registers username with socket. 
     */
    @Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	super.afterConnectionEstablished(session);
		String userName = session.getPrincipal().getName();
		log.info("User enters: {}", userName);
    	users.put(userName, new SocketHolder(session));
	}
    
    /**
     * Called when socket disconnected.
     * 
     * Un-registers username with socket.
     */
    @Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		String userName = session.getPrincipal().getName();
		log.info("User leaves: {}; status now {}, reason {}", userName, status.getCode(), status.getReason());
    	users.remove(userName);
	}
}