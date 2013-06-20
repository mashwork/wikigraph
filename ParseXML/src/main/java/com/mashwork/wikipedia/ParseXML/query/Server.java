package com.mashwork.wikipedia.ParseXML.query;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.*;
import org.vertx.java.deploy.Verticle;

import java.util.List;
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
	    	sb.append(ServerInfo.HelpInfo());
	        for (Map.Entry<String, String> param: commands.entrySet()) 
	        {
	            sb.append(param.getKey()).append(":").append(param.getValue()).append("\n");
	        }
	        
	        
	        String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB";
	    	WikiQuery wikiQuery = new WikiQuery(DBDir);
	    	sb.append("DB set up. Start to search.\n");
	    	List<String> names = wikiQuery.findPath("Game of Thrones","War of the Roses",20);
	    	sb.append(wikiQuery.resultToString(names));
	        
		request.response.end(sb.toString()); 
		//request.response.end("Hello World!");
	    }
  };
  
  	
    server.requestHandler(HD).listen(8080, "localhost");
  }
  
}




