package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.IOUtils;

public class RequestParser {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());
	
	private String action;
	private String uri;
	private String version;
	private HashMap<String,String> headers;
	private String contentsBody;
	
	public RequestParser(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String firstLine = br.readLine();
		if (firstLine == null) {
			return;
		}
		
		String[] splitted = firstLine.split(" ");
		this.action = splitted[0];
		this.uri = splitted[1];
		this.version = splitted[2];
		log.debug("HTTP Request[0]: Action={}, URI={}, Version={}", this.action, this.uri, this.version);
		
    	this.headers = new HashMap<String,String>();
    	int i = 1, index = 0;
    	String tmpLine;
    	while(!"".equals(tmpLine = br.readLine())) {
    		log.debug("HTTP Request[{}]: {}", i++, tmpLine);
    		index = tmpLine.indexOf(":");
    		this.headers.put(tmpLine.substring(0,index), tmpLine.substring(index+1).stripLeading());
    	}
    	log.debug(this.headers.toString());
    	
    	// contents Body 읽기
    	if ("POST".equals(this.action) && this.headers.containsKey("Content-Length")) {
    		log.debug("Length: {}", this.headers.get("Content-Length"));
    		this.contentsBody = IOUtils.readData(br, Integer.parseInt(this.headers.get("Content-Length")));
    	}
	}

	public String getMethod() {
		return this.action;
	}

	public String getUri() {
		return this.uri;
	}

	public String getContentsBody() {
		return this.contentsBody;
	}

	public String findKey(String string) {
		return this.headers.get(string);
	}
}
