package com.mashwork.wikipedia.ParallelXML.ParallelLucene;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;


import com.mashwork.wikipedia.ParallelXML.Filter;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
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

public class ParallelIndexBuilder implements PageCallbackHandler
{
	//String DumpDir;
	//String IndexDir;
	IndexWriter indexWriter;
	ModularParser Mparser;
	StackTracker stackTracker;
	
	int Id;
	int counter = 0;
	int total = 762385/8;
	int docCount = 0;
	long startTime = System.currentTimeMillis();
	
	public ParallelIndexBuilder(IndexWriter indexWriter, int Id)
	{
		stackTracker = new StackTracker();
		
		this.indexWriter = indexWriter;
		this.Id = Id;
		
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
	    List<String> imageIdentifiers = new ArrayList<String>();
	    imageIdentifiers.add("file");
	    pf.setImageIdentifers(imageIdentifiers);       
	    pf.setShowImageText(true);
	    MediaWikiParser parser = pf.createParser();
		this.Mparser = (ModularParser)parser;
		this.Mparser.setTemplateParser(new WikiTemplateParser());
	}
	
	public void addText2Index(String text)
	{
		String title = stackTracker.getPath();
		Document doc = new Document();
		doc.add(new Field("title", title, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("text", text, Field.Store.NO,
				Field.Index.ANALYZED));
		docCount++;
		try
		{
			indexWriter.addDocument(doc);
		}catch(IOException e)
		{
			System.out.println("Adding lucene index error!");
		}
		try
		{
			flush();
		}catch(Exception e)
		{
			System.out.println("Flush error!");
			e.printStackTrace();
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
				stackTracker.tractPath(pair);
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
					addText2Index(text);
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
	
	public void flush() throws Exception
	{
		if(docCount%10000 == 0)
		{
			indexWriter.commit();
		}
	}
	
	public void printStatus()
	{
		DecimalFormat df = new DecimalFormat("0.00");
		double percentage = ((double)counter/total*100);
		if(counter++%(total/100) == 0 )
		{
			System.out.print("Processing: part "+Id+" "+ counter++
				+"  "+df.format(percentage)+"%   Time used: "+(System.currentTimeMillis() - startTime) / 1000+"S.");
			int time = (int)((100-percentage)/percentage*(System.currentTimeMillis() - startTime) / 1000);
			int hour = time/3600;
			int minute = (time - hour*3600)/60;
			System.out.println("Time estimate: "+hour+"h"+minute+"m.");
		}
	}
	
	public void process(WikiPage page){

			String title = Filter.processTitle(page.getTitle());		
			
			printStatus();
		
			Pair<String,String> pair = new Pair<String,String>("t",title);
			stackTracker.tractPath(pair);
			
			ParsedPage pp = Mparser.parse(page.getWikiText());
			
			StructureRecursion(pp.getSections(),"");

 }
}
