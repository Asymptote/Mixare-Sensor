/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.render.Matrix;
import org.mixare.lib.render.MixVector;

/**
 * This class calculates the bearing and pitch out of the angles
 */
public class MixState implements MixStateInterface{

	public static int NOT_STARTED = 0; 
	public static int PROCESSING = 1; 
	public static int READY = 2; 
	public static int DONE = 3; 

	int nextLStatus = MixState.NOT_STARTED;

	private float curBearing;
	private float curPitch;
	private float curRoll;

	private float xAng;
	private float yAng;
	private float zAng;


	private boolean detailsView;

    @Override
	public boolean handleEvent(MixContextInterface ctxI, String onPress) {
		return false;
	}

	public float getxAng() {
		return xAng;
	}
	public float getyAng() {
		return yAng;
	}
	public float getzAng() {
		return zAng;
	}



	public float getCurBearing() {
		return curBearing;
	}

	public float getCurPitch() {
		return curPitch;
	}

	public float getCurRoll() {
		return curRoll;
	}
	
	public boolean isDetailsView() {
		return detailsView;
	}
	
	public void setDetailsView(boolean detailsView) {
		this.detailsView = detailsView;
	}

	public void calcPitchBearing(Matrix rotationM) {
		//this.yAng = (float)-Math.sin((double)(rotationM.c1));
		this.xAng = -(float)Math.atan2(rotationM.c2,rotationM.c3);


		//this.xAng = (float)Math.atan2(rotationM.c2/Math.cos((double)xAng),rotationM.c3/Math.cos((double)xAng));
		float sq = (float)Math.sqrt((double)(rotationM.c2*rotationM.c2+rotationM.c3*rotationM.c3));
		this.yAng = (float)Math.atan2(-rotationM.c1,sq);

		//this.zAng = (float)Math.atan2(rotationM.b1/Math.cos((double)xAng),rotationM.a1/Math.cos((double)xAng));
		this.zAng = (float)Math.atan2(rotationM.b1,rotationM.a1);

		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		this.curBearing = (int) (MixUtils.getAngle(0, 0, looking.x, looking.z)  + 360 ) % 360 ;

		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
        //noinspection SuspiciousNameCombination
        this.curPitch = -MixUtils.getAngle(0, 0, looking.y, looking.z);

		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		//noinspection SuspiciousNameCombination
		this.curRoll = MixUtils.getAngle(0, 0, looking.y, looking.x);

	}
}
