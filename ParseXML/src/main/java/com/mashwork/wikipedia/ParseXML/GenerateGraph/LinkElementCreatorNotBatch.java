package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;
import com.mashwork.wikipedia.ParseXML.neo4j.TOCLocator;
import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;


import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;

public class LinkElementCreatorNotBatch implements PageCallbackHandler
{
	int counter = 0;
	//protected int total = 13539091;			//this is for the whole wikidump
	int total = 782367;				//this is for GOT D3
	int linkCount = 0;
	int badLinkCount = 0;
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
	MediaWikiParser parser;
    Transaction tx;
    int retrieveTime = 0;
    int retrieveCount = 0;
    //private final Map<String, Long> inMemoryIndex;
    long startTime = System.currentTimeMillis();
    
    public LinkElementCreatorNotBatch(GraphDatabaseService graphDb, Index<Node> nodeIndex,Index<Node> TocIndex,
    		Index<Node> fullTextIndex, Transaction tx)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
        this.tx = tx;
		//this.inMemoryIndex = inMemoryIndex;
        pf = new MediaWikiParserFactory();   
		parser = pf.createParser();
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
	
	private boolean isCategoryLink(String value)
	{
		if(value.contains("Category:"))
			return true;
		else
			return false;
	}
    
    private Node retrievePageNode(String nodeName)
	{
    	//nodeName = "Vowel";
		//long startTime = System.currentTimeMillis();
		Node result = nodeIndex.get(USERNAME_KEY,nodeName).getSingle();
		//long endTime = System.currentTimeMillis();
		//retrieveTime += (endTime-startTime);
		//retrieveCount++;
		//System.out.println("Avg Time used: "+(double)retrieveTime/retrieveCount);
		//if((endTime-startTime)>1) System.out.println((endTime-startTime) + "pageNane: "+nodeName);
		return result;
	}
	
    //make sure the nodeName is like abc#ddd	
	private Node retrieveTocNode(String value)
	{
		//Node result = TocIndex.get(TOC_KEY,value).getSingle();
		//long startTime = System.currentTimeMillis();
		String pageName[] = value.split("#");
		Node parent = retrievePageNode(pageName[0]);
		int position = 1;
		while(position<pageName.length)
		{
			if(parent==null) return null;
			parent = TOCLocator.findToc2(parent,pageName[position++]);
		}
		//long endTime = System.currentTimeMillis();
		//retrieveTime += (endTime-startTime);
		//retrieveCount++;
		//System.out.println("Avg Time used: "+(double)retrieveTime/retrieveCount);
		//if((endTime-startTime)>1) System.out.println((endTime-startTime) + "pageNane: "+nodeName);
		return parent;
	}
	
	private Node retrieveNode(String nodeName)
	{
		if(nodeName.contains("#"))
		{
			//return null;
			return retrieveTocNode(nodeName);
		}
		else
		{
			//return null;
			return retrievePageNode(nodeName);
		}
	}
    
	private void createLink(Node parentNode,Node childNode,RelTypes type)
	{
		if(parentNode!=null && childNode!=null)
		{
			parentNode.createRelationshipTo(childNode,type);
			linkCount++;
		}
		else
		{
			badLinkCount++;
		}
	}
	
	public void StructureRecursion(List<Section> sectionList, String l)
	{
		int k = 1;
		for(Section section:sectionList)
		{
			if(section.getTitle() != null)
			{
				int level = section.getLevel();
				String tableLevel = "c" + String.valueOf(level);
				Pair<String,String> pair = new Pair<String,String>(tableLevel,section.getTitle());
				HierachyManager.tractPath(pair);
			}
			
			if(section instanceof SectionContent)
			{
				List<Content> content = section.getContentList();
				for(Content c: content)
				{
					List<Link> links = c.getLinks();
					for(Link link:links)
					{
						if(link.getType().equals(Link.type.INTERNAL) || link.getType().equals(Link.type.UNKNOWN))
						{
							String linkName = link.getTarget().toString();
							
							if(toBeFiltered(linkName)) continue;
							linkName = toNormalLink(linkName);
							
							Node parentNode = retrieveNode(HierachyManager.getPath());
							Node childNode = retrieveNode(linkName);

							if(isAnchorLink(linkName))
							{	
								createLink(parentNode,childNode,RelTypes.ANCHOR);
							}
							else if(isCategoryLink(linkName))
							{
								createLink(parentNode,childNode,RelTypes.CATEGORY);
							}
							else
							{
								createLink(parentNode,childNode,RelTypes.INTERNAL);
							}
							linkCount++;
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
			String title = processTitle(page.getTitle());
			pageProcessed++;
			if ( pageProcessed > 0 && pageProcessed % 100 == 0 ) {
				System.out.println(pageProcessed);
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
		
//			Node node = retrievePageNode(title);
//			Pair<String,Node> pair = new Pair<String,Node>("t",node);
//			HierachyManager.MyPop(pair);
//			HierachyManager.MyPush(pair);
			Pair<String,String> pair = new Pair<String,String>("t",title);
			HierachyManager.tractPath(pair);
			
			ParsedPage pp = parser.parse(page.getWikiText());
			StructureRecursion(pp.getSections(),"");

 }
}
