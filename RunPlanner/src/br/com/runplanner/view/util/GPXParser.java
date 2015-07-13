package br.com.runplanner.view.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityLap;
import br.com.runplanner.domain.ActivityTack;

public class GPXParser extends GPSParser {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private File file;
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ParseException {
		File f = new File("C:\\Users\\Casa\\Desktop\\06_11_2011 08_39_36_history.gpx");
		GPXParser parser = new GPXParser(f);
		parser.parse();	
	}
	
	public GPXParser(File file) {		
		this.file=file;
	}
	
	public Activity parse() throws SAXException, IOException, ParserConfigurationException, ParseException {
		Document doc = createDocument();
		return getLaps(doc);
	}
	
	private Document createDocument() throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder(); 
		Document doc = docBuilder.parse( file );
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	private Activity getLaps(final Document doc) throws ParseException {

		Activity activityResult = new Activity();

		Element gps = doc.getDocumentElement();    	
		NodeList trkList = gps.getElementsByTagName("trk");

		List<ActivityLap> activityLapList = new ArrayList<ActivityLap>();
		List<ActivityTack> tracks = new ArrayList<ActivityTack>();
		
		for (int i = 0; i < trkList.getLength(); i++) {
			Element trk = (Element)trkList.item(i);    		
			NodeList trksegList = trk.getElementsByTagName("trkseg");

			double lapDistance = 0;
			double acumulatedDistance = 0;
			Date lapStartTime = null;
			ActivityTack lastTrack = null;
			Date timeDate = null;
			
			for (int j = 0; j < trksegList.getLength(); j++) {
				
				Element activity = (Element)trksegList.item(j);    			
				NodeList trkptList = activity.getElementsByTagName("trkpt");
				
				for (int k = 0; k < trkptList.getLength(); k++) {
					Element trkpt = (Element)trkptList.item(k);
					
					NodeList timeList = trkpt.getElementsByTagName("time");
					Element time = (Element)timeList.item(0);					
					NodeList textList = time.getChildNodes();
					String startTime = ((Node)textList.item(0)).getNodeValue().trim();	
					
					if ( startTime.length()>20 ) {
						//Retirar milesegundos que alguns aparelhos colocam
						int index = startTime.indexOf(".");						
						startTime = startTime.substring(0, index);
						startTime += "Z";
					}
					
					timeDate = sdf.parse(startTime);	
					
					NodeList eleList = trkpt.getElementsByTagName("ele");
					Element ele = (Element)eleList.item(0);
					double altitudeMeters = 0d;;
					if ( ele!=null) {
						NodeList eleTextList = ele.getChildNodes();
						String altitude = ((Node)eleTextList.item(0)).getNodeValue().trim();	
						altitudeMeters = Double.parseDouble(altitude);
					}
					double latitudeDegrees = Double.parseDouble(trkpt.getAttribute("lat")); 
					double longitudeDegrees = Double.parseDouble(trkpt.getAttribute("lon")); 
					
					ActivityTack track = new ActivityTack();
					track.setLatitudeDegrees(latitudeDegrees);
					track.setLongitudeDegrees(longitudeDegrees);
					track.setAltitudeMeters(altitudeMeters);
					track.setTime(timeDate);
					
					if ( lastTrack!=null ) {
						double distance = distance(track.getLatitudeDegrees(), track.getLongitudeDegrees(), 
								lastTrack.getLatitudeDegrees(), lastTrack.getLongitudeDegrees());
						lapDistance += distance;
						acumulatedDistance += distance;
						track.setDistanceMeters(acumulatedDistance);
						
						lastTrack = new ActivityTack();
						lastTrack.setLatitudeDegrees(latitudeDegrees);
						lastTrack.setLongitudeDegrees(longitudeDegrees);
						
						if ( lapDistance>=1000 ) {
							//Criar nova volta
							ActivityLap lap = new ActivityLap();
							lap.setDistanceMeters(lapDistance);
							lap.setStartTime(lapStartTime);

							double dif = (timeDate.getTime()-lapStartTime.getTime())/1000;
							lap.setTotalTimeSeconds(dif);
							activityLapList.add(lap);
							
							lapDistance = 0;
							lastTrack = null;
							lapStartTime = null;
						}
					}
					else {
						lastTrack = new ActivityTack();
						lastTrack.setLatitudeDegrees(latitudeDegrees);
						lastTrack.setLongitudeDegrees(longitudeDegrees);

						lapStartTime = timeDate;
						track.setDistanceMeters(0d);
					}
					
					tracks.add(track);
				}
			}
			
			//Ultima volta
			if ( lapDistance>0 && lapStartTime!=null ) {
				ActivityLap lap = new ActivityLap();
				lap.setDistanceMeters(lapDistance);
				lap.setStartTime(lapStartTime);
				
				double dif = (timeDate.getTime()-lapStartTime.getTime())/1000;
				lap.setTotalTimeSeconds(dif);
				activityLapList.add(lap);
			}
		}
		
		activityResult.setLaps(activityLapList);  
		activityResult.setTracks(tracks);
		
		return activityResult;
	}	
	
    private double distance(double p1Lat, double p1Lon, double p2Lat, double p2Lon) {
        double constant = 6371.0;
           
        p1Lat = p1Lat * Math.PI / 180.0;
        p1Lon = p1Lon * Math.PI / 180.0;
        p2Lat = p2Lat * Math.PI / 180.0;
        p2Lon = p2Lon * Math.PI / 180.0;
           
        double lat = p2Lat - p1Lat;
        double lon = p2Lon - p1Lon;
           
        double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(p1Lat) * Math.cos(p2Lat) * Math.sin(lon / 2) * Math.sin(lon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
           
        return Math.round(constant * c * 1000); // resultado em metros.
    }


	
}
