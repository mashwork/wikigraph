package com.mashwork.wikipedia.ParallelXML;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;


/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * this class is used for putting page nodes into neo4j(table of content node will be added by LinkElementParsermemoryIndex)
 * Nodes will be created in batch mode which will be more efficient.
 */
public class NodeElementParserMemoryIndex
{
	public final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();	//***
	BatchInserter inserter;
	BatchInserterIndexProvider indexProvider;
	BatchInserterIndex index;
	public HashMap<String,Long> inMemoryIndex;
	public int part;
	
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";

	static String fatherName;
	final int pageCount = 13539091/64;		//782367
	int links = 0;
	int nodeCount = 0;
	int counter = 0;
	long startTime;
	
	public NodeElementParserMemoryIndex(String DBDir, HashMap<String,Long> inMemoryIndex)
	{
    	this.inserter = BatchInserters.inserter(DBDir);
        this.indexProvider = new LuceneBatchInserterIndexProvider(inserter);
        this.index = indexProvider.nodeIndex("nodes", MapUtil.stringMap("type", "exact", "to_lower_case", "false"));
		this.inMemoryIndex = inMemoryIndex;
    	registerShutdownHook();
		//this.part = part;
	}
	
	
	public void parse(String fileName,int part)
	{
		this.part = part;
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
	
	/*
	 * recognize the start and end of a xml element
	 */
	private void parseElements(XMLStreamReader reader){
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
        	System.out.println("Text: "+textBuffer.toString());
        	e.printStackTrace();
        }
    }
	
	/*
	 * every time an element is recognized, this function will be called to create node in neo4j.
	 */
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
			createAndIndexNode(value);
		}
	}
	
	
	
	public void needToFlush()
	{
		if ( counter > 0 && counter % 5000 == 0 ) {
            index.flush();
            System.out.println("Flushed");
        }
	}
	
	public void printStatus()
	{
		DecimalFormat df = new DecimalFormat("0.00");
		double percentage = ((double)counter++/pageCount*100);
		//needToFlush();
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
	
	
	//This is for page nodes.
	private void createAndIndexNode(String pageName)
    {
		Map<String, Object> properties = MapUtil.map(USERNAME_KEY, pageName);
        long nodeId = inserter.createNode(properties);
        index.add(nodeId, properties);
        //long startTime = System.currentTimeMillis();
        inMemoryIndex.put(pageName,nodeId);
        nodeCount++;
        if(nodeCount%50000==0)
        {
        	index.flush();
        	System.out.println("Flushed.");
        }
        //long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        //if(elapsedSeconds >1) System.out.println(elapsedSeconds);
    }
	
	public void shutdown()
    {
		index.flush();
    	indexProvider.shutdown();
        inserter.shutdown(); 
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
