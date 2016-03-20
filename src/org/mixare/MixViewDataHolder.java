package org.mixare;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mixare.lib.reality.Filter;
import org.mixare.lib.render.Matrix;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Internal class that holds MixViewActivity field Data.
 *
 * @author A B, KlemensE
 */
public class MixViewDataHolder {
	private float[] RTmp;
	private float[] Rot;
	private float[] I;
	private float[] grav;
	private float[] mag;
	private float[] gyro;
	private float[] angle;
	private Filter gravFilter;
	private Filter magFilter;
	private SensorManager sensorMgr;
	private Sensor sensorGrav;
	private Sensor sensorMag;
	private Sensor sensorGyro;
	private ArrayList<Sensor> sensorList;
	private int rHistIdx;
	private Matrix tempR;
	private Matrix finalR;
	private Matrix smoothR;
	private Matrix[] histR;
	private Matrix m1;
	private Matrix m2;
	private Matrix m3;
	private Matrix m4;
	private int compassErrorDisplayed;
    private float range;
	private TextView searchNotificationTxt;
    private static MixViewDataHolder instance;
    private Location curDestination;
	private Location curLocation;

	private MixViewDataHolder() {
		this.RTmp = new float[9];
		this.Rot = new float[9];
		this.I = new float[9];
		this.grav = new float[3];
		this.mag = new float[3];
		this.gyro = new float[3];
		this.angle = new float[3];
		this.gravFilter = new Filter();
		this.gravFilter.setLimit(0.5f, 1.0f);

		this.magFilter = new Filter();
		this.magFilter.setLimit(2.0f, 5.0f);

		this.rHistIdx = 0;
		this.tempR = new Matrix();
		this.finalR = new Matrix();
		this.smoothR = new Matrix();
		this.histR = new Matrix[60];
		this.m1 = new Matrix();
		this.m2 = new Matrix();
		this.m3 = new Matrix();
		this.m4 = new Matrix();
		this.compassErrorDisplayed = 0;
		this.sensorList = new ArrayList<>();

        curDestination=Config.getDefaultFix();
	}

	public synchronized static MixViewDataHolder getInstance()
	{
		if (instance == null)	{
			instance = new MixViewDataHolder();
		}
		return instance;
	}

    public Location getCurDestination() {
        return curDestination;
    }

	public Location getCurLocation() {
		return curLocation;
	}

	public void setCurLocation(Location curLocation) {
		this.curLocation = curLocation;
	}

    public void setCurDestination(Location curDestination) {
        this.curDestination = curDestination;
    }

	/* ******* Getter and Setters ********** */
	public float[] getRTmp() {
		return RTmp;
	}

	public void setRTmp(float[] rTmp) {
		RTmp = rTmp;
	}

	public float[] getRot() {
		return Rot;
	}

	public void setRot(float[] rot) {
		Rot = rot;
	}

	public float[] getI() {
		return I;
	}

	public void setI(float[] i) {
		I = i;
	}

	public float[] getGrav() {
		return grav;
	}

	public void setGrav(float[] grav) {
		this.grav = grav;
	}

	public float[] getMag() {
		return mag;
	}

	public void setMag(float[] mag) {
		this.mag = mag;
	}

	public float[] getGyro() {
		return gyro;
	}

	public void setGyro(float[] gyro) {
		this.gyro = gyro;
	}

	public float[] getAngle() {
		return angle;
	}

	public void setAngle(float[] angle) {
		this.angle = angle;
	}

	public Filter getGravFilter() {
		return gravFilter;
	}

	public Filter getMagFilter() {
		return magFilter;
	}

	public SensorManager getSensorMgr() {
		return sensorMgr;
	}

	public void setSensorMgr(SensorManager sensorMgr) {
		this.sensorMgr = sensorMgr;
	}

	public void addSensor (Sensor snr){
		sensorList.add(snr);
	}

	public void addListSensors (Collection<Sensor> listSnr){
		this.sensorList.addAll((Collection<? extends Sensor>) listSnr);
	}

	public Sensor getSensor(int location){
		return this.sensorList.get(location);
	}

	public void removeSensor (Sensor snr){
		this.sensorList.remove(snr);
	}

	/**
	 * Removes all "Stored" sensors.
	 * Please UNREGISTER them first before clearing.
	 */
	public void clearAllSensors (){
		this.sensorList.clear();
	}

	public Sensor getSensorGrav() {
		return sensorGrav;
	}

	public void setSensorGrav(Sensor sensorGrav) {
		this.sensorGrav = sensorGrav;
	}

	public Sensor getSensorMag() {
		return sensorMag;
	}

	public void setSensorMag(Sensor sensorMag) {
		this.sensorMag = sensorMag;
	}

	public Sensor getSensorGyro() {
		return sensorGyro;
	}

	public void setSensorGyro(Sensor sensorGyro) {
		this.sensorGyro = sensorGyro;
	}

	public int getrHistIdx() {
		return rHistIdx;
	}

	public void setrHistIdx(int rHistIdx) {
		this.rHistIdx = rHistIdx;
	}

	public Matrix getTempR() {
		return tempR;
	}

	public void setTempR(Matrix tempR) {
		this.tempR = tempR;
	}

	public Matrix getFinalR() {
		return finalR;
	}

	public void setFinalR(Matrix finalR) {
		this.finalR = finalR;
	}

	public Matrix getSmoothR() {
		return smoothR;
	}

	public void setSmoothR(Matrix smoothR) {
		this.smoothR = smoothR;
	}

	public Matrix[] getHistR() {
		return histR;
	}

	public void setHistR(Matrix[] histR) {
		this.histR = histR;
	}

	public Matrix getM1() {
		return m1;
	}

	public void setM1(Matrix m1) {
		this.m1 = m1;
	}

	public Matrix getM2() {
		return m2;
	}

	public void setM2(Matrix m2) {
		this.m2 = m2;
	}

	public Matrix getM3() {
		return m3;
	}

	public void setM3(Matrix m3) {
		this.m3 = m3;
	}

	public Matrix getM4() {
		return m4;
	}

	public void setM4(Matrix m4) {
		this.m4 = m4;
	}

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public String getRangeString() {
        return String.valueOf(this.range);
    }

	public int getCompassErrorDisplayed() {
		return compassErrorDisplayed;
	}

	public void setCompassErrorDisplayed(int compassErrorDisplayed) {
		this.compassErrorDisplayed = compassErrorDisplayed;
	}

	public TextView getSearchNotificationTxt() {
		return searchNotificationTxt;
	}

	public void setSearchNotificationTxt(TextView searchNotificationTxt) {
		this.searchNotificationTxt = searchNotificationTxt;
	}
}
