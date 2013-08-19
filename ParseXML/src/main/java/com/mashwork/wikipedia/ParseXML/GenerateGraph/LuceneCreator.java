package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public class LuceneCreator
{
	private  static GraphDatabaseService graphDb;
    private  static Index<Node> nodeIndex;
    private  static Index<Node> TocIndex;
    private  static Index<Node> fullTextIndex;
	private  static BatchInserter inserter;
	
	public static void main(String[] args) throws Exception
	{
//		String DBDir = "/Users/Ricky/mashwork/ListCharac";
//		String DumpDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/ListCharacD1_Page.xml";
		String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_FullText";
		String DumpDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Pages.xml";
		
		
		//DBCreator DB = new DBCreator(DBDir);
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(DumpDir);
		
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

		
		Transaction tx = graphDb.beginTx();
        try
        {
        	long startTime = System.currentTimeMillis();
        	LuceneNodeCreator LNC = new LuceneNodeCreator(graphDb, 
        			nodeIndex,TocIndex, fullTextIndex, tx);
        	wxsp.setPageCallback(LNC);
        	System.out.println("Creating lucene.");
        	wxsp.parse();
        	System.out.println("Node counter " + LNC.pageProcessed);
        	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Totally "+elapsedSeconds+" seconds used to build lucene text into neo4j.");
        }
        finally
        {
        	tx.success();
            tx.finish();
        }
        shutdown();
        
        
        
        inserter = BatchInserters.inserter(DBDir);
        Map<String, String> config = new HashMap<String, String>();
		config.put( "read_only", "true" );
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
		        DBDir )
		        .setConfig( config )
		        .newGraphDatabase();

		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
        nodeIndex = graphDb.index().forNodes( "nodes" );
        TocIndex = graphDb.index().forNodes( "Toc" );
        fullTextIndex= graphDb.index().forNodes("full");
        registerShutdownHook();

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
