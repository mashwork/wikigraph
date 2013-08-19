package com.mashwork.wikipedia.ParallelXML;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * this code is used to insert the wikidump into neo4j graph database.
 * make sure the input xml file is the preprocessed xml, not the original dump.
 * the input xml will only contain Page-TableOfContent-Link information.
 */
public class GraphCreator
{
	String XMLPrefix;
	String XMLDir;
	String DBDir;
	HashMap<String,Long> inMemoryIndex;
    
    public GraphCreator(String XMLPrefix,String DBDir) throws Exception
    {
    	this.XMLPrefix = XMLPrefix;
    	this.DBDir = DBDir;
    	this.inMemoryIndex = new HashMap<String,Long>(14000000);

    }
    
    /**
     *create a NodeElementParserMemoryIndex class. That class will add page nodes
     */
    public void insertNodes(String XMLDir, int part)
    {
    	this.XMLDir = XMLDir;
    	long startTime = System.currentTimeMillis();
    	NodeElementParserMemoryIndex NEP = new NodeElementParserMemoryIndex(DBDir,inMemoryIndex);
    	System.out.println("Creating tree Structure.");
    	NEP.parse(XMLDir,part);
    	System.out.println("Node counter " + NEP.counter);
    	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Totally "+elapsedSeconds+" seconds used to put pages and build lucene text into neo4j.");
        NEP.shutdown();
    }
    
    /**
     * after the page nodes are created, start to insert toble of content nodes and create links.
     */
    public void insertLinks(String XMLDir, int part)
    {
        
    	long startTime = System.currentTimeMillis();
    	LinkElementParserMemoryIndex LEP = new LinkElementParserMemoryIndex(DBDir,inMemoryIndex);
    	System.out.println("Creating links.");
    	LEP.parse(XMLDir,part);
    	System.out.println("Link counter " + LEP.linkCount);
    	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Totally "+elapsedSeconds+" seconds used to create links into neo4j.");
        LEP.shutdown();  
    }
    
    
	
	public String getXMLDir(int i)
	{
		return XMLPrefix+"-"+i+"-structure.xml";
	}
	
	/*
	 * write the in memory index to disk
	 */
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
		if (args.length < 4) {
		      System.out.println("USAGE: ExtractLinks <XMLPrefix-dir> <DBDir> <portion> <inMemoryIndex>");
		      System.exit(255);
		      }
//		String XMLPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki64Structure/wholeWiki";
//		String DBDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
//		String inMemoryIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/hashMap";
		
		
		String XMLPrefix = args[0];
		String DBDir = args[1];
		int portion = Integer.parseInt(args[2]);
		String inMemoryIndexDir = args[3];
		
		GraphCreator graphCreator = new GraphCreator(XMLPrefix,DBDir);
		
		for(int i = 1;i <= portion;i++)
		{
			String XMLDir = graphCreator.getXMLDir(i);
			graphCreator.insertNodes(XMLDir,i);
		}
		
		
		graphCreator.writeInMemoryIndex(inMemoryIndexDir);
		
		for(int i = 1;i <= portion;i++)
		{
			String XMLDir = graphCreator.getXMLDir(i);
			graphCreator.insertLinks(XMLDir,i);
		}
	}
}
