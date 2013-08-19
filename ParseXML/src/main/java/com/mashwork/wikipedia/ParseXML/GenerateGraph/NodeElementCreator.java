package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;
import com.mashwork.wikipedia.ParseXML.neo4jText.WikiTemplateParser;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ModularParser;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;

@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public class NodeElementCreator implements PageCallbackHandler
{
	int counter = 0;
	//protected int total = 13539091;			//this is for the whole wikidump
	int total = 782367;				//this is for GOT D3 825175  processed 762385
	int pageProcessed = 0;
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";
	final String TEXT_KEY = "fullText";
	WikiXMLParser wxsp;
	GraphDatabaseService graphDb;
    Index<Node> nodeIndex;
    Index<Node> TocIndex;
    Index<Node> fullTextIndex;
    MediaWikiParserFactory pf;
    List<String> imageIdentifiers;
	MediaWikiParser parser;
    ModularParser Mparser;
    Transaction tx;
    XMLStreamWriter writer;
    long startTime = System.currentTimeMillis();
    //private final Map<String, Long> inMemoryIndex;
	
	public NodeElementCreator(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx, XMLStreamWriter writer)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
        this.tx = tx;
        this.writer = writer;
		//this.inMemoryIndex = inMemoryIndex;
        
        pf = new MediaWikiParserFactory();
        List<String> imageIdentifiers = new ArrayList<String>();
        imageIdentifiers.add("file");
        pf.setImageIdentifers(imageIdentifiers);       
        pf.setShowImageText(true);
		parser = pf.createParser();
		Mparser = (ModularParser)parser;
		Mparser.setTemplateParser(new WikiTemplateParser());
	}
	
	public String processTitle(String input)
	{
		String[] temp = input.split("\n");
		return temp[0];
	}
    
    protected String toNormalLink(String link)
	{
		if(link==null || link.length()<=0) return null;
    	link =  link.substring(0,1).toUpperCase()+link.substring(1,link.length());
    	try{
    		link = URLDecoder.decode(link,"UTF-8");
    	}catch(Exception e)
    	{
    		//System.out.print("URLDecoder Error! ");
    		//System.out.println(link);
    	}
		return link.replace('_',' ');
	}
	
	protected boolean toBeFiltered(String link)
	{
		if(link=="") return true;
		if(link==null) return true;
		if(link.contains("File:") || link.contains("Special:") || 
				link.contains("User:") || (link.length()>0 && link.charAt(0)=='#'))
			return true;
		else
			return false;
	}
	
	public boolean isAnchorLink(String link)
	{
		if(link.contains("#"))
			return true;
		else
			return false;
	}
	
	//This is for Table of Content nodes.
		private Node createTocNode(String pageName)
		{
			String prePath = HierachyManager.getPrePath();
				Node node = graphDb.createNode();
				//long nodeId = node.getId();
				
		        node.setProperty( USERNAME_KEY, pageName );
		        String TocName = prePath + "#"+pageName;
		        //inMemoryIndex.put(TocName, node.getId());
		        
		        //System.out.println(TocName);
		        node.setProperty( TOC_KEY, TocName);
		        //if(fatherName.equals("Amsterdam")) System.out.println("The stored name: "+TocName);
		        TocIndex.add(node,TOC_KEY,TocName);
		        
		        fullTextIndex.add(node,TOC_KEY,TocName.replace('#',' '));
		        return node;
//			}
		}
		
		//This is for page nodes.
		private Node createAndIndexNode(String pageName)
	    {
			
	        Node node = graphDb.createNode();
	        node.setProperty( USERNAME_KEY, pageName );
	        nodeIndex.add( node, USERNAME_KEY, pageName );
	        //inMemoryIndex.put(pageName, node.getId());
	        fullTextIndex.add(node,USERNAME_KEY,pageName);
	        return node;
	    }
	
	public void StructureRecursion(List<Section> sectionList, String l) throws Exception
	{
		int k = 1;
		for(Section section:sectionList)
		{
			if(section.getTitle() != null)
			{
				int level = section.getLevel();
				String tableLevel = "c" + String.valueOf(level);
				writeSpace(level);
				writer.writeStartElement(tableLevel);
			    writer.writeCharacters(section.getTitle());
			    writer.writeEndElement();
			    writeLine();
				
				Node fatherNode = HierachyManager.findParentNode(tableLevel);
				Node currentPage = createTocNode(section.getTitle());
				fatherNode.createRelationshipTo(currentPage, RelTypes.TOC);
				Pair<String,Node> pair = new Pair<String,Node>(tableLevel,currentPage);
				HierachyManager.MyPop(pair);
				HierachyManager.MyPush(pair);
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
						if(link.getType().equals(Link.type.INTERNAL) || link.getType().equals(Link.type.UNKNOWN))
						{
							String linkName = link.getTarget().toString();
							
							if(toBeFiltered(linkName)) continue;
							
							if(isAnchorLink(linkName))
							{
								String[] pageNames = linkName.split("#");
								linkName = pageNames[0];
							}
							else
							{
								linkName = linkName.split("\n")[0];
							}
							//linkCount++;
							writeSpace(level+1);
							writer.writeStartElement("l");
					        writer.writeCharacters(linkName);
					       	writer.writeEndElement();
					       	writeLine();
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
	
	public void process(WikiPage page){
		try
		{
			String title = processTitle(page.getTitle());
			pageProcessed++;
			if ( pageProcessed > 0 && pageProcessed % 1000 == 0 ) {
				//System.out.println(pageProcessed);
	            tx.success();
	            tx.finish();
	            tx = graphDb.beginTx();
			}
			
			
			DecimalFormat df = new DecimalFormat("0.00");
			double percentage = ((double)counter/total*100);
			if(counter++%(total/100) == 0 )
			{
				System.out.print("Processing: "+ counter++
					+"  "+df.format(percentage)+"% " +"  "+ 
						title+ " Time used: "+(System.currentTimeMillis() - startTime) / 1000+"S.");
				int time = (int)((100-percentage)/percentage*(System.currentTimeMillis() - startTime) / 1000);
				int hour = time/3600;
				int minute = (time - hour*3600)/60;
				System.out.println("Time estimate: "+hour+"h"+minute+"m.");
			}
		
			Node node = createAndIndexNode(title);
			
			writer.writeStartElement("t");
	        writer.writeCharacters(title);
	        writer.writeEndElement();
	        writeLine();
			
			Pair<String,Node> pair = new Pair<String,Node>("t",node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);
			
			ParsedPage pp = Mparser.parse(page.getWikiText());
			
			StructureRecursion(pp.getSections(),"");
			
		} catch (Exception e)
		{
			System.out.println("XML write error!");
			// TODO: handle exception
		}
 }
	private void writeSpace(int num) throws XMLStreamException
	{
		while(num-->0)
		{
			writer.writeCharacters(" ");
		}
	}
	protected void writeLine() throws XMLStreamException
	{
		writer.writeCharacters("\n");
	}
}
