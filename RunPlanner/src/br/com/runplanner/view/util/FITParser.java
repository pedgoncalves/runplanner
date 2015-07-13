package br.com.runplanner.view.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.garmin.fit.Decode;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;

import br.com.runplanner.domain.Activity;
import br.com.runplanner.domain.ActivityLap;
import br.com.runplanner.domain.ActivityTack;

/**
 * Classe responsável por tratar os arquivos do tipo Fit
 * @author Daniel
 */
public class FITParser extends GPSParser implements RecordMesgListener, LapMesgListener {
	
	private File file;
	private List<ActivityLap> laps = new ArrayList<ActivityLap>();
	private List<ActivityTack> tracks = new ArrayList<ActivityTack>();
	
	public FITParser(File file) {
		this.file = file;
	}

	public Activity parse() throws SAXException, IOException,
			ParserConfigurationException, ParseException {
		
		Decode decode = new Decode();
		MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
		Activity activityResult = new Activity();
		
		FileInputStream in;

		try {
			in = new FileInputStream(file);
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error opening file " + file + " [1]");
		}

		try {
			if (!Decode.checkIntegrity((InputStream) in))
				throw new RuntimeException("FIT file integrity failed." + file);
		} finally {
			try {
				in.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			in = new FileInputStream(file);
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error opening file " + file + " [2]");
		}

		mesgBroadcaster.addListener((RecordMesgListener) this);
		mesgBroadcaster.addListener((LapMesgListener) this);

		try {
			mesgBroadcaster.run(in);
		} catch (FitRuntimeException e) {
			System.err.print("Exception decoding file: "+file.toString());
			System.err.println(e.getMessage());

			try {
				in.close();
			} catch (java.io.IOException f) {
				throw new RuntimeException(f);
			}

		}

		try {
			in.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		
		activityResult.setLaps(laps);
		activityResult.setTracks(tracks);
		return activityResult;
	}

	public void onMesg(LapMesg lap) {
		ActivityLap activityLap = new ActivityLap();
		
		if(lap.getStartTime() != null){
			activityLap.setStartTime(lap.getStartTime().getDate());
		}
		
		if(lap.getAvgHeartRate() != null) {
			activityLap.setAverageHeartRateBpm(lap.getAvgHeartRate().doubleValue());
		}
		
		if(lap.getMaxHeartRate() != null) {
			activityLap.setMaximumHeartRateBpm(lap.getMaxHeartRate().doubleValue());
		}
		
		if(lap.getTotalCalories() != null) {
			activityLap.setCalories(lap.getTotalCalories().doubleValue());
		}
		
		if(lap.getTotalDistance() != null) {
			activityLap.setDistanceMeters(lap.getTotalDistance().doubleValue());
		}
		
		if(lap.getMaxSpeed() != null) {
			activityLap.setMaximunSpeed(lap.getMaxSpeed().doubleValue());
		}
		
		if(lap.getTotalElapsedTime() != null) {
			activityLap.setTotalTimeSeconds(lap.getTotalElapsedTime().doubleValue());
		}
		
		this.laps.add(activityLap);
	}

	public void onMesg(RecordMesg track) {
		if (track.getPositionLat() != null && track.getPositionLong() != null) {
			ActivityTack activityTrack = new ActivityTack();
			
			if(track.getDistance() != null) {
				activityTrack.setDistanceMeters(track.getDistance().doubleValue());
			}
			
			activityTrack.setLatitudeDegrees(semicircleToDegrees(track.getPositionLat().doubleValue()));
			activityTrack.setLongitudeDegrees(semicircleToDegrees(track.getPositionLong().doubleValue()));
			
			if(track.getAltitude() != null) {
				activityTrack.setAltitudeMeters(track.getAltitude().doubleValue());
			}
			
			if(track.getTimestamp() != null) {
				activityTrack.setTime(track.getTimestamp().getDate());
			}
			
			this.tracks.add(activityTrack);
		}
	}

}
