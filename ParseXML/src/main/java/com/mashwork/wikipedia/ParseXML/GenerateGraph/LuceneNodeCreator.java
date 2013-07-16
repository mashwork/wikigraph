package com.mashwork.wikipedia.ParseXML.GenerateGraph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;
import com.mashwork.wikipedia.ParseXML.neo4jText.WikiTemplateParser;

import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
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


public class LuceneNodeCreator implements PageCallbackHandler
{
	int counter = 0;
	//protected int total = 13539091;			//this is for the whole wikidump
	int total = 762385;				//this is for GOT D3 825175  processed 762385
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
    long startTime = System.currentTimeMillis();
	
	public LuceneNodeCreator(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
        this.tx = tx;
        
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
	
	private void addText2Node(String text)
	{
		Node node = HierachyManager.MyPeek().getSecond();
		if(node == null) System.out.println("Node is null");
		node.setProperty(TEXT_KEY,text);
		fullTextIndex.add(node,TEXT_KEY,text);
//		if(HierachyManager.isPageNode(node))
//		{
//			System.out.println("Adding text to page node:"+node.getProperty(USERNAME_KEY).toString());
//		}
//		else
//		{
//			System.out.println("Adding text to toc node:"+node.getProperty(TOC_KEY).toString());
//		}
	}
	
	//This is for Table of Content nodes.
		private Node createTocNode(String pageName)
		{
			String prePath = HierachyManager.getPrePath();
//			Node exist = TocIndex.get(TOC_KEY,prePath+"#"+pageName).getSingle();
//			if(exist!=null)
//			{
//				return exist;
//			}
//			else
//			{
				Node node = graphDb.createNode();
				//long nodeId = node.getId();
				
		        node.setProperty( USERNAME_KEY, pageName );
		        String TocName = prePath + "#"+pageName;
		        
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
	        fullTextIndex.add(node,USERNAME_KEY,pageName);
	        return node;
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
				
				Node fatherNode = HierachyManager.findParentNode(tableLevel);
				Node currentPage = createTocNode(section.getTitle());
				fatherNode.createRelationshipTo(currentPage, RelTypes.TOC);
				Pair<String,Node> pair = new Pair<String,Node>(tableLevel,currentPage);
				HierachyManager.MyPop(pair);
				HierachyManager.MyPush(pair);
			}
			
			if(section instanceof SectionContent)
			{
				StringBuilder sb = new StringBuilder();
				for(Paragraph para:section.getParagraphs())
            	{
            		//System.out.println(para.getText());
					sb.append(para.getText());
            	}
				String text = sb.toString();
				if(text!=null && !text.equals(""))
				{
					addText2Node(text);
				}
				
			}
			
			if(section instanceof SectionContainer)
			{
				SectionContainer sc = (SectionContainer)section;
				
				StringBuilder sb = new StringBuilder();
				for(Paragraph para:section.getParagraphs())
            	{
					//System.out.println(para.getText());
					sb.append(para.getText());
            	}
				String text = sb.toString();
				if(text!=null && !text.equals(""))
				{
					addText2Node(text);
				}
				
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
			Pair<String,Node> pair = new Pair<String,Node>("t",node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);
			
			ParsedPage pp = Mparser.parse(page.getWikiText());
			
			StructureRecursion(pp.getSections(),"");

 }
}
