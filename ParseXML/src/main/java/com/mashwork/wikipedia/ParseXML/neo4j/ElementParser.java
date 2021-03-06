package com.mashwork.wikipedia.ParseXML.neo4j;

import java.util.*;
import java.io.*;

import javax.xml.stream.*;
import javax.xml.stream.events.*;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

//import org.codehaus.stax2.XMLInputFactory2;

@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public abstract class ElementParser {
	private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();	//***
	
	protected   GraphDatabaseService graphDb;
    protected  Index<Node> nodeIndex;
    protected  Index<Node> TocIndex;
    protected  Index<Node> fullTextIndex;
	
	public ElementParser(GraphDatabaseService graphDb, Index<Node> nodeIndex, 
			Index<Node> TocIndex, Index<Node> fullTextIndex)
	{
		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.TocIndex = TocIndex;
		this.fullTextIndex = fullTextIndex;
	}
	
	public void parse(String fileName) throws IOException, XMLStreamException {
           parse(new FileInputStream(fileName));
    }
	
	protected abstract void handleElement(String element, String value);
	
	private void parse(InputStream inputStream) throws IOException, XMLStreamException {
        XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream, "UTF-8");
        try {
            parseElements(reader);
        } finally {
            reader.close();
            inputStream.close();
        }
    }
	
	private void parseElements(XMLStreamReader reader) throws XMLStreamException {
        LinkedList<String> elementStack = new LinkedList<String>();
        StringBuilder textBuffer = new StringBuilder();
        
        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLEvent.START_ELEMENT:
                elementStack.push(reader.getName().getLocalPart());
                textBuffer.setLength(0);
                break;
            case XMLEvent.END_ELEMENT:
                String element = elementStack.pop();
                handleElement(element, textBuffer.toString().trim());
                break;
            case XMLEvent.CHARACTERS:
                textBuffer.append(reader.getText());
                break;
            }
        }
    }
}
