package com.mashwork.wikipedia.ParseXML.SmallPortion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * Detailed implementation of extracting small portion of wkiDump. A list of names will be kept
 * in HashMap called <code>toCrawl</code>. Every time the reader reads a page, check whether this is
 * the page we want. If it is, write it to file. Use this method to create a small dataset or the 
 * dataset about certain stuff, such as TV series or Sports.
 */
public class PortionExtractor
{
	//HashSet<String> toCrawl;
	HashMap<String,Integer> toCrawl;
	BufferedReader BR;
	String page;
	FileWriter FW;
	int counter = 0;
	int processed = 0;
	protected int total = 13539091;
	
	public PortionExtractor(String dir, HashMap<String,Integer> toCrawl, String output, int dumpTotalPage) throws IOException
	{
		FileInputStream FS = new FileInputStream(dir);
		InputStreamReader SR = new InputStreamReader(FS);
		BR = new BufferedReader(SR);
		this.toCrawl = toCrawl;
		FW = new FileWriter(output);
		this.total = dumpTotalPage;
	}
	
	public String returnPage()
	{
		return page;
	}
	
	public void parse() throws IOException
	{
		String line;
		String secondLine;
		String title;
		StringBuilder SB = new StringBuilder();
		
		//int limit = 1000;
		
		//preprocessing. Save all the original head information
		StringBuilder hd = new StringBuilder();
		while(!(line = BR.readLine()).contains("<page>"))
		{
			hd.append(line+"\n");
		}
		page = hd.toString();
		//System.out.println(page);
		FW.write(page);
		
		
		while((line = BR.readLine())!=null && !line.contains("</mediawiki>"))	// && limit-- >0
		{
			
			if(line.contains("<page>"))
			{
				counter++;
				//System.out.println("Counter: "+counter+" Total/100: " + total/100);
				DecimalFormat df = new DecimalFormat("0.00");
				double percentage = ((double)counter/total*100);
				if(counter%(total/100) == 0 )
				{
					System.out.println("Processing: "+ counter +"  "+df.format(percentage)+"% ");
				}
				
				
				secondLine = BR.readLine();
				title = parseTitle(secondLine);
				if(checkList(title))
				{
					SB = new StringBuilder();
					SB.append(line+"\n");
					SB.append(secondLine+"\n");
					while(!(line = BR.readLine()).contains("</page>"))
					{
						SB.append(line+"\n");
					}
					SB.append(line+"\n");
					page = SB.toString();
					//System.out.println(page);
					FW.write(page);
					processed++;
					
					if(processed%100 == 0)
					{
						FW.flush();
					}
				}
			}
		}
		System.out.println("Totally "+processed+"pages crawled.");
		FW.write(line);				//add "</mediawiki>" to the end of the file
		
		FW.flush();
		FW.close();
	}
	
	//extract the title information from the wikidump
	public String parseTitle(String line)
	{
		int start = line.indexOf("<title>");
		int end = line.indexOf("</title>");
		return line.substring(start+7, end);
	}
	
	protected boolean checkList(String query)				
    {
    	
    	//Below is the "equals String" version
    	if(toCrawl.containsKey(query))//
    	{
    		toCrawl.put(query, 1);
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
	
}
