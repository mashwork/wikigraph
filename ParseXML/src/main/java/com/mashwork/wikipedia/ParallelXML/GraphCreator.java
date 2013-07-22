package com.mashwork.wikipedia.ParallelXML;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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

public class GraphCreator
{
	String XMLPrefix;
	String XMLDir;
	String DBDir;
	HashMap<String,Long> inMemoryIndex;
	
	GraphDatabaseService graphDb;
    Index<Node> nodeIndex;
    Index<Node> TocIndex;
    Index<Node> fullTextIndex;
	BatchInserter inserter;
    
    public GraphCreator(String XMLPrefix,String DBDir) throws Exception
    {
    	this.XMLPrefix = XMLPrefix;
    	this.DBDir = DBDir;
    	this.inMemoryIndex = new HashMap<String,Long>(14000000);
    }
    
    public void insertNodes(String XMLDir, int part)
    {
    	this.XMLDir = XMLDir;
    	this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
    	this.nodeIndex = graphDb.index().forNodes( "nodes" );
    	this.TocIndex = graphDb.index().forNodes( "Toc" );

        Analyzer myAnalyzer = new SimpleAnalyzer(Version.LUCENE_36);
        String className = myAnalyzer.getClass().getName();
        myAnalyzer.close();
        this.fullTextIndex = graphDb.index().forNodes("full",
        		MapUtil.stringMap(IndexManager.PROVIDER,"lucene","type", "fulltext",
        				"to_lower_case", "true","analyzer",className));
        registerShutdownHook();
        
    	long startTime = System.currentTimeMillis();
    	Transaction tx = graphDb.beginTx();
    	NodeElementParserMemoryIndex NEP = new NodeElementParserMemoryIndex(graphDb, 
    			nodeIndex,TocIndex, fullTextIndex, tx,inMemoryIndex,part);
    	System.out.println("Creating tree Structure.");
    	NEP.parse(XMLDir);
    	tx.finish();
    	System.out.println("Node counter " + NEP.counter);
    	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Totally "+elapsedSeconds+" seconds used to put pages and build lucene text into neo4j.");
        shutdown();
    }
    
    public void insertLinks(String XMLDir, int part)
    {
    	this.XMLDir = XMLDir;
    	this.inserter = BatchInserters.inserter(DBDir);
    	Map<String, String> config = new HashMap<String, String>();
		config.put( "read_only", "true" );
		this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
		        DBDir )
		        .setConfig( config )
		        .newGraphDatabase();
    	this.nodeIndex = graphDb.index().forNodes( "nodes" );
    	this.TocIndex = graphDb.index().forNodes( "Toc" );

        Analyzer myAnalyzer = new SimpleAnalyzer(Version.LUCENE_36);
        String className = myAnalyzer.getClass().getName();
        myAnalyzer.close();
        this.fullTextIndex = graphDb.index().forNodes("full",
        		MapUtil.stringMap(IndexManager.PROVIDER,"lucene","type", "fulltext",
        				"to_lower_case", "true","analyzer",className));
        registerShutdownHook();
        
    	long startTime = System.currentTimeMillis();
    	Transaction tx = graphDb.beginTx();
    	LinkElementParserMemoryIndex LEP = new LinkElementParserMemoryIndex(graphDb, 
    			nodeIndex,TocIndex, fullTextIndex, tx,inMemoryIndex,inserter,part);
    	System.out.println("Creating links.");
    	LEP.parse(XMLDir);
    	tx.finish();
    	inserter.shutdown();
    	System.out.println("Link counter " + LEP.linkCount);
    	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Totally "+elapsedSeconds+" seconds used to create links into neo4j.");
        shutdown();
    }
    
    private void shutdown()
    {
        graphDb.shutdown();
    }
	
	private void registerShutdownHook()
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
	
	public String getXMLDir(int i)
	{
		return XMLPrefix+"-"+i+"-structure.xml";
	}
	
	public void writeInMemoryIndex(String Dir)
	{
		try
		{
			FileWriter FW = new FileWriter(Dir);
			Iterator<String> it = inMemoryIndex.keySet().iterator();
			while(it.hasNext())
			{
				String nodeName = it.next();
				FW.write(nodeName+"\n");
				FW.write(inMemoryIndex.get(nodeName)+"\n");
			}
			FW.close();
		}catch(IOException e)
		{
			System.out.println("Trying to write hashmap into disk! ");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		String XMLPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki";
		String DBDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
		GraphCreator graphCreator = new GraphCreator(XMLPrefix,DBDir);
		for(int i = 3;i <= 8;i++)
		{
			String XMLDir = graphCreator.getXMLDir(i);
			graphCreator.insertNodes(XMLDir,i);
		}
		
		String inMemoryIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/hashMap";
		graphCreator.writeInMemoryIndex(inMemoryIndexDir);
		
//		for(int i = 1;i <= 8;i++)
//		{
//			String XMLDir = graphCreator.getXMLDir(i);
//			graphCreator.insertLinks(XMLDir,i);
//		}
		
	}
}
