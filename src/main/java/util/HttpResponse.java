package util;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	private DataOutputStream dos;
	private Map<String,String> header;

	public HttpResponse(OutputStream out) {
		// TODO Auto-generated constructor stub
        this.dos = new DataOutputStream(out);
        this.header = new HashMap<String,String>();
	}

	public void forward(String filename) {
		// TODO Auto-generated method stub
		// HTTP/1.1 200 OK 로 회신
		// Content-Length 헤더 삽입
		// js, css, html 확장자에 따라 헤더가 달라져야 함
		// filename 을 읽어서 body에 넣어줌
	}

	public void addHeader(String key, String value) {
		// TODO Auto-generated method stub
		this.header.put(key, value);
	}

	public void sendRedirect(String url) {
		// TODO Auto-generated method stub
		
	}
	
}
