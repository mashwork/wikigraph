package com.mashwork.wikipedia.ParseXML;
import java.util.*;
import java.text.DecimalFormat; 
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;


//import wikipediaParser.*;
import edu.jhu.nlp.wikipedia.*;
public class MyCallBackHandler implements PageCallbackHandler {
	XMLStreamWriter writer;
	static int counter = 0;
	static int total = 13254844;
	private HashMap<String,Integer> list;
    //private HashMap<String,Integer> list2;
    int iteration;
    boolean printLinks2Screen = false;
    boolean printStructure2Screen = false;
    boolean printProgress2Screen = true;
    
    static int pageCount = 0;
    static int linkCount = 0;
    
	public MyCallBackHandler(XMLStreamWriter writer, HashMap<String,Integer> list)
	{
		this.writer = writer;
		this.list = list;
		//this.list2 = new HashMap<String,Integer>();
        //list2.put("Total_Page_to_Process", 0);
	}
	
	private String processTitle(String input)
	{
		String[] temp = input.split("\n");
		return temp[0];
	}
	
	public void setPrintLink(boolean print)
	{
		this.printLinks2Screen = print;
	}
	
	public boolean needPrintLinks()
	{
		return this.printLinks2Screen;
	}
	
	public void setPrintStructure(boolean print)
	{
		this.printStructure2Screen = print;
	}
	
	public boolean needPrintStructure()
	{
		return this.printStructure2Screen;
	}
	
	public void setPrintProgress(boolean print)
	{
		this.printProgress2Screen = print;
	}
	
	public boolean needPrintProgress()
	{
		return this.printProgress2Screen;
	}
	
	public void setIteNum(int num)
    {
    	this.iteration = num;
    }
    
    public int getIteNum()
    {
    	return this.iteration;
    }
	
  //check whether the query page is in the list, if yes, mark it as 1(visited) and return true;
    //else return false;
    private boolean checkList(String query)				
    {
    	//Below is the "Contains String" version
//    	Set<String> keys = list.keySet();
//    	Iterator<String> it = keys.iterator();
//    	while(it.hasNext())
//    	{	
//    		String key = it.next();
//    		if(query.contains(key))
//    		{
//    			return true;
//    		}
//    	}
//    	return false;
    	
    	//Below is the "equals String" version
    	if(list.containsKey(query) && list.get(query) == 0)//
    	{
    		list.put(query, 1);
    		//int num = list.get("Total_Page_to_Process");
    		//list.put("Total_Page_to_Process",num-1);
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    
  //code to add recently found link/pages
    private void tryAddPage(String link)
    {
    	if(list2.containsKey(link)) return;
    	else
    	{
    		//int num = list2.get("Total_Page_to_Process");
    		//list2.put("Total_Page_to_Process",num+1);
    		list2.put(link,0);
    	}
    }
    
    public void switchList()
    {
    	this.list = this.list2;
    	this.list2 = new HashMap<String,Integer>();
    }
    
	private void StructureRecursion(List<Section> sectionList, String l) throws XMLStreamException
	{
		int k = 1;
		for(Section section:sectionList)
		{
			if(section.getTitle() != null)
			{
				int level = section.getLevel();
				if(needPrintStructure())
				{
					for(int i = 0; i < level; i++)
					{
						System.out.print("  ");
					}
					if(level==2)
					{
						System.out.print(k++ +" ");
					}
					else
					{
						System.out.print(l+k++ +" ");
					}
					System.out.println(section.getTitle());
				}
				String tableLevel = "c" + String.valueOf(level);
				writer.writeStartElement(tableLevel);
			    writer.writeCharacters(section.getTitle());
			    writer.writeEndElement();
			}
			if(section instanceof SectionContent)
			{
				List<Content> content = section.getContentList();
				for(Content c: content)
				{
					List<Link> links = c.getLinks();
					for(Link link:links)
					{
						int level = section.getLevel();
						if(needPrintLinks())
						{
							while(level-->0)
							{
								System.out.print("  ");
							}
							System.out.println("Type: "+link.getType()+"  Link: "+link.getTarget());
						}
						if(link.getType().equals(Link.type.INTERNAL) || link.getType().equals(Link.type.UNKNOWN))
						{
							String link2 = link.getTarget().toString();
							
							if(toBeFiltered(link2)) continue;
							
							if(isAnchorLink(link2))
							{
								String[] pageNames = link2.split("#");
								tryAddPage(pageNames[0]);
							}
							else
							{
								tryAddPage(link.getTarget().toString());
							}
							linkCount++;
							writer.writeStartElement("l");
					        writer.writeCharacters(link.getTarget().toString());
					       	writer.writeEndElement();
						}
					}
				}
				
			}
			if(section instanceof SectionContainer)
			{
				SectionContainer sc = (SectionContainer)section;
				List<Section> content = sc.getSubSections();
				StructureRecursion(content,l+String.valueOf(k-1)+".");
			}
		}
		
		return;
	}
	
	private boolean toBeFiltered(String link)
	{
		if(link=="") return true;
		if(link.contains("File:") || link.contains("Special:") || 
				link.contains("User:") || link.charAt(0)=='#')
			return true;
		else
			return false;
	}
	
	private boolean isAnchorLink(String link)
	{
		if(link.contains("#"))
			return true;
		else
			return false;
	}
	
	public void process(WikiPage page){
		try{
			
			String title = processTitle(page.getTitle());
			
			if(needPrintProgress())
			{
				DecimalFormat df = new DecimalFormat("0.00");
				double percentage = ((double)counter/total*100);
				if(counter++%(total/100) == 0 )
				{
					System.out.println("Processing: "+ counter++
						+"  "+df.format(percentage)+"% " +"  "+ title);
				}
			}
			//if(!page.getTitle().contains("Anarchism")) return;
			if(!checkList(title)) return;
			//System.out.print("Title: " + title);
			pageCount++;
	        writer.writeStartElement("t");
	        writer.writeCharacters(title);
	        writer.writeEndElement();
	
	        //System.out.println(page.getWikiText());
	        MediaWikiParserFactory pf = new MediaWikiParserFactory();
			MediaWikiParser parser = pf.createParser();
			
			ParsedPage pp = parser.parse(page.getWikiText());
			
			StructureRecursion(pp.getSections(),"");
	        
		}
		catch(XMLStreamException e)
		{
			System.out.println("XML write error!");
		}
 }
}
