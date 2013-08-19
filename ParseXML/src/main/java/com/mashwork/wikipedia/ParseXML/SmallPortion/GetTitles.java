package com.mashwork.wikipedia.ParseXML.SmallPortion;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.mashwork.wikipedia.ParseXML.MyCallBackHandler;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import edu.jhu.nlp.wikipedia.WikiPage;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class will keep on tracking a list of names and steps. It will expand from certain points in certain
 * steps and write all the page titles that can be retrieved into file.
 */
public class GetTitles extends MyCallBackHandler
{
	public HashSet<String> titles;
	public GetTitles(XMLStreamWriter writer, HashMap<String, Integer> list,
			int step)
	{
		super(writer, list, step);
		titles = new HashSet<String>(2000000);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	//extract all the links from a page
	public void StructureRecursion(List<Section> sectionList, String l) throws XMLStreamException
	{
		int k = 1;
		for(Section section:sectionList)
		{
			if(section.getTitle() != null)
			{
				titles.add(section.getTitle());
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
							
							String[] pageNames;
							if(isAnchorLink(linkName))
							{
								pageNames = linkName.split("#");
								tryAddPage(toNormalLink(pageNames[0]));
							}
							else
							{
								pageNames = linkName.split("\n");
								//tryAddPage(link.getTarget().toString());
								tryAddPage(toNormalLink(pageNames[0]));
							}
							linkCount++;
					        titles.add(toNormalLink(pageNames[0]));
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
			
			if((pageCount%1000) == 0) writer.flush();

			if(!checkList(title)) return;
			pageCount++;
	        titles.add(title);
	
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