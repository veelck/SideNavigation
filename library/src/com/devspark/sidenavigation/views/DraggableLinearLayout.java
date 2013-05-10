/**
 *
 */
package com.devspark.sidenavigation.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

/**
 * @author Damian Walczak
 *
 */
public class DraggableLinearLayout extends LinearLayout {

    public interface AnimationListener {
        public void onAnimationStart();

        public void onAnimationStop();
    }

    TranslateAnimation translateAnimation;
    Transformation transformation = new Transformation();

    Matrix translationMatrix;
    Matrix tmpMatrix = new Matrix();
    Paint paint = new Paint();
    float[] matrixValues = new float[9];

    public DraggableLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        translationMatrix = new Matrix();
    }

    public DraggableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        translationMatrix = new Matrix();
        paint.setColor(0x0f00ff00);
    }

    public void moveBy(float dx, float dy) {
        translationMatrix.preTranslate(dx, dy);
        invalidate();
    }

    public void setTransX(float transX) {
        translationMatrix.getValues(matrixValues);
        matrixValues[Matrix.MTRANS_X] = transX;
        translationMatrix.setValues(matrixValues);
        invalidate();
    }

    public void setTransY(float transY) {
        translationMatrix.getValues(matrixValues);
        matrixValues[Matrix.MTRANS_Y] = transY;
        translationMatrix.setValues(matrixValues);
        invalidate();
    }

    public float getTransX() {
        translationMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_X];
    }

    public float getTransY() {
        translationMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_Y];
    }

    public void animTranslation(float fromX, float toX, long durationMs) {
        animTranslation(fromX, toX, durationMs, null, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, AnimationListener listener) {
        animTranslation(fromX, toX, durationMs, listener, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, final AnimationListener listener, Interpolator interpolator) {
        Log.e("animTranslation", fromX + " " + toX);
        translateAnimation = new TranslateAnimation(fromX, toX, 0, 0);
        translateAnimation.setDuration(durationMs);
        translateAnimation.setInterpolator(interpolator == null ? new DecelerateInterpolator() : interpolator);
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if (listener != null) {
                    listener.onAnimationStart();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                translateAnimation = null;
                if (listener != null) {
                    listener.onAnimationStop();
                }
            }
        });
        int parentWidth = 0, parentHeight = 0;
        translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
        translateAnimation.initialize(getWidth(), getHeight(), parentWidth, parentHeight);
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // initialize the position of the drawer to be outside visible part of the screen.
        moveBy(-getWidth(), 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * since Google changed the logic of managing matrix, from API 16 the getMatrix() of the
         * canvas was deprecated Managing of the transformation matrix was moved to the view in API
         * 11, but since we want to support versions since API 7, we have to stick to this
         * deprecated method.
         */
        canvas.getMatrix(tmpMatrix);
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (translateAnimation != null) {
            translateAnimation.getTransformation(currentTime, transformation);
            transformation.getMatrix().getValues(matrixValues);
            translationMatrix.setValues(matrixValues);
            Log.d("onDraw", translationMatrix.toString());
            invalidate();
        }

        tmpMatrix.postConcat(translationMatrix);
        canvas.setMatrix(tmpMatrix);
        canvas.drawPaint(paint);
        super.onDraw(canvas);
    }

}
