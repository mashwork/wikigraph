package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

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

import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class StructureCreatorNotBatch
{
	private GraphDatabaseService graphDb;
    private Index<Node> nodeIndex;
    private Index<Node> TocIndex;
    private Index<Node> fullTextIndex;
    private WikiXMLParser wxsp;
    private String XMLDir;
	//private  static Map<String, Long> inMemoryIndex;
	
    public StructureCreatorNotBatch(String DumpDir, String DBDir) throws Exception
    {
    	this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
        this.nodeIndex = graphDb.index().forNodes( "nodes" );
        this.TocIndex = graphDb.index().forNodes( "Toc" );

        Analyzer myAnalyzer = new SimpleAnalyzer(Version.LUCENE_36);
        String className = myAnalyzer.getClass().getName();
        myAnalyzer.close();
        this.fullTextIndex = graphDb.index().forNodes("full",
        		MapUtil.stringMap(IndexManager.PROVIDER,"lucene","type", "fulltext",
        				"to_lower_case", "true","analyzer",className));
        this.wxsp = WikiXMLParserFactory.getSAXParser(DumpDir);
        this.XMLDir = DumpDir.split("\\.")[0]+"_XML.xml";
        
        registerShutdownHook();
    }
    
    private void insertNodes() throws Exception
    {
    	Transaction tx = graphDb.beginTx();
    	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    	XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(XMLDir), "UTF-8");
        try
        {
            writer.writeStartDocument();
            writer.writeStartElement("d");
        	long startTime = System.currentTimeMillis();
        	NodeElementCreator NEC = new NodeElementCreator(graphDb, 
        			nodeIndex,TocIndex, fullTextIndex, tx,writer);
        	wxsp.setPageCallback(NEC);
        	System.out.println("Creating tree Structure.");
        	wxsp.parse();
        	System.out.println("Node counter " + NEC.pageProcessed);
        	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Totally "+elapsedSeconds+" seconds used to put pages and build lucene text into neo4j.");
        }
        finally
        {
        	writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        	tx.success();
            tx.finish();
        }
    }
    
    private void insertLinks() throws Exception
    {
    	Transaction tx2 = graphDb.beginTx();
    	try
        {
        	long startTime = System.currentTimeMillis();
        	LinkElementCreatorNotBatch LEC = new LinkElementCreatorNotBatch(graphDb, nodeIndex
        			,TocIndex, fullTextIndex, tx2);
        	wxsp.setPageCallback(LEC);
        	System.out.println("\nCreating links.");
        	wxsp.parse();
        	System.out.println("Link counter: " + LEC.linkCount);
        	System.out.println("Bad link counter: " + LEC.badLinkCount);
        	//tx2.success();     	
        	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Totally "+elapsedSeconds+" seconds used to create links.");
        }
        finally
        {
        	tx2.success();
            tx2.finish();
        }
        System.out.println( "Shutting down database ..." );
    }
    
	public static void main(String[] args) throws Exception
	{
//		String DBDir = "/Users/Ricky/mashwork/Anarchism_test_Pages";
//		String DumpDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/Anarchism_test_Pages.xml";
		String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_Structure";
		String DumpDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Pages.xml";
		
		
		StructureCreatorNotBatch structureCreator = new StructureCreatorNotBatch(DumpDir,DBDir);
		
		structureCreator.insertNodes();
		
		//structureCreator.insertLinks();
			
		structureCreator.shutdown();
        
        

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
}
