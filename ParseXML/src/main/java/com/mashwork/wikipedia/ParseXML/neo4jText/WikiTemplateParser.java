package com.mashwork.wikipedia.ParseXML.neo4jText;

import java.net.URLDecoder;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiTemplateParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ResolvedTemplate;

public class WikiTemplateParser implements MediaWikiTemplateParser
{
	private final String templatePrefix = "";
	private final String templatePostfix = "\n";
	private final String parameterDivisor = "  ";
	
	public ResolvedTemplate parseTemplate(Template t, ParsedPage pp) {
		ResolvedTemplate result = new ResolvedTemplate( t );
		//result.setPreParseReplacement( ResolvedTemplate.TEMPLATESPACER );
		result.setPreParseReplacement(" ");
		
		StringBuilder sb = new StringBuilder();
		sb.append(templatePrefix);
		//sb.append( t.getName()+parameterDivisor );
		for( String s: t.getParameters()){
			if(!toFilter(s))
			{
				sb.append( toNormalLink(s) +parameterDivisor);
			}
		}
		//sb.delete( sb.length()-parameterDivisor.length(), sb.length() );
		sb.append(templatePostfix);
		result.setPostParseReplacement( sb.toString() );
		
		result.setParsedObject( t );
		return result;
	}
	
	private boolean toFilter(String s)
	{
		if(s.contains("File:") || s.contains("url=")|| s.contains("http://"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private String toNormalLink(String link)
	{
		if(link==null) return null;
    	link =  link.substring(0,1).toUpperCase()+link.substring(1,link.length());
    	try{
    		link = URLDecoder.decode(link,"UTF-8");
    	}catch(Exception e)
    	{
    		
    	}
    	link = link.replace('=',' ');
    	link = link.replace("[["," ");
    	link = link.replace("]]"," ");
    	link = link.replace("<br />"," ");
		return link.replace('_',' ');
	}
	
	public String configurationInfo(){
		return "shows the Template names and all parameters";
	}

}
