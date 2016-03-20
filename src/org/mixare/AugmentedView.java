package org.mixare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class AugmentedView extends View {
	MixViewActivity mixViewActivity;

	public AugmentedView(Context context) {
		super(context);

		try {
			mixViewActivity = (MixViewActivity) context;

			mixViewActivity.killOnError();
		} catch (Exception ex) {
			mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			mixViewActivity.killOnError();
			MixViewActivity.getPaintScreen().setCanvas(canvas);

			if (!mixViewActivity.getMarkerRenderer().isInited()) {
                mixViewActivity.getMarkerRenderer().init(MixViewActivity.getPaintScreen());
			}

            mixViewActivity.getMarkerRenderer().draw();
		} catch (Exception ex) {
			mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
		}
	}
}