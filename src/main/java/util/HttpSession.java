package util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class HttpSession {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());	
	
	private String id;
	private int count;
	private Map<String,Object> attributes; 
	
	public HttpSession(String id) {
		this.id = id;
		this.count = 0;
		this.attributes = new HashMap<String,Object>();
	}

	public String getId() {
		return this.id;
	}

	public boolean isNew() {
		log.debug("count: {}", this.count);
		this.count++;
		return (this.count == 1) ? true : false;
	}
}
