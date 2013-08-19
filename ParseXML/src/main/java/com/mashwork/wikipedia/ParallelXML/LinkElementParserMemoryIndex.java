package com.mashwork.wikipedia.ParallelXML;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time
 * this class is used to create toc nodes and insert links	
 */
public class LinkElementParserMemoryIndex
{
	public final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();	//***
	BatchInserter inserter;
	public HashMap<String,Long> inMemoryIndex;
	public int part;
	
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";

	static String fatherName;
	final int pageCount = 13539091/64;	//782367
	int linkCount = 0;
	int counter = 0;
	long startTime;
	
	HashSet<String> avoidDupe;
	
	public LinkElementParserMemoryIndex(String DBDir, HashMap<String,Long> inMemoryIndex)
	{
    	this.inserter = BatchInserters.inserter(DBDir);
		this.inMemoryIndex = inMemoryIndex;
    	registerShutdownHook();
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
	
	//use a stream reader to recognize start and end of xml
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
		if(element.equals("d"))			//the whole xml is in a big <d> </d>
		{
			return;
		}
		
		if(element.equals("t"))			//means title
		{
			printStatus();
			Long nodeId = retrieveFatherNodeId(value);
			Pair<String,Long> IdPair = new Pair<String,Long>(element,nodeId);
			HierachyManager.addIdPath(IdPair);
			
			Pair<String,String> namePair = new Pair<String,String>(element,value);
			HierachyManager.tractNamePath(namePair);

			clearDupe();
		}
		
		else if(!element.equals("l"))		//means links
		{
			if(Filter.need2Skip(element)) return;
			
			Pair<String,String> namePair = new Pair<String,String>(element,value);	//for example, it is a pair of <t,Game of Thrones>
			HierachyManager.tractNamePath(namePair);		//used to keep tracking the position of a tree structure

			Map<String, Object> properties = MapUtil.map(USERNAME_KEY, HierachyManager.getNamePath());//TOC_KEY
			Long sonNodeId = inserter.createNode(properties);
			Pair<String,Long> IdPair = new Pair<String,Long>(element,sonNodeId);
			
			HierachyManager.updateIdPath(IdPair);
			Long fatherNodeId = HierachyManager.getTopId();
			
			HierachyManager.addIdPath(IdPair);
			
			createTocLinks(fatherNodeId, sonNodeId);
			clearDupe();				
			//fatherNodeId = sonNodeId;
		}
		else			//This is the case for links. Need to analyze several cases.
		{
			if(linkAlreadyCreated(value)) return;
			Long fatherNodeId = HierachyManager.getTopId();
			createLinks(fatherNodeId, value);	
		}
	}
	
	public void clearDupe()
	{
		avoidDupe = new HashSet<String>();
	}
	
	public boolean linkAlreadyCreated(String linkName)
	{
		if(avoidDupe.add(linkName))
		{
			return false;
		}
		else
		{
			return true;
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
	
	private void createLinks(Long fatherNodeId, String link)
	{
		if(fatherNodeId == null) 
		{
			System.out.println("fahter id is null");
			return;
		}
		link = Filter.toNormalLink(link);	
		if(Filter.toBeFiltered(link)) return;
		Long sonNodeId = retrieveSonNode(link);
		if(sonNodeId==null)
		{
			//System.out.println("son id is null");
			return;
		}

		linkCount++;
			if(Filter.isCategoryLink(link))
		{
			inserter.createRelationship(fatherNodeId, sonNodeId, RelTypes.CATEGORY, MapUtil.map());
		}
		else
		{
			inserter.createRelationship(fatherNodeId, sonNodeId, RelTypes.INTERNAL, MapUtil.map());
		}
	}
	
	private void createTocLinks(Long fatherNodeId,Long sonNodeId)
	{
		if(fatherNodeId == null || sonNodeId == null)
		{
			System.out.println("id is null");
			return;
		}
		else
		{
			inserter.createRelationship(fatherNodeId, sonNodeId, RelTypes.TOC, MapUtil.map());
		}
	}
	
	private Long retrieveFatherNodeId(String nodeName)
	{
		long Id = inMemoryIndex.get(nodeName);
		return Id;
	}
	
	
	private Long retrieveSonNode(String nodeName)
	{
		return inMemoryIndex.get(nodeName);
	}
	public void shutdown()
    {
    	//indexProvider.shutdown();
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
