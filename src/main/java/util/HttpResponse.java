package util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());	
	
	private DataOutputStream dos;
	private Map<String,String> header;

	public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
        this.header = new HashMap<String,String>();
	}

	public void forward(String url) {
		try {
			byte[] body = Files.readAllBytes(new File(url).toPath());
			if (url.endsWith(".css")) {
				addHeader("Content-Type", "text/css");
			} else if (url.endsWith(".js")) {
				addHeader("Content-Type", "application/javascript");
			} else if (url.endsWith(".html")) {
				addHeader("Content-Type", "text/html;charset=utf-8");
			}
			addHeader("Content-Length", body.length+"");
			response200Header();
			responseBody(body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public void forwardBody(String bodyContent) {
		byte[] body = bodyContent.getBytes();
		addHeader("Content-Type", "text/html;charset=utf-8");
		addHeader("Content-Length", body.length+"");
		response200Header();
		responseBody(body);
	}

	public void addHeader(String key, String value) {
		this.header.put(key, value);
	}

	public void sendRedirect(String redirectUrl) {
		try {
			addHeader("Location", redirectUrl);
	        this.dos.writeBytes("HTTP/1.1 302 Found \r\n");
	        processHeaders();
	        this.dos.flush();			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header() {
        try {
            this.dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
    	try {
    		Set<String> keySet = header.keySet();
    		for (String key:keySet) {
    			this.dos.writeBytes(key + ": " + header.get(key) + " \r\n");
    		}
        	this.dos.writeBytes("\r\n");    		
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
	}

	private void responseBody(byte[] body) {
        try {
            this.dos.write(body, 0, body.length);
            this.dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
	
}
