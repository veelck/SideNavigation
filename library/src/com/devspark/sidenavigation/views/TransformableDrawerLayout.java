/**
 *
 */
package com.devspark.sidenavigation.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
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

    /**
     * Interface for animations applied with
     * {@link TransformableDrawerLayout#animTranslation(float, float, long, AnimationListener)} etc.
     */
    public interface AnimationListener {
        public void onAnimationStart();

        public void onAnimationStop();
    }

    /**
     * Interface allowing the listener to get information about how much of menu is actually
     * visible.
     */
    public interface OpenningProgressListener {
        public void onProgress(float progress);
    }

    public static final boolean DEBUG_LOG = SideNavigationView.DEBUG_LOG;

    protected Handler handler = new Handler();

    private Matrix translationMatrix = new Matrix();;
    private float[] matrixValues = new float[9];

    private OpenningProgressListener openningProgressListener;

    private TranslateAnimation translateAnimation;
    private Transformation transformation = new Transformation();

    private View contentView;
    private View shadowView;
    private ImageView ivHandle;

    private Runnable progressReporter = new Runnable() {
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
    }

    public TransformableDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }


    /**
     * Register a callback to get information about progress of the menu being opened.
     *
     * @param openningProgressListener the callback that will run
     */
    public void setOpenningProgressListener(OpenningProgressListener openningProgressListener) {
        this.openningProgressListener = openningProgressListener;
    }

    /**
     * Unregisters the callback of getting the progress information.
     */
    public void removeOpenningProgressListener() {
        this.openningProgressListener = null;
    }

    /**
     * Translate the drawer layout by given values. <br>
     * Translation is applied using {@link Matrix#preTranslate(float, float)} method.
     *
     * @param dx difference of the x coordinate, that will be applied with the transformation
     * @param dy difference of the y coordinate, that will be applied with the transformation
     */
    public void moveBy(float dx, float dy) {
        translationMatrix.preTranslate(dx, dy);
        invalidate();
    }

    /**
     * Sets the transformation of the view to the value passed as the parameter. Old value will be
     * dropped and not taken into account.
     *
     * @param transX translation of the x coordinate for the view.
     */
    public void setTransX(float transX) {
        translationMatrix.getValues(matrixValues);
        matrixValues[Matrix.MTRANS_X] = transX;
        translationMatrix.setValues(matrixValues);
        invalidate();
    }

    /**
     * Sets the transformation of the view to the value passed as the parameter. Previous value will
     * be dropped.
     *
     * @param transY translation of the y coordinate for the view.
     */
    public void setTransY(float transY) {
        translationMatrix.getValues(matrixValues);
        matrixValues[Matrix.MTRANS_Y] = transY;
        translationMatrix.setValues(matrixValues);
        invalidate();
    }

    /**
     * Returns current view translation in X-axis.
     *
     * @return translation in X-axis in pixels
     */
    public float getTransX() {
        translationMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_X];
    }

    /**
     * Returns current view translation in Y-axis.
     *
     * @return translation in Y-axis in pixels
     */
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

    /**
     * Returns width of the content of drawer with margins + shadow size.
     *
     * @return drawer content width in pixels
     */
    public int getContentWidth() {
        ViewGroup.MarginLayoutParams vlp = (MarginLayoutParams) contentView.getLayoutParams();
        return contentView.getWidth() + vlp.leftMargin + vlp.rightMargin + shadowView.getWidth();
    }

    /**
     * Calculates and returns {@link Rect} describing size and position of the handle of the drawer.
     * It takes into account current transformation applied to the drawer.
     *
     * @return Handle's bounding rect.
     */
    public Rect getHandleRect() {
        int left = ivHandle.getLeft() + (int) getTransX();
        int top = ivHandle.getTop() + (int) getTransY();
        int right = ivHandle.getRight() + (int) getTransX();
        int bottom = ivHandle.getBottom() + (int) getTransY();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    /**
     * Convenience method for applying transformation animation for X-axis for the drawer.<br>
     * It is useful for applying fling or open animations.
     *
     * @param fromX start point for the animation in X-axis
     * @param toX end point for the animation in X-axis
     * @param durationMs animation duration in milliseconds
     */
    public void animTranslation(float fromX, float toX, long durationMs) {
        animTranslation(fromX, toX, durationMs, null, null);
    }

    /**
     * Convenience method for applying transformation animation for X-axis for the drawer.<br>
     * It is useful for applying fling or open animations.
     *
     * @param fromX start point for the animation in X-axis
     * @param toX end point for the animation in X-axis
     * @param durationMs animation duration in milliseconds
     * @param listener animation listener, that will get updates about state of the animation.
     */
    public void animTranslation(float fromX, float toX, long durationMs, AnimationListener listener) {
        animTranslation(fromX, toX, durationMs, listener, null);
    }

    /**
     * Convenience method for applying transformation animation for X-axis for the drawer.<br>
     * It is useful for applying fling or open animations.
     *
     * @param fromX start point for the animation in X-axis
     * @param toX end point for the animation in X-axis
     * @param durationMs animation duration in milliseconds
     * @param listener animation listener, that will get updates about state of the animation.
     * @param interpolator {@link Interpolator} object, that should be used during the animation.
     */
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

    /**
     * Indicates if the menu is currently visible.
     *
     * @return true if the menu is opened (or openning), false if it's closed.
     */
    public boolean isDrawerVisible() {
        boolean val = Math.abs(getTransX()) < getContentWidth();
        return val;
    }

    /**
     * Shows the content of the menu.
     *
     * @see #hideMenuContent()
     */
    public void showMenuContent() {
        contentView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides content of the menu. Necessary when menu is closed, so all the buttons etc. are not
     * possible to be clicked.
     *
     * @see #showMenuContent()
     */
    public void hideMenuContent() {
        contentView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        contentView = findViewById(R.id.side_navigation_content);
        shadowView = findViewById(R.id.shadow);
        ivHandle = (ImageView) findViewById(R.id.side_navigation_handle);
        hideMenuContent();
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
}
