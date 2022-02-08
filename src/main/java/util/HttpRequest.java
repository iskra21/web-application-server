package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import webserver.RequestHandler;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private String method;
	private String url;
	private String httpVer;
	private Map<String,String> headers;
	private Map<String,String> params;
	
	public HttpRequest(InputStream in) throws IOException {
		BufferedReader ins = new BufferedReader(new InputStreamReader(in));
		
		String line = ins.readLine();
		if (line == null) {
			return;
		}
		
		String[] tokens = line.split(" ");
		this.method = tokens[0];
		this.url = tokens[1];
		this.httpVer = tokens[2];
		
    	this.headers = new HashMap<String,String>();
    	while(!Strings.isNullOrEmpty(line = ins.readLine())) {
    		HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
    		headers.put(pair.getKey(), pair.getValue());
    	}
    	
    	int index = this.url.indexOf('?');
    	if (index != -1) {
    		line = url.substring(index+1); // 이제 line에는 Query가 담기게 됨
    		this.url = url.substring(0, index);
    	}
    	
    	// 컨텐츠바디 읽어서 파싱하기
    	if (!Strings.isNullOrEmpty(line)) {
    		this.params = HttpRequestUtils.parseQueryString(line);    		
    	} else if ("POST".equals(this.method) && !Strings.isNullOrEmpty(line = getHeader("Content-Length"))) {
    		line = IOUtils.readData(ins, Integer.parseInt(line));
    		this.params = HttpRequestUtils.parseQueryString(line);
    	}
	}

    /**
     * @param getHeader(String Key)
     *            HTTP 헤더에서 찾을 Key 값을 넘겨 줌
     * @return Key 값에 매칭되는 value를 리턴함
     */	
	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	public String getParameter(String param) {
		return this.params.get(param);
	}

	public String getMethod() {
		return this.method;
	}
	
	public String getUrl() {
		return this.url;
	}
}
