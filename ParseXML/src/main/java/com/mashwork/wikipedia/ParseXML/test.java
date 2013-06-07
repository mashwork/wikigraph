package com.mashwork.wikipedia.ParseXML;
//import wikipediaParser.*;
import edu.jhu.nlp.wikipedia.*;
import java.io.FileOutputStream;
import java.util.HashMap;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

//import org.codehaus.stax2.XMLOutputFactory2;
//import org.codehaus.stax2.*;

public class test {
	
	public static void main(String[] args)
	{
//      if (args.length < 4) {
//      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <query> <step>");
//      System.exit(255);
//  }
		//String dir = "/Users/Ricky/mashwork/wikiXmlParser/wiki_test.xml";
		//String dir = "/Users/Ricky/mashwork/wikidump/enwiki-20130304-pages-articles.xml.bz2";
		String dir = "/Users/Ricky/mashwork/wikidump/enwiki-20130304-pages-articles.xml";
		String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/MVN_test.xml";
		String query = "Game of Thrones (TV series)";
		int step = 4;
		
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
        	
            MyCallBackHandler MBH = new MyCallBackHandler(writer,list);
            
            //if you want to print the information to the screen, uncomment the following line
            //MBH.setPrintLinks(true);
            //MBH.setPrintStructure(true);
            //MBH.setPrintProgress(false);
            
            wxsp.setPageCallback(MBH);
                
            while(step-->0)
            {
            	wxsp.parse();
            	MBH.switchList();
            	wxsp.setPageCallback(MBH);
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
           
        }catch(Exception e) {
                e.printStackTrace();
        }
	}
}
