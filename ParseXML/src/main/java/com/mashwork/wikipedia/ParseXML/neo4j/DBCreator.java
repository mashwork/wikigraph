package com.mashwork.wikipedia.ParseXML.neo4j;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;

@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public class DBCreator {
	private  static GraphDatabaseService graphDb;
    private  static Index<Node> nodeIndex;
    private  static Index<Node> TocIndex;
    private  static Index<Node> fullTextIndex;
	
    //private static final String USERNAME_KEY = "nodename";
    
//	public DBCreator(String DBDir)
//	{
//		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
//        nodeIndex = graphDb.index().forNodes( "nodes" );
//        registerShutdownHook();
//	}
	public static void main(String[] args) throws Exception
	{
		String DBDir = "/Users/Ricky/mashwork/AnarchismD2DB";
		//String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_Title";
		String XMLDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/Anarchism_D1_TOC_Equal.xml";
		//String XMLDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_TOC_Equal.xml";
		
		//DBCreator DB = new DBCreator(DBDir);
		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
        nodeIndex = graphDb.index().forNodes( "nodes" );
        TocIndex = graphDb.index().forNodes( "Toc" );
        
        Analyzer myAnalyzer = new SimpleAnalyzer(Version.LUCENE_36);
        String className = myAnalyzer.getClass().getName();
        myAnalyzer.close();
        fullTextIndex = graphDb.index().forNodes("full",
        		MapUtil.stringMap(IndexManager.PROVIDER,"lucene","type", "fulltext",
        				"to_lower_case", "true","analyzer",className));
        registerShutdownHook();
        
		
		//HierachyManager.Init();
		
		Transaction tx = graphDb.beginTx();
		
		NodeElementParser NEP = new NodeElementParser(graphDb, nodeIndex,TocIndex, fullTextIndex, tx);
		LinkElementParser LEP = new LinkElementParser(graphDb, nodeIndex,TocIndex, fullTextIndex, tx);
		
		
        try
        {
        	long startTime = System.currentTimeMillis();
        	
        	System.out.println("Creating tree Structure.");
        	NEP.parse(XMLDir);				//Generate tree structure first;
        	System.out.println("Node counter " + NodeElementParser.counter);
        	System.out.println("Link counter " + NodeElementParser.links);
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
