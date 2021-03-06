package com.mashwork.wikipedia.ParseXML;
//import wikipediaParser.*;
import edu.jhu.nlp.wikipedia.*;
import java.io.FileOutputStream;
import java.util.HashMap;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

//import org.codehaus.stax2.XMLOutputFactory2;
//import org.codehaus.stax2.*;

public class ExtractXML {
	
	public static void main(String[] args)
	{
//      if (args.length < 4) {
//      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <query> <step>");
//      System.exit(255);
//  }
		//String dir = "/Users/Ricky/mashwork/wikiXmlParser/20130304/wiki_test.xml";
		//String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml.bz2";
		//String dir = "/Users/Ricky/mashwork/wikidump/new/GameOfThrones.xml";
		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
		//String dir = "/Users/Ricky/mashwork/wikidump/old/Mytest.xml";
		//String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/test.xml";
		String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/test.xml";
		//String query = "Game of Thrones (TV series)";
		String query = "Anarchism";
		int step = 1;
		
		HashMap<String,Integer> list = new HashMap<String,Integer>();
        list.put(query, 0);
        //list.put("Total_Page_to_Process",1);
		
        //long startTime = System.currentTimeMillis();
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(dir);
        
        try {
        	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        	XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            writer.writeStartDocument();
            writer.writeStartElement("d");
        	
            MyCallBackHandler MBH = new MyCallBackHandler(writer,list,step);
            
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
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
            
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            
            System.out.println("Totally "+MyCallBackHandler.pageCount+" pages  crawled. Totally "
            		+MyCallBackHandler.linkCount+" links crawled.");
            System.out.println("Totally "+elapsedSeconds+" seconds used.");
        }catch(Exception e) {
                e.printStackTrace();
        }
	}
}
