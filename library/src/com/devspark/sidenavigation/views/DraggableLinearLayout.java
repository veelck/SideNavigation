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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * @author Damian Walczak
 *
 */
public class DraggableLinearLayout extends LinearLayout {

    class CustomTranslateAnimation {

        float fromX;
        float toX;

        long startTime = 0;
        long duration = 0;
        long animationTime = 0;

        long lastFrameTime = 0;

        float frameTranslationX;

        float distanceLeft = 0;

        Interpolator interpolator;

        boolean isAnimating() {
            long current = System.currentTimeMillis();
            return startTime < current && Math.abs(distanceLeft) > 1e-3;
        }

        void calculateAnimation() {
            long current = System.currentTimeMillis();
            if (lastFrameTime == 0) {
                lastFrameTime = startTime;
            }
            long timeDiff = current - lastFrameTime;
            animationTime += timeDiff;
            float interpolatedTime = interpolator.getInterpolation(animationTime / (float) duration);

            if (animationTime < duration) {
                frameTranslationX = (distanceLeft) * interpolatedTime;
                distanceLeft -= frameTranslationX;
            } else {
                frameTranslationX = distanceLeft;
                distanceLeft = 0;
            }

            Log.d("Animation", String.format("Time d: %d interpolated: %f trans x: %f", timeDiff, interpolatedTime, frameTranslationX));
            lastFrameTime = current;
        }

        float getFrameTranslationX() {
            return frameTranslationX;
        }

        void startAnimation() {
            distanceLeft = toX - fromX;
            animationTime = 0;
        }
    }

    Matrix translationMatrix;
    Paint paint = new Paint();
    float[] matrixValues = new float[9];

    CustomTranslateAnimation animation = null;


    public DraggableLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        translationMatrix = new Matrix();
        paint.setColor(0x0f00ff00);
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

    public void animTranslation(float fromX, float toX, long durationMs, Interpolator interpolator) {
        animation = new CustomTranslateAnimation();
        animation.fromX = fromX;
        animation.toX = toX;
        animation.startTime = System.currentTimeMillis();
        animation.duration = durationMs;
        animation.interpolator = interpolator == null ? new DecelerateInterpolator() : interpolator;
        animation.startAnimation();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        moveBy(-getWidth(), 0);
        Log.e("onlayout", "called");
        // translationMatrix.preTranslate(-getWidth(), 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Matrix matrix = canvas.getMatrix();
        if (animation != null && animation.isAnimating()) {
            animation.calculateAnimation();
            moveBy(animation.getFrameTranslationX(), 0);
        }
        matrix.preConcat(translationMatrix);
        canvas.setMatrix(translationMatrix);
        canvas.drawPaint(paint);
        super.onDraw(canvas);
    }

}
