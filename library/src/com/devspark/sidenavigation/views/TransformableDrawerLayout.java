/**
 *
 */
package com.devspark.sidenavigation.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.sidenavigation.R;
import com.devspark.sidenavigation.SideNavigationView;

/**
 * A LinearLayout, that has possibility of being moved using transformation matrix. All changes
 * applied using {@link TransformableDrawerLayout#moveBy(float, float)} or
 * {@link TransformableDrawerLayout#setTransX(float)},
 * {@link TransformableDrawerLayout#setTransY(float)} is applied using canvas transformation.
 *
 * If the layout is to be used (e.g. items in it clicked when transformed), the touch events
 * handling should be added, since current implementation doesn't translate touch events by applied
 * transformations.
 * 
 * @author Damian Walczak
 *
 */
public class TransformableDrawerLayout extends LinearLayout {

    public static final boolean DEBUG_LOG = SideNavigationView.DEBUG_LOG;

    public interface AnimationListener {
        public void onAnimationStart();

        public void onAnimationStop();
    }

    public interface OpenningProgressListener {
        public void onProgress(float progress);
    }

    TranslateAnimation translateAnimation;
    Transformation transformation = new Transformation();

    Matrix translationMatrix;
    Matrix tmpMatrix = new Matrix();
    Paint paint = new Paint();
    float[] matrixValues = new float[9];

    OpenningProgressListener openningProgressListener;

    Handler handler = new Handler();

    View contentView;
    View shadowView;
    ImageView ivHandle;
    Runnable progressReporter = new Runnable() {
        @Override
        public void run() {
            if (openningProgressListener != null) {
                openningProgressListener.onProgress(getPercentOpen());
            }
        }
    };

    public TransformableDrawerLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        translationMatrix = new Matrix();
    }

    public TransformableDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        translationMatrix = new Matrix();
        paint.setColor(0x0f00ff00);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        contentView = findViewById(R.id.side_navigation_content);
        shadowView = findViewById(R.id.shadow);
        ivHandle = (ImageView) findViewById(R.id.side_navigation_handle);
        hideMenuContent();
    }

    public void setOpenningProgressListener(OpenningProgressListener openningProgressListener) {
        this.openningProgressListener = openningProgressListener;
    }

    public void removeOpenningProgressListener() {
        this.openningProgressListener = null;
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

    /**
     * Returns percent of the currently visible part of the drawer
     *
     * @return value in rage [0, 1]
     */
    public float getPercentOpen() {
        int contentWidth = getContentWidth();
        return (contentWidth + getTransX()) / contentWidth;
    }

    public int getContentWidth() {
        ViewGroup.MarginLayoutParams vlp = (MarginLayoutParams) contentView.getLayoutParams();
        return contentView.getWidth() + vlp.leftMargin + vlp.rightMargin + shadowView.getWidth();
    }

    public Rect getHandleRect() {
        int left = ivHandle.getLeft() + (int) getTransX();
        int top = ivHandle.getTop() + (int) getTransY();
        int right = ivHandle.getRight() + (int) getTransX();
        int bottom = ivHandle.getBottom() + (int) getTransY();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    public void animTranslation(float fromX, float toX, long durationMs) {
        animTranslation(fromX, toX, durationMs, null, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, AnimationListener listener) {
        animTranslation(fromX, toX, durationMs, listener, null);
    }

    public void animTranslation(float fromX, float toX, long durationMs, final AnimationListener listener, Interpolator interpolator) {
        // Log.d("animTranslation", fromX + " " + toX);
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

    public boolean isMenuVisible() {
        boolean val = Math.abs(getTransX()) < getContentWidth();
        // Log.d("isMenuVisible", String.valueOf(val) + " trans " + getTransX() + " content width: "
        // + getContentWidth());
        return val;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // initialize the position of the drawer to be outside visible part of the screen.
        setTransX(-getContentWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (translateAnimation != null) {
            translateAnimation.getTransformation(currentTime, transformation);
            transformation.getMatrix().getValues(matrixValues);
            translationMatrix.setValues(matrixValues);
            if (DEBUG_LOG) {
                Log.v("onDraw", translationMatrix.toString());
            }
            invalidate();
        }
        canvas.concat(translationMatrix);
        // canvas.drawPaint(paint);
        super.onDraw(canvas);
        if (openningProgressListener != null) {
            handler.post(progressReporter);
        }
    }

    public void showMenuContent() {
        contentView.setVisibility(View.VISIBLE);
    }

    public void hideMenuContent() {
        contentView.setVisibility(View.INVISIBLE);
    }

}
