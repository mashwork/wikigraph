package com.mashwork.wikipedia.ParseXML.serverNetty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashwork.wikipedia.ParseXML.serverQuery.WikiServerQuery;

public class SearchAPIServer {
	
	private static Logger logger = LoggerFactory.getLogger(SearchAPIServer.class.getName());
    private String DBDir;
    private String serverAddress;
	private String serverPort;
    private ServerBootstrap bootstrap;
    private ChannelGroup allChannels;
    private WikiServerQuery wikiServerQuery;

    //@SuppressWarnings("unchecked")
	public SearchAPIServer(String[] args) throws Exception {
    	OptionParser parser = new OptionParser();
    	OptionSpec<String> input =
                parser.accepts("input-dir").withRequiredArg().ofType(String.class)
                //.defaultsTo("/Users/Ricky/mashwork/Anarchism_D2_FULL_LUCENE_TEST");
                .defaultsTo("/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_TEST");
    	
    	OptionSpec<String> port =
                parser.accepts("port").withRequiredArg().ofType(String.class).defaultsTo("8080");
    	
    	OptionSpec<String> address =
                parser.accepts("address").withRequiredArg().ofType(String.class).defaultsTo("localHost");
    	
    	//OptionSet options = parser.parse("--input-dir","--address","--port");
    	OptionSet options = parser.parse(args);
    	
    	DBDir = input.value(options);
    	serverAddress = address.value(options);
    	serverPort = port.value(options);
    	
    	try {
    		wikiServerQuery = new WikiServerQuery(DBDir);
    	} catch (Exception e) {
    		logger.error("Error: {}. Failed to initialize the wiki database, and quit ...", e.getMessage());
    		e.printStackTrace();
    		System.exit(1);
    	}
    }
    
    public boolean start() {
        ChannelFactory factory = new NioServerSocketChannelFactory(
        		                 Executors.newCachedThreadPool(), 
        		                 Executors.newCachedThreadPool());
        allChannels = new DefaultChannelGroup("SearchAPIServer");
        
        bootstrap = new ServerBootstrap(factory);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        /* XXX: buffer size most likely wrong if search result is very big */
        bootstrap.setOption("child.sendBufferSize", 1048576);
        bootstrap.setOption("receiveBufferSize", 1048576);
        
        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder(12288, 12288, 12288));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new SearchAPIServerHandler(wikiServerQuery,serverAddress,serverPort));
                return pipeline;
            }
        });
        // Bind and start to accept incoming connections.
        Channel channel = bootstrap.bind(new InetSocketAddress(Integer.parseInt(serverPort)));
        
        if (channel.isBound()) {
        	logger.info("SERVER - bound to *:{}", serverPort);
        	allChannels.add(channel);
        	return true;
        }
        else {
        	logger.error("SERVER - failed to bind to *:{}", serverPort);
        	bootstrap.releaseExternalResources();
        	return false;
        }
    }

    public void stop() {
        allChannels.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
        logger.info("SERVER stopped ...");
    }
    
    public static void main(String[] args) throws Exception {
    	final SearchAPIServer server = new SearchAPIServer(args);
    	boolean serverStarted = server.start();
    	if (!serverStarted) {
    		logger.error("Failed to start SearchAPIServer");
    		System.exit(1);
    	}
    	
    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    		public void run() {
    			server.stop();
    		}
    	}, "serverShutdownHook"));
    }
}
