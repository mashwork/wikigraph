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

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used to create a list of names(page titles) that can be retrieved from certain start points
 * within certain steps. The minimum length of arguments is 4. When it is 4, the last one is the start point.
 * This class accepts multiple starting points. You can keep on adding page names into it. The detailed method
 * is written in <code>GetTitles</code>.java.
 */
public class ExtractTitles
{
	public static void main(String... args) throws IOException
	{
      if (args.length < 4) {
      System.out.println("USAGE: ExtractLinks <dumpDir> <output-file> <step> <query>...");
      System.out.println("You can have multiple queries.");
      System.exit(255);
  }

//		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
//		String outputFile = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/TSB_D2_Titles1.xml";
//		String query = "True Blood";
//		String query2 = "South Beach Tow";
//		String query3 = "Breaking Bad";
//		//For degree 2, use 2;
//		int step = 1;
		
		int argLength = args.length;
		String dir = args[0];
		String outputFile = args[1];
		int step = Integer.parseInt(args[2]);
		
		
		//Adding starting points
		HashMap<String,Integer> list = new HashMap<String,Integer>();
//        list.put(query, 0);
//        list.put(query2, 0);
//        list.put(query3, 0);
		for(int i = 3; i < argLength;i++)
		{
			list.put(args[i], 0);
		}
		
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
