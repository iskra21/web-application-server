package util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class HttpSession {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());	
	
	private String id;
	private Map<String,Object> attributes = new HashMap<String,Object>(); 
	
	public HttpSession(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
	
	public void setAttribute(String name, Object obj) {
		attributes.put(name, obj);
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public void invalidate() {
		HttpSessions.remove(this.id);
	}
}
