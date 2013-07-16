package com.mashwork.wikipedia.ParallelXML;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

public class ParallelHandler implements PageCallbackHandler
{
	protected XMLStreamWriter writer;
	protected int counter = 0;
	protected int portionSize;
	int Id;
	long startTime = System.currentTimeMillis();
	MediaWikiParserFactory pf;
	MediaWikiParser parser;
    ModularParser Mparser;
    
	public ParallelHandler(XMLStreamWriter writer,int portionSize, int Id)
	{
		this.writer = writer;
		this.portionSize = portionSize;
		System.out.println(this.portionSize);
		this.Id = Id;
		
		pf = new MediaWikiParserFactory();
        List<String> imageIdentifiers = new ArrayList<String>();
        imageIdentifiers.add("file");
        pf.setImageIdentifers(imageIdentifiers);       
        pf.setShowImageText(true);
		parser = pf.createParser();
		Mparser = (ModularParser)parser;
		Mparser.setTemplateParser(new WikiTemplateParser());
	}
	
	public void writeSpace(int num) throws XMLStreamException
	{
		while(num-->0)
		{
			writer.writeCharacters(" ");
		}
	}
	public void writeLine() throws XMLStreamException
	{
		writer.writeCharacters("\n");
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
							
							if(Filter.toBeFiltered(linkName)) continue;
							
							if(Filter.isAnchorLink(linkName))
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
			String title = Filter.processTitle(page.getTitle());
			DecimalFormat df = new DecimalFormat("0.00");
			double percentage = ((double)counter/portionSize*100);
			if(counter++%(portionSize/100) == 0 )
				synchronized(this){
				System.out.print("Processing: Part "+ Id
					+"  "+df.format(percentage)+"% " +"  "+ 
						title+ " Time used: "+(System.currentTimeMillis() - startTime) / 1000+"S.");
				int time = (int)((100-percentage)/percentage*(System.currentTimeMillis() - startTime) / 1000);
				int hour = time/3600;
				int minute = (time - hour*3600)/60;
				System.out.println("Time estimate: "+hour+"h"+minute+"m.");
			}
			
			writer.writeStartElement("t");
	        writer.writeCharacters(title);
	        writer.writeEndElement();
	        writeLine();
			
			ParsedPage pp = Mparser.parse(page.getWikiText());
			
			StructureRecursion(pp.getSections(),"");
			
		} catch (Exception e)
		{
			System.out.println("XML write error!");
			// TODO: handle exception
		}
 }
}
