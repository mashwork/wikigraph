package com.mashwork.wikipedia.ParseXML.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class DBCreator {
	private  static GraphDatabaseService graphDb;
    private  static Index<Node> nodeIndex;
    private  static Index<Node> TocIndex;
	
    //private static final String USERNAME_KEY = "nodename";
    
//	public DBCreator(String DBDir)
//	{
//		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
//        nodeIndex = graphDb.index().forNodes( "nodes" );
//        registerShutdownHook();
//	}
	public static void main(String[] args) throws Exception
	{
		//String DBDir = "/Users/Ricky/mashwork/AnarchismD2DB";
		String DBDir = "/Users/Ricky/mashwork/GOT_D4_DB";
		//String XMLDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/Anarchism_D2_TOC_Equal.xml";
		String XMLDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/GOT_D4_TOC_Equal.xml";
		
		//DBCreator DB = new DBCreator(DBDir);
		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
        nodeIndex = graphDb.index().forNodes( "nodes" );
        TocIndex = graphDb.index().forNodes( "Toc" );
        registerShutdownHook();
        
		NodeElementParser NEP = new NodeElementParser(graphDb, nodeIndex,TocIndex);
		LinkElementParser LEP = new LinkElementParser(graphDb, nodeIndex,TocIndex);
		
		//HierachyManager.Init();
		
		Transaction tx = graphDb.beginTx();
        try
        {
        	long startTime = System.currentTimeMillis();
        	
        	System.out.println("Creating tree Structure.");
        	NEP.parse(XMLDir);				//Generate tree structure first;
        	System.out.println("Node counter" + NodeElementParser.counter);
        	System.out.println();
        	System.out.println("Tree structure generated. Start to link the nodes....");
        	System.out.println();
        	LEP.parse(XMLDir);				//Then create the links between the nodes.
        	tx.success();
        	
        	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Totally "+elapsedSeconds+" seconds used to put pages and links into neo4j.");
            System.out.println("Link not found: "+TOCLocator.linkNotFound);
        }
        finally
        {
            tx.finish();
        }
        System.out.println( "Shutting down database ..." );
        shutdown();

	}
	
	private static void shutdown()
    {
        graphDb.shutdown();
    }
	
	private static void registerShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                shutdown();
            }
        } );
    }
}
