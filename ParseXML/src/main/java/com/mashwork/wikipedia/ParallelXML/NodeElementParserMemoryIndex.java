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

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;

public class NodeElementParserMemoryIndex
{
	public final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();	//***
	public GraphDatabaseService graphDb;
	public Index<Node> nodeIndex;
	public Index<Node> TocIndex;
	public Index<Node> fullTextIndex;
	public Transaction tx;
	public HashMap<String,Long> inMemoryIndex;
	public int part;
	
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";

	static String fatherName;
	final int pageCount = 782367/8;
	int links = 0;
	int nodeCount = 0;
	int counter = 0;
	long startTime;
	
	public NodeElementParserMemoryIndex(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx, HashMap<String,Long> inMemoryIndex,int part)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
		this.tx = tx;
		this.inMemoryIndex = inMemoryIndex;
		this.part = part;
	}
	
	public void parse(String fileName)
	{
		try
		{
			parse(new FileInputStream(fileName));
		}
		catch(Exception e)
		{
			System.out.println("IOException Error or XMLStreamException!");
		}
	}
	
	public void parse(InputStream inputStream) throws IOException, XMLStreamException {
        XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream, "UTF-8");
        try {
        	startTime = System.currentTimeMillis();
            parseElements(reader);
        } finally {
            reader.close();
            inputStream.close();
        }
    }
	
	private void parseElements(XMLStreamReader reader) throws XMLStreamException {
        LinkedList<String> elementStack = new LinkedList<String>();
        StringBuilder textBuffer = new StringBuilder();
        
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
    }
	
	public void handleElement(String element, String value)
	{
		if(element.equals("l") || element.equals("d"))
		{
			links++;
			return;
		}
		if(element.equals("t"))
		{
			printStatus();
			Node node = createAndIndexNode(value);
			Pair<String,Node> pair = new Pair<String,Node>(element,node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);
			fatherName = node.getProperty(USERNAME_KEY).toString();
			
		}
		else
		{
			Node fatherNode = HierachyManager.findParentNode(element);
			Node node = createTocNode(value);
			Pair<String,Node> pair = new Pair<String,Node>(element,node);			
			fatherNode.createRelationshipTo(node, RelTypes.TOC);
			HierachyManager.MyPush(pair);
		}
	}
	
	public void needToFlush()
	{
		if ( counter > 0 && counter % 1000 == 0 ) {
            tx.success();
            tx.finish();
            tx = graphDb.beginTx();
        }
	}
	
	public void printStatus()
	{
		DecimalFormat df = new DecimalFormat("0.00");
		double percentage = ((double)counter++/pageCount*100);
		needToFlush();
		if(counter%(pageCount/100) == 0 )
		{	
			System.out.print("Processing: part "+part +" "+ counter
					+"  "+df.format(percentage)+"%  Time used: "+(System.currentTimeMillis() - startTime) / 1000+"S.");
				int time = (int)((100-percentage)/percentage*(System.currentTimeMillis() - startTime) / 1000);
				int hour = time/3600;
				int minute = (time - hour*3600)/60;
				System.out.println("Time estimate: "+hour+"h"+minute+"m.");
		}
	}
	
	//This is for Table of Content nodes.
	private Node createTocNode(String pageName)
	{	
		String prePath = HierachyManager.getPrePath();
		Node exist = TocIndex.get(TOC_KEY,prePath+"#"+pageName).getSingle();
		if(exist!=null)
		{
			return exist;
		}
		else
		{
			Node node = graphDb.createNode();
	        node.setProperty( USERNAME_KEY, pageName );
	        String TocName = prePath + "#"+pageName;
	        //System.out.println(TocName);
	        node.setProperty( TOC_KEY, TocName);
	        //if(fatherName.equals("Amsterdam")) System.out.println("The stored name: "+TocName);
	        TocIndex.add(node,TOC_KEY,TocName);
	        
	        fullTextIndex.add(node,TOC_KEY,TocName.replace('#',' '));
	        return node;
		}
	}
	
	//This is for page nodes.
	private Node createAndIndexNode(String pageName)
    {
        Node node = graphDb.createNode();
        node.setProperty( USERNAME_KEY, pageName );
        nodeIndex.add( node, USERNAME_KEY, pageName );
        Long nodeId = node.getId();
        inMemoryIndex.put(pageName, nodeId);
        fullTextIndex.add(node,USERNAME_KEY,pageName);
        return node;
    }

}
