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


public class GarminTCXParser extends GPSParser {
	

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ParseException {
		File f = new File("C:\\Users\\d333247\\Desktop\\21_08_2011 09_14_46_history(1).tcx");
		GarminTCXParser parser = new GarminTCXParser(f);
		parser.parse();		

	}
	private File file;

	public GarminTCXParser(File file) {
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

		Element trainingCenterDatabase = doc.getDocumentElement();    	
		NodeList activitiesList = trainingCenterDatabase.getElementsByTagName("Activities");

		List<ActivityLap> activityLapList = new ArrayList<ActivityLap>();
		List<ActivityTack> tracks = new ArrayList<ActivityTack>();

		for (int i = 0; i < activitiesList.getLength(); i++) {
			Element activities = (Element)activitiesList.item(i);    		
			NodeList activityList = activities.getElementsByTagName("Activity");

			for (int j = 0; j < activityList.getLength(); j++) {
				Element activity = (Element)activityList.item(j);    			
				NodeList lapList = activity.getElementsByTagName("Lap");

				for (int k = 0; k < lapList.getLength(); k++) {
					Element lap = (Element)lapList.item(k);
					String startTime = lap.getAttribute("StartTime");
					
					if ( startTime.length()>20 ) {
						//Retirar milesegundos que alguns aparelhos colocam
						int index = startTime.indexOf(".");						
						startTime = startTime.substring(0, index);
						startTime += "Z";
					}

					ActivityLap activityLap = new ActivityLap();

					Date date = sdf.parse(startTime);
					activityLap.setStartTime(date);
					

					getLapData(lap, activityLap);
					getTracks(lap, tracks);

					activityLapList.add(activityLap);
				}	
			}
		}
		activityResult.setLaps(activityLapList);  
		activityResult.setTracks(tracks);
		return activityResult;
	}

	private void getLapData(Element lap, ActivityLap activityLap) {
		
		Node totalTimeNode = lap.getElementsByTagName("TotalTimeSeconds").item(0);
		if ( totalTimeNode !=null ) {
			NodeList textList = totalTimeNode.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setTotalTimeSeconds(Double.parseDouble(text));
		}

		Node distanceMeters = lap.getElementsByTagName("DistanceMeters").item(0);
		if ( distanceMeters!=null ) {
			NodeList textList = distanceMeters.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setDistanceMeters(Double.parseDouble(text));
		}

		Node maximumSpeed = lap.getElementsByTagName("MaximumSpeed").item(0);
		if ( maximumSpeed!=null ) {
			NodeList textList = maximumSpeed.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setMaximunSpeed(Double.parseDouble(text));
		}
		
		Node calories = lap.getElementsByTagName("Calories").item(0);
		if ( calories!=null ) {
			NodeList textList = calories.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setCalories(Double.parseDouble(text));
		}

		Element averageHeartRateBpm = (Element)lap.getElementsByTagName("AverageHeartRateBpm").item(0);
		if ( averageHeartRateBpm!=null ) {
			Node averageHeartRateBpmValue = averageHeartRateBpm.getElementsByTagName("Value").item(0);
			NodeList textList = averageHeartRateBpmValue.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setAverageHeartRateBpm(Double.parseDouble(text));
		}

		Element maximumHeartRateBpm = (Element)lap.getElementsByTagName("MaximumHeartRateBpm").item(0);
		if ( maximumHeartRateBpm!=null ) {
			Node maximumHeartRateBpmValue = maximumHeartRateBpm.getElementsByTagName("Value").item(0);
			NodeList textList = maximumHeartRateBpmValue.getChildNodes();
			String text = ((Node)textList.item(0)).getNodeValue().trim();
			if ( text.equals("") ) text = "0.0";
			activityLap.setMaximumHeartRateBpm(Double.parseDouble(text));
		}
	}	    
	
	
	private void getTracks(Element lap, List<ActivityTack> tracks) {
		
		
		NodeList trackList = lap.getElementsByTagName("Track");
		
		for( int tIndex=0; tIndex<trackList.getLength(); tIndex++ ) {
			Element track = (Element)lap.getElementsByTagName("Track").item(tIndex);
			
			if ( track!=null ) {
				
				NodeList trackpoints = track.getChildNodes();
				
				for (int i=0; i<trackpoints.getLength(); i++) {
					Node trackpoint = trackpoints.item(i);
					NodeList trackpointData = trackpoint.getChildNodes();
					
					ActivityTack activityTrack = new ActivityTack();
					for (int j=0; j<trackpointData.getLength(); j++) {
						
						Node position = trackpointData.item(j);
						if ( position.getNodeName().equals("Position") ) {
							
							NodeList positionData = position.getChildNodes();						
							for (int k=0; k<positionData.getLength(); k++) {
								Node data = positionData.item(k);
								
								String nodeData = data.getTextContent();
								
								if ( data.getNodeName().equals("LatitudeDegrees") ) {
									activityTrack.setLatitudeDegrees( Double.parseDouble(nodeData) );
								}
								else if ( data.getNodeName().equals("LongitudeDegrees") ) {
									activityTrack.setLongitudeDegrees( Double.parseDouble(nodeData) );
								}
							}
						}
						else if ( position.getNodeName().equals("AltitudeMeters") ) {
							String nodeData = position.getTextContent();
							activityTrack.setAltitudeMeters( Double.parseDouble(nodeData) );							
						}
						else if ( position.getNodeName().equals("DistanceMeters") ) {
							String nodeData = position.getTextContent();
							activityTrack.setDistanceMeters( Double.parseDouble(nodeData) );
						}
						else if ( position.getNodeName().equals("Time") ) {
							String nodeData = position.getTextContent();
							
							if ( nodeData.length()>20 ) {
								//Retirar milesegundos que alguns aparelhos colocam
								int index = nodeData.indexOf(".");						
								nodeData = nodeData.substring(0, index);
								nodeData += "Z";
							}

							Date date;
							try {
								date = sdf.parse(nodeData);
								activityTrack.setTime(date);
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
					if( activityTrack.getLatitudeDegrees()!=null && activityTrack.getLongitudeDegrees()!=null ) {
						tracks.add(activityTrack);
					}
				}
			}			
		}
		
		
	}

}
