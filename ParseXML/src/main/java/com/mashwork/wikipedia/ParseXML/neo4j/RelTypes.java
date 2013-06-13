package com.mashwork.wikipedia.ParseXML.neo4j;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType{
	TOC, INTERNAL,CATEGORY, ANCHOR

}
