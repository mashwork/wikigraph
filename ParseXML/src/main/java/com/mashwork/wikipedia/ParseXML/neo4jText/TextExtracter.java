package com.mashwork.wikipedia.ParseXML.neo4jText;

import com.mashwork.wikipedia.ParseXML.MyCallBackHandler;
import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.FlushTemplates;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ModularParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ShowTemplateNamesAndParameters;
import edu.jhu.nlp.wikipedia.*;

public class TextExtracter extends MyCallBackHandler
{
	final static int pageCount = 25579;
	static int counter = 0;
	final static int total = 3038323;
	static int pageProcessed = 0;
	
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";
	final String TEXT_KEY = "fullText";
	
	MediaWikiParserFactory pf;
    List<String> imageIdentifiers;
	MediaWikiParser parser;
	ModularParser Mparser;	
	Transaction tx;
	
	protected   GraphDatabaseService graphDb;
    protected  Index<Node> nodeIndex;
    protected  Index<Node> TocIndex;
    protected  Index<Node> fullTextIndex;
	
	public TextExtracter(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx)
	{
		super(null,null,0);
		
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
	
	public void StructureRecursion(List<Section> sectionList, String l) throws XMLStreamException
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
				
				Node currentPage = retrieveTocNode(section.getTitle());
				if(currentPage==null) break;
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
		try{
			
			String title = processTitle(page.getTitle());
			//title = page.getTitle();
			if(needPrintProgress())
			{
				DecimalFormat df = new DecimalFormat("0.00");
				double percentage = ((double)counter++/total*100);
				if(counter%(total/100) == 0 )
				{
					System.out.println("Processing: "+ counter
						+"  "+df.format(percentage)+"% " +"  "+ title);
				}
			}

			//if(!page.getTitle().contains("Anarchism")) return;
//			if(!checkList(title)) return;
//			System.out.println("Title: " + title);
//			pageCount++;
	
			Node currentPage = retrievePageNode(title);
			if(currentPage!=null)
			{
				pageProcessed++;
				if ( pageProcessed > 0 && pageProcessed % 100 == 0 ) {
		            tx.success();
		            tx.finish();
		            tx = graphDb.beginTx();
		        }
				
				Pair<String,Node> pair = new Pair<String,Node>("t",currentPage);
				HierachyManager.MyPop(pair);
				HierachyManager.MyPush(pair);
				
				ParsedPage pp = Mparser.parse(page.getWikiText());
				StructureRecursion(pp.getSections(),"");
			}

	        
		}
		catch(XMLStreamException e)
		{
			System.out.println("XML write error!");
		}
 }
	
	private Node retrievePageNode(String nodeName)
	{
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
	
}
