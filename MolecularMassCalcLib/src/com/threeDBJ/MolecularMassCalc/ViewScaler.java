package com.threeDBJ.MolecularMassCalcLib;

import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout.LayoutParams;

import android.util.Log;

public class ViewScaler extends ScaleAnimation {

    private View mView;

    private LayoutParams mLayoutParams;

    private int mMarginBottomFromY, mMarginBottomToY;

    private boolean mVanishAfter = false;

    public ViewScaler(float fromX, float toX, float fromY, float toY, int duration, View view,
		    boolean vanishAfter) {
	super(fromX, toX, fromY, toY);
	setDuration(duration);
	mView = view;
	mVanishAfter = vanishAfter;
	mLayoutParams = (LayoutParams) view.getLayoutParams();
	int height = mView.getHeight();
	mMarginBottomFromY = (int) (height * fromY) + mLayoutParams.bottomMargin - height;
	mMarginBottomToY = (int) (0 - ((height * toY) + mLayoutParams.bottomMargin)) - height;
	//mMarginBottomFromY = (fromY == 0.0f) ? 0 : -1 * height;
	//mMarginBottomToY = (toY == 0.0f) ? 0 : -1 * height;
	Log.e("mmc", height+" "+mLayoutParams.bottomMargin);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
	super.applyTransformation(interpolatedTime, t);
	if (interpolatedTime < 1.0f) {
	    int newMarginBottom = mMarginBottomFromY
		+ (int) ((mMarginBottomToY - mMarginBottomFromY) * interpolatedTime);
	    mLayoutParams.setMargins(mLayoutParams.leftMargin, mLayoutParams.topMargin,
				     mLayoutParams.rightMargin, newMarginBottom);
	    mView.getParent().requestLayout();
	    mView.setVisibility(View.VISIBLE);
	} else {
	    if (mVanishAfter) {
		mView.setVisibility(View.GONE);
	    }
	    
	}
    }
}