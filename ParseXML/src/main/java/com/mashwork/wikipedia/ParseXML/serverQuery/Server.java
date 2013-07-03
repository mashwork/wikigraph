package com.mashwork.wikipedia.ParseXML.serverQuery;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.*;
import org.vertx.java.deploy.Verticle;

import java.util.Map;

public class Server extends Verticle 
{
  public void start() 
  {
	
    HttpServer server = vertx.createHttpServer();
    Handler<HttpServerRequest> HD = new Handler<HttpServerRequest>()
    {
	    public void handle(HttpServerRequest request) 
	    {
	        Map<String, String> commands = request.params();
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("Below is your query input:\n");
	        for (Map.Entry<String, String> param: commands.entrySet()) 
	        {
	            sb.append(param.getKey()).append(":").append(param.getValue()).append("  ");
	        }
	        sb.append("\n");
	    	sb.append(ServerInfo.HelpInfo("localhost","8080"));
	    	sb.append("\nBelow is the result:\n");
	        sb.append(ServerHandler.query(commands));
	        
	        request.response.end(sb.toString()); 
	    }
  };
  
  	
    server.requestHandler(HD).listen(8080, "localhost");
  }
  
}




