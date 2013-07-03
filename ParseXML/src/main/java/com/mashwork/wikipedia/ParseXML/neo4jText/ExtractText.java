package com.mashwork.wikipedia.ParseXML.neo4jText;

import java.io.FileOutputStream;

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

import com.mashwork.wikipedia.ParseXML.MyCallBackHandler;
import com.mashwork.wikipedia.ParseXML.neo4j.NodeElementParser;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class ExtractText
{
	private  static GraphDatabaseService graphDb;
    private  static Index<Node> nodeIndex;
    private  static Index<Node> TocIndex;
    private  static Index<Node> fullTextIndex;
    
	public static void main(String[] args)
	{
		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
		//String dir = "/Users/Ricky/mashwork/wikidump/new/GameOfThrones.xml";
		String DBDir = "/Users/Ricky/mashwork/Anarchism_FULL_LUCENE_TEST";
		//String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_TEST";

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
        
		
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(dir);
		Transaction tx = graphDb.beginTx();
        
        try {
        	
        	
            TextExtracter TE = new TextExtracter(graphDb, nodeIndex,TocIndex, fullTextIndex, tx);
            
            //if you want to print the information to the screen, uncomment the following line
            //MBH.setPrintLink(true);
            //TE.setPrintStructure(true);
            //MBH.setPrintProgress(false);
            
            wxsp.setPageCallback(TE);
                
            long startTime = System.currentTimeMillis();
            
            wxsp.parse();
            
            
            tx.success();
            
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            
            System.out.println("Totally "+elapsedSeconds+" seconds used.");
            System.out.println("Page processed " + TextExtracter.pageProcessed);
        }catch(Exception e) {
                e.printStackTrace();
        }finally
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
