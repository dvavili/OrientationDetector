package edu.cmu.orientation;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class MyLocationService {

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in
																																			// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 500; // in
																																	// Milliseconds

	LocationManager myLocationManager;
	private Location curLocation;
	private Float trueNorthAngleDiff;

	public MyLocationService(LocationManager locMgr) {
		curLocation = null;
		trueNorthAngleDiff = new Float(0);
		myLocationManager = locMgr;
		myLocationManager
				.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES,
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
	}

	public Location getCurLocation() {
		if (curLocation != null) {
			synchronized (curLocation) {
				return curLocation;
			}
		}
		return null;
	}

	public void setCurLocation(Location curLoc) {
		if (curLocation == null) {
			curLocation = curLoc;
			GeomagneticField field = new GeomagneticField((float)curLocation.getLatitude(), (float)curLocation.getLongitude(), (float)curLocation.getAltitude(), curLocation.getTime());
			setTrueNorth(field.getDeclination());
		} else {
			synchronized (curLocation) {
				curLocation = curLoc;
				GeomagneticField field = new GeomagneticField((float)curLocation.getLatitude(), (float)curLocation.getLongitude(), (float)curLocation.getAltitude(), curLocation.getTime());
				setTrueNorth(field.getDeclination());
			}
		}
	}

	private void setTrueNorth(float bearing) {
		synchronized (trueNorthAngleDiff) {
			trueNorthAngleDiff = bearing;
		}
	}

	public Float getTrueNorthAngleDiff() {
		synchronized (trueNorthAngleDiff) {
			return trueNorthAngleDiff;
		}
	}

	class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			setCurLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}
}
