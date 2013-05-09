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
        AnimationListener animationListener = null;

        boolean isAnimating() {
            long current = System.currentTimeMillis();
            return startTime < current && Math.abs(distanceLeft) > 1e-3;
        }

        void calculateAnimation() {
            long current = System.currentTimeMillis();
            long timeDiff = current - lastFrameTime;
            animationTime += timeDiff;
            float timeNormalized = animationTime / (float) duration;
            float interpolatedMultiplier = interpolator.getInterpolation(timeNormalized);

            if (timeNormalized < 1.0f) {
                frameTranslationX = (distanceLeft) * interpolatedMultiplier;
                distanceLeft -= frameTranslationX;
            } else {
                frameTranslationX = distanceLeft;
                distanceLeft = 0;
                Log.e("Animation", "STOP");
                if (animationListener != null) {
                    animationListener.onAnimationStop();
                }
            }

            Log.d("Animation",
 String.format("Time d: %d interpolated: %f trans x: %f norm: %f", timeDiff, interpolatedMultiplier,
                            frameTranslationX, timeNormalized));
            lastFrameTime = current;
        }

        float getFrameTranslationX() {
            return frameTranslationX;
        }

        void startAnimation() {
            distanceLeft = toX - fromX;
            animationTime = 0;
            lastFrameTime = startTime;
            if(animationListener != null) {
                animationListener.onAnimationStart();
            }
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

    public void animTranslation(float fromX, float toX, long durationMs) {
        animTranslation(fromX, toX, durationMs, null, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, AnimationListener listener) {
        animTranslation(fromX, toX, durationMs, listener, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, AnimationListener listener, Interpolator interpolator) {
        // animation = new CustomTranslateAnimation();
        // animation.fromX = fromX;
        // animation.toX = toX;
        // animation.startTime = System.currentTimeMillis();
        // animation.duration = durationMs;
        // animation.interpolator = interpolator == null ? new DecelerateInterpolator() :
        // interpolator;
        // animation.animationListener = listener;
        // animation.startAnimation();
        // invalidate();
        Log.e("animTranslation", fromX + " " + toX);
        translateAnimation = new TranslateAnimation(fromX, toX, 0, 0);
        translateAnimation.setDuration(durationMs);
        translateAnimation.setInterpolator(interpolator == null ? new DecelerateInterpolator() : interpolator);
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                translateAnimation = null;
            }
        });
        translateAnimation.initialize(getWidth(), getHeight(), 0, 0);
        translateAnimation.start();
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
        long currentTime = System.currentTimeMillis();
        if (translateAnimation != null) {
            translateAnimation.getTransformation(currentTime, transformation);
            Log.d("onDraw", transformation.toString());
            translationMatrix.preConcat(transformation.getMatrix());
            invalidate();
        }

        // if (animation != null && animation.isAnimating()) {
        // animation.calculateAnimation();
        // moveBy(animation.getFrameTranslationX(), 0);
        // }
        matrix.preConcat(translationMatrix);
        canvas.setMatrix(translationMatrix);
        canvas.drawPaint(paint);
        super.onDraw(canvas);
    }

}
