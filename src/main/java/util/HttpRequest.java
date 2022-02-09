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
		
    	this.headers = new HashMap<String,String>();
    	while(!"".equals(line = reader.readLine())) {
    		HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
    		headers.put(pair.getKey(), pair.getValue());
    	}
    	
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