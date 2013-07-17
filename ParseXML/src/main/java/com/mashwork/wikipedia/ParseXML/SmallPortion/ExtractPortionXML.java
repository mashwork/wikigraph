package com.mashwork.wikipedia.ParseXML.SmallPortion;
//import wikipediaParser.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;


//import org.codehaus.stax2.XMLOutputFactory2;
//import org.codehaus.stax2.*;

public class ExtractPortionXML {
	
	public static void main(String[] args) throws IOException
	{
//      if (args.length < 4) {
//      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <query> <step>");
//      System.exit(255);
//  }

		String titleDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Titles.xml";
		String XMLDir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
		String output = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Pages.xml";
		
		String titleNotFound = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_TitlesNotFound.xml";
		FileWriter FW = new FileWriter(titleNotFound);
		
		//HashSet<String> list = loadTitles(titleDir);
		HashMap<String,Integer> list = loadTitles(titleDir);
        System.out.println("The number of pages to extract: "+list.size());
		
		
        PortionExtractor portionExtractor = new PortionExtractor(XMLDir,list,output);
        portionExtractor.parse();
        
        
        Iterator<String> it = portionExtractor.toCrawl.keySet().iterator();
        {
        	while(it.hasNext())
        	{
        		String query = it.next();
        		if(portionExtractor.toCrawl.get(query)==0)
        		{
        			FW.write(query+"\n");
        		}
        	}
        }
        FW.close();
	}
	
	public static HashMap<String,Integer> loadTitles(String dir) throws IOException
	{
		FileInputStream FS = new FileInputStream(dir);
		InputStreamReader SR = new InputStreamReader(FS);
		BufferedReader BR = new BufferedReader(SR);
		String line;
		HashMap<String,Integer> titles = new HashMap<String,Integer>();
		//HashSet<String> titles = new HashSet<String>();
		while((line = BR.readLine())!=null)
		{
			titles.put(line,0);
		}
		BR.close();
		return titles;
	}
}
