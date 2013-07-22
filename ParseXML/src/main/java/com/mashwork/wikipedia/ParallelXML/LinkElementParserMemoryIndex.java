package com.mashwork.wikipedia.ParallelXML;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;

public class LinkElementParserMemoryIndex
{
	public final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();	//***
	public GraphDatabaseService graphDb;
	public Index<Node> nodeIndex;
	public Index<Node> TocIndex;
	public Index<Node> fullTextIndex;
	public Transaction tx;
	public HashMap<String,Long> inMemoryIndex;
	public BatchInserter inserter;
	public Node fatherNode;
	public int part;
	
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";

	static String fatherName;
	final int pageCount = 13539091/8;	//782367
	int linkCount = 0;
	int counter = 0;
	long startTime;
	
	public LinkElementParserMemoryIndex(GraphDatabaseService graphDb, Index<Node> nodeIndex,Index<Node> TocIndex,
			Index<Node> fullTextIndex, Transaction tx, HashMap<String,Long> inMemoryIndex,BatchInserter inserter,int part)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
		this.tx = tx;
		this.inMemoryIndex = inMemoryIndex;
		this.inserter = inserter;
		this.part = part;
	}
	
	public void parse(String fileName)
	{
		System.out.println(fileName);
		try
		{
			startTime = System.currentTimeMillis();
			parse(new FileInputStream(fileName));
		}
		catch(Exception e)
		{
			System.out.println("IOException Error or XMLStreamException!");
			e.printStackTrace();
		}
	}
	
	
	public void parse(InputStream inputStream) {
		XMLStreamReader reader = null;
		try
		{
        	reader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream, "UTF-8");
		}catch(XMLStreamException e)
		{
			System.out.println("Open input stream error!");
			e.printStackTrace();
		}
        try {
            parseElements(reader);
        }finally {
        	try
            {
        		reader.close();
        		inputStream.close();
            }catch(IOException e)
            {
            	System.out.println("Error! Can not close the stream !");
            	e.printStackTrace();
            }catch(XMLStreamException e)
            {
            	System.out.println("Error! Can not close the reader!");
            	e.printStackTrace();
            }
        }
    }
	
	private void parseElements(XMLStreamReader reader)
	{
        LinkedList<String> elementStack = new LinkedList<String>();
        StringBuilder textBuffer = new StringBuilder();
        
        try
        {
	        while (reader.hasNext()) {
	            switch (reader.next()) {
	            case XMLEvent.START_ELEMENT:
	                elementStack.push(reader.getName().getLocalPart());
	                textBuffer.setLength(0);
	                break;
	            case XMLEvent.END_ELEMENT:
	                String element = elementStack.pop();
	                handleElement(element, textBuffer.toString().trim());
	                break;
	            case XMLEvent.CHARACTERS:
	                textBuffer.append(reader.getText());
	                break;
	            }
	        }
        }catch(Exception e)
        {
        	System.out.println("xmlstream reader error!");
        	System.out.println("Text: "+textBuffer.toString().trim());
        	e.printStackTrace();
        }
    }
	
	public void handleElement(String element, String value)
	{
		if(element.equals("d"))
		{
			return;
		}
		
		if(element.equals("t"))
		{
			printStatus();
		}
		
		if(!element.equals("l"))
		{
			Pair<String,String> pair = new Pair<String,String>(element,value);
			HierachyManager.tractPath(pair);
			fatherNode = retrieveFatherNode(HierachyManager.getPath());
		}
		else			//This is the case for links. Need to analyze several cases.
		{
			createLinks(fatherNode, value);	
		}
	}
	
	public void needToFlush()
	{
		linkCount++;
		if ( linkCount > 0 && linkCount % 100000 == 0 ) {
            tx.success();
            tx.finish();
            tx = graphDb.beginTx();
        }
	}
	
	public void printStatus()
	{
		DecimalFormat df = new DecimalFormat("0.00");
		double percentage = ((double)counter/pageCount*100);
		if(counter++%(pageCount/100) == 0 )
		{
			System.out.print("Processing: part "+part+" "+counter
					+"  "+df.format(percentage)+"%  Time used: "+(System.currentTimeMillis() - startTime) / 1000+"S.");
				int time = (int)((100-percentage)/percentage*(System.currentTimeMillis() - startTime) / 1000);
				int hour = time/3600;
				int minute = (time - hour*3600)/60;
				System.out.println("Time estimate: "+hour+"h"+minute+"m.");
		}
	}
	
	private void createLinks(Node fatherNode, String link)
	{
		if(fatherNode == null) return;
		link = Filter.toNormalLink(link);	
		if(Filter.toBeFiltered(link)) return;
		Long sonNodeId = retrieveSonNode(link);
		if(sonNodeId==null) return;
		
		needToFlush();
		if(Filter.isAnchorLink(link))
		{	
			inserter.createRelationship(fatherNode.getId(), sonNodeId, RelTypes.ANCHOR, MapUtil.map());
		}
		else if(Filter.isCategoryLink(link))
		{
			inserter.createRelationship(fatherNode.getId(), sonNodeId, RelTypes.CATEGORY, MapUtil.map());
		}
		else
		{
			inserter.createRelationship(fatherNode.getId(), sonNodeId, RelTypes.INTERNAL, MapUtil.map());
		}
	}
	
	private Node retrieveFatherNode(String nodeName)
	{
		nodeName = nodeName.split("#")[0];
		Node result = nodeIndex.get(USERNAME_KEY,nodeName).getSingle();
		return result;
	}
	private Long retrieveSonNode(String nodeName)
	{
		return inMemoryIndex.get(nodeName);
	}
}
