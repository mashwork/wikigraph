package com.mashwork.wikipedia.ParseXML.serverNetty;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashwork.wikipedia.ParseXML.serverQuery.ServerInfo;
import com.mashwork.wikipedia.ParseXML.serverQuery.WikiServerQuery;


public class SearchAPIServerHandler extends SimpleChannelUpstreamHandler {
	
	private Logger logger = LoggerFactory.getLogger(SearchAPIServerHandler.class.getName());
	private final WikiServerQuery wikiServerQuery;
	private String address;
	private String port;
	
    public SearchAPIServerHandler(WikiServerQuery wikiServerQuery,String address,String port) {
    	this.wikiServerQuery = wikiServerQuery;
    	this.address = address;
    	this.port = port;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        }
        else {
            super.messageReceived(ctx, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {

        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (req.getUri().equals("/status")) {
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
            res.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            ChannelBuffer content = ChannelBuffers.copiedBuffer("UP", CharsetUtil.UTF_8);
            setContentLength(res, content.readableBytes());
            res.setContent(content);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (req.getUri().startsWith("/search")) {
            String uri = req.getUri();
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            Map<String, List<String>> args = decoder.getParameters();
            String help = ServerInfo.HelpInfo(address,port);
            	
            String output = query(args);
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
            if (req.getUri().startsWith("/search.html")) {
            	//output = json2html(output);
            	output = "<html><body><pre><code>"+help+"\n"+ output + "</code></pre></body></html>";
            	res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
            	}
            else {
            	output = help+"\n"+ output;
            	res.setHeader(CONTENT_TYPE, "text; charset=UTF-8");
            }
            	
            ChannelBuffer content = ChannelBuffers.copiedBuffer(output, CharsetUtil.UTF_8);
            setContentLength(res, content.readableBytes());
            res.setContent(content);
            sendHttpResponse(ctx, req, res);

            return;
        }

        // Send an error page otherwise.
        sendHttpResponse(
                ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(
                    ChannelBuffers.copiedBuffer(
                            res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    public String query(Map<String, List<String>> args)
	{
    	String method = null;
		String startNode = null;
		String endNode = null;
		int maxDepth = -1;
		boolean isPageOnly = true;
		int listLength = -1;
		List<String> nodeList = null;
    	
		String errorInfo = null;
		if(args.get("method")==null) return null;
		method = args.get("method").get(0);
		
		if(args.get("startNode")!=null)
		{
			startNode = args.get("startNode").get(0);
		}
		if(args.get("endNode")!=null)
		{
			endNode = args.get("endNode").get(0);
		}
		if(args.get("maxDepth")!=null)
		{
			maxDepth = Integer.parseInt(args.get("maxDepth").get(0));
		}
		if(args.get("isPageOnly")!=null)
		{
			if(args.get("isPageOnly").get(0).equals("false"))
			{
				isPageOnly = false;
			}
			else
			{
				isPageOnly = true;
			}
		}
		//int listLength = Integer.parseInt(args.get("listLength").get(0));
		if(args.get("nodeList")!=null)
		{
			nodeList = args.get("nodeList");
		}

		if(method.equals("findPath"))
		{
			 errorInfo = CheckCommand.isErrorCommand("123",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindPath(startNode,endNode,maxDepth);
			}
		}
		else if(method.equals("findShortestPaths"))
		{
			errorInfo = CheckCommand.isErrorCommand("123",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindShortestPaths(startNode,endNode,maxDepth);
			}
		}
		else if(method.equals("findCategories"))
		{
			errorInfo = CheckCommand.isErrorCommand("13",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindCategories(startNode,maxDepth);
			}
		}
		else if(method.equals("findFathers"))
		{
			errorInfo = CheckCommand.isErrorCommand("134",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindFathers(startNode,maxDepth,isPageOnly);
			}
		}
		else if(method.equals("findCommonAncestors"))
		{
			errorInfo = CheckCommand.isErrorCommand("356",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindCommonAncestor(nodeList,maxDepth);
			}
		}
		else if(method.equals("printQuerySuggestion"))
		{
			errorInfo = CheckCommand.isErrorCommand("1",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverQuerySuggestion(startNode);
			}
		}
		else if(method.equals("getComponentNumbers"))
		{
			errorInfo = CheckCommand.isErrorCommand("1",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverGetComponentNumbers(startNode);
			}
		}
		else if(method.equals("findRoot"))
		{
			errorInfo = CheckCommand.isErrorCommand("1",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindRoot(startNode);
			}
		}
		else if(method.equals("findPage"))
		{
			errorInfo = CheckCommand.isErrorCommand("1",args);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindPage(startNode);
			}
		}
		else
		{
			return CheckCommand.errorCommand(0);
		}
		
		
		return errorInfo;
	}
    
}
