package br.com.runplanner.view.util;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import br.com.runplanner.domain.Activity;

public abstract class GPSParser {
	public abstract Activity parse() throws SAXException, IOException, ParserConfigurationException, ParseException;
	
	public double semicircleToDegrees(Double semicircle){
		return semicircle*(180/Math.pow(2, 31)); 
	}
}
