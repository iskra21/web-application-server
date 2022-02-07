package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());
	
	private String method;
	private String url;
	private String httpVersion;
	private Map<String,String> headers;
	private Map<String,String> params;
	
	public HttpRequest(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line = reader.readLine();
		if (line == null) {
			return;
		}
		
		String[] tokens = line.split(" ");
		this.method = tokens[0];
		this.url = tokens[1];
		this.httpVersion = tokens[2];
		//log.debug("HTTP Request[0]: Action={}, URI={}, Version={}", this.action, this.uri, this.version);
		
    	this.headers = new HashMap<String,String>();
    	while(!"".equals(line = reader.readLine())) {
    		HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
    		headers.put(pair.getKey(), pair.getValue());
    	}
    	/* int i = 1, index = 0;
    	String tmpLine;
    	while(!"".equals(tmpLine = br.readLine())) {
    		log.debug("HTTP Request[{}]: {}", i++, tmpLine);
    		index = tmpLine.indexOf(":");
    		this.headers.put(tmpLine.substring(0,index), tmpLine.substring(index+1).stripLeading());
    	}
    	log.debug(this.headers.toString()); */
    	
    	// 본문 읽기
    	int index = url.indexOf('?');
    	if ("POST".equals(this.method) && (line = getHeader("Content-Length")) != null) {
    		this.params = HttpRequestUtils.parseQueryString(IOUtils.readData(reader, Integer.parseInt(line)));
    	} else if ("GET".equals(this.method) && index != -1) {
    		this.params = HttpRequestUtils.parseQueryString(url.substring(index+1));
    	}
	}

	public String getMethod() {
		return this.method;
	}

	public String getUrl() {
		return this.url;
	}

	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	public String getParameter(String key) {
		return this.params.get(key);
	}
}
