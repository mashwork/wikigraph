package com.mashwork.wikipedia.ParseXML.SmallPortion;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.mashwork.wikipedia.ParseXML.MyCallBackHandler;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class ExtractTitles
{
	public static void main(String[] args) throws IOException
	{
//      if (args.length < 4) {
//      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <query> <step>");
//      System.exit(255);
//  }

		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
		String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Titles.xml";
		//String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/ListCharacD1.xml";
		String query = "Game of Thrones";
		//String query = "List of A Song of Ice and Fire characters";
		int step = 2;
		HashMap<String,Integer> list = new HashMap<String,Integer>();
        list.put(query, 0);
		
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(dir);
        
        try {
        	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        	XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        	
        	GetTitles MBH = new GetTitles(writer,list,step);
            
            //if you want to print the information to the screen, uncomment the following line
            //MBH.setPrintLink(true);
            //MBH.setPrintStructure(true);
            //MBH.setPrintProgress(false);
            
            wxsp.setPageCallback(MBH);
                
            long startTime = System.currentTimeMillis();
            
            while(true)
            {
            	if(MBH.timeToStop())
            	{
            		break;
            	}
            	wxsp.parse();
            	MyCallBackHandler.level++;
            	wxsp.setPageCallback(MBH);
            }
            
            Iterator<String> it = MBH.titles.iterator();
            while(it.hasNext())
            {
            	writer.writeCharacters(it.next()+"\n");
            }
            writer.close();
            
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            
            System.out.println("Totally "+MyCallBackHandler.pageCount+" pages  crawled. Totally ");
            System.out.println("Totally "+elapsedSeconds+" seconds used.");
        }catch(Exception e) {
                e.printStackTrace();
        }

	}
}
