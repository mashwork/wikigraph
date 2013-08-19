package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.List;

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

@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public class LinkElementCreator implements PageCallbackHandler
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
    private final BatchInserter inserter;
    //private final Map<String, Long> inMemoryIndex;
    long startTime = System.currentTimeMillis();
    
    public LinkElementCreator(GraphDatabaseService graphDb, Index<Node> nodeIndex,Index<Node> TocIndex,
    		Index<Node> fullTextIndex, Transaction tx, BatchInserter inserter)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
        this.tx = tx;
        this.inserter = inserter;
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
//    	if(nodeIndex.get(USERNAME_KEY,nodeName).size()>1)
//    	{
//    		IndexHits<Node> hits = nodeIndex.get(USERNAME_KEY,nodeName);
//    		while(hits.hasNext())
//    		System.out.println(hits.next().getProperty(USERNAME_KEY).toString());
//    		return null;
//    	}
		Node result = nodeIndex.get(USERNAME_KEY,nodeName).getSingle();
		return result;
	}
	
	private Node retrieveTocNode(String nodeName)
	{
		String TocName = HierachyManager.getPrePath() +"#"+nodeName;
		//System.out.println("In retrieveTocNode: "+TocName);
		Node result = TocIndex.get(TOC_KEY,TocName).getSingle();
		return result;
	}
	
	private void createRelationship(long nodeId, String link, RelTypes type) {
        Long linkNodeId = findNodeId(link);
        if (linkNodeId != null && linkNodeId!=-1) {
        	//System.out.println("The node Id is " + nodeId);
        	//System.out.println("The linked node Id is " + linkNodeId);
            inserter.createRelationship(nodeId, linkNodeId, type, MapUtil.map());	//define wikirelation first
            linkCount++;
        } else {
        	//System.out.println(nodeId + " "+ link + " failed");
            badLinkCount++;
        }
    }
	
	private Long findNodeId(String nodeName) 		//try inMemory index, make it fast
	{
		Node result = nodeIndex.get(USERNAME_KEY,nodeName).getSingle();
		if(result==null)			//in this case, it is an anchor link
		{
			IndexHits<Node> hits = TocIndex.get(TOC_KEY,nodeName);
			if(hits.size()>=1)
			{
				result = hits.next();
			}
		}	
		if(result==null)
		{
			return (long) -1;
		}
		else
		{
			return result.getId();
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
				HierachyManager.tractNamePath(pair);
			}
			
			if(section instanceof SectionContent)
			{
				List<Content> content = section.getContentList();
				for(Content c: content)
				{
					List<Link> links = c.getLinks();
					for(Link link:links)
					{
						//int level = section.getLevel();
						if(link.getType().equals(Link.type.INTERNAL) || link.getType().equals(Link.type.UNKNOWN))
						{
							String linkName = link.getTarget().toString();
							
							if(toBeFiltered(linkName)) continue;
							linkName = toNormalLink(linkName);
							if(isAnchorLink(linkName))
							{
								//createRelationship(HierachyManager.MyPeek().getSecond().getId(),linkName,RelTypes.ANCHOR);
								long parentId = findNodeId(HierachyManager.getNamePath());
								if(parentId!=-1)
								createRelationship(parentId,linkName,RelTypes.ANCHOR);
							}
							else if(isCategoryLink(linkName))
							{
								long parentId = findNodeId(HierachyManager.getNamePath());
								if(parentId!=-1)
								createRelationship(parentId,linkName,RelTypes.CATEGORY);
							}
							else
							{
								//createRelationship(HierachyManager.MyPeek().getSecond().getId(),linkName,RelTypes.INTERNAL);
								long parentId = findNodeId(HierachyManager.getNamePath());
								if(parentId!=-1)
								createRelationship(parentId,linkName,RelTypes.INTERNAL);
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
			if ( pageProcessed > 0 && pageProcessed % 1000 == 0 ) {
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
		
			Pair<String,String> pair = new Pair<String,String>("t",title);
			HierachyManager.tractNamePath(pair);
			
			ParsedPage pp = parser.parse(page.getWikiText());
			StructureRecursion(pp.getSections(),"");
 }
}
