package br.com.runplanner.util;

import java.io.File;

import br.com.runplanner.view.util.Constants;

public class Utils {

	public static String formataTempo(long elapsed) {  
		long ss = elapsed % 60;  
		elapsed /= 60;  
		long min = elapsed % 60;  
		elapsed /= 60;  
		long hh = elapsed;// % 24;  
		return strzero(hh) + ":" + strzero(min) + ":" + strzero(ss);  
	} 
	
	
	public static String strzero(long n) {  
		if(n < 10)  
			return "0" + String.valueOf(n);  
		return String.valueOf(n);  
	}
	
	public static double distance(double p1Lat, double p1Lon, double p2Lat, double p2Lon) {
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
	
	public static boolean verifyUserPhotoPath() {
		return verifyPath(Constants.PHOTO_PATH);
	} 
	
	public static boolean verifyAdvicePhotoPath() {
		return verifyPath(Constants.PHOTO_PATH);
	} 
	
	public static boolean verifyAdviceBannerPhotoPath() {
		return verifyPath(Constants.BANNER_PATH);
	} 
	
	private static boolean verifyPath(String path) {
		File dir = new File(path);
		boolean result = true;
		if ( !dir.exists() ) {
			result = false;
			try {
				result = dir.mkdir();			
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
		return result;
	} 
		
			
}
