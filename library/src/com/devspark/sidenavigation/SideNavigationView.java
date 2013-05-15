package com.devspark.sidenavigation;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devspark.sidenavigation.views.DraggableLinearLayout;
import com.devspark.sidenavigation.views.DraggableLinearLayout.AnimationListener;
import com.devspark.sidenavigation.views.DraggableLinearLayout.OpenningProgressListener;
import com.nineoldandroids.view.ViewHelper;

/**
 * View of displaying side navigation.
 *
 * @author e.shishkin
 *
 */
public class SideNavigationView extends LinearLayout {
    public static final boolean DEBUG_LOG = true;

    private static final int MAX_HIDE_ANIMATION_TIME = 200;

    private static final int MAX_SHOW_ANIMATION_TIME = 500;

    private static final String LOG_TAG = SideNavigationView.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;

    private static final float MIN_VELOCITY = 0.8f;

    private int activeXDiff = 30;

    private DraggableLinearLayout navigationMenu;
    private LinearLayout menuContent;
    private View menuContentView;
    private View outsideView;
    private ImageView ivHandle;

    private Mode mMode = Mode.LEFT;

    VelocityTracker velocityTracker;

    private float velocityX;

    float mLastTouchX = 0f;
    float mLastTouchY = 0f;

    int mActivePointerId = 0;

    float mPosX = 0f;
    float mPosY = 0f;

    boolean isDragging = false;

    Paint paint = new Paint();

    private OpenningProgressListener openningListener = new OpenningProgressListener() {

        @Override
        public void onProgress(float progress) {
            if (outsideView != null && outsideView.isShown()) {
                ViewHelper.setAlpha(outsideView, progress);
            }
        }
    };

    public static enum Mode {
        LEFT, RIGHT
    };

    /**
     * Constructor of {@link SideNavigationView}.
     *
     * @param context
     */
    public SideNavigationView(Context context) {
        super(context);
        load();
    }

    /**
     * Constructor of {@link SideNavigationView}.
     *
     * @param context
     * @param attrs
     */
    public SideNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        load();
    }

    /**
     * Loading of side navigation view.
     */
    private void load() {
        if (isInEditMode()) {
            return;
        }
        initView();
        paint.setColor(0x0f0f0000);
        activeXDiff = getResources().getDimensionPixelOffset(R.dimen.side_navigation_active_margin);
    }

    /**
     * Initialization layout of side menu.
     */
    private void initView() {
        removeAllViews();
        int sideNavigationRes;
        switch (mMode) {
            case LEFT:
                sideNavigationRes = R.layout.side_navigation_left;
                break;
            case RIGHT:
                sideNavigationRes = R.layout.side_navigation_right;
                break;

            default:
                sideNavigationRes = R.layout.side_navigation_left;
                break;
        }
        LayoutInflater.from(getContext()).inflate(sideNavigationRes, this, true);
        navigationMenu = (DraggableLinearLayout) findViewById(R.id.side_navigation_menu);
        // navigationMenu.setEnabled(false);
        menuContent = (LinearLayout) findViewById(R.id.side_navigation_content);
        ivHandle = (ImageView) findViewById(R.id.side_navigation_handle);
        ivHandle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
                if (DEBUG_LOG) {
                    Log.d(LOG_TAG, "handle clicked");
                }
            }
        });
        outsideView = findViewById(R.id.side_navigation_outside_view);
    }

    /**
     * Setup sliding mode of side menu ({@code Mode.LEFT} or {@code Mode.RIGHT}). {@code Mode.LEFT} by default.
     *
     * @param mode Sliding mode
     */
    public void setMode(Mode mode) {
        if (isShown()) {
            hideMenu();
        }
        mMode = mode;
        initView();
        // setup menu items
    }

    /**
     * Getting current side menu mode ({@code Mode.LEFT} or {@code Mode.RIGHT}). {@code Mode.LEFT} by default.
     *
     * @return side menu mode
     */
    public Mode getMode() {
        return mMode;
    }

    /**
	 *
	 */
    @Override
    public void setBackgroundResource(int resource) {
        menuContent.setBackgroundResource(resource);
    }

    /**
     * Sets content of the menu view
     *
     * @param resId layout ID for the view to be used as menu content.
     */
    public void setContentView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (inflater != null) {
            menuContentView = inflater.inflate(resId, null);
            menuContent.addView(menuContentView);
        } else {
            throw new IllegalStateException("Unable to get valid inflater!");
        }
    }

    public void setContentView(View contentView) {
        if (contentView != null) {
            menuContent.addView(contentView);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        if (navigationMenu != null) {
            navigationMenu.setOpenningProgressListener(openningListener);
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (navigationMenu != null) {
            navigationMenu.removeOpenningProgressListener();
        }
        super.onDetachedFromWindow();
    }

    /**
     * Show side navigation menu.
     */
    public void showMenu() {
        setDrawerVisible();
        velocityX = 1f;
        showMenuWithVelocity();
        if (DEBUG_LOG) {
            Log.d(LOG_TAG, "showMenu()");
        }
    }

    /**
     * Hide side navigation menu.
     */
    public void hideMenu() {
        if (isDragging) {
            return;
        }
        velocityX = 1f;
        hideMenuWithVelocity();
        if (DEBUG_LOG) {
            Log.d(LOG_TAG, "hideMenu()");
        }
    }

    /**
     * Show/Hide side navigation menu depending on visibility.
     */
    public void toggleMenu() {
        if (isShown()) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            boolean retVal = false;
            final int action = MotionEventCompat.getActionMasked(ev);
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    float navMenuRight = navigationMenu.getContentWidth() + navigationMenu.getTransX();
                    if (navigationMenu.getHandleRect().contains((int) x, (int) y)) {
                        retVal = true;
                    } else if (Math.abs(x) < activeXDiff || (isShown() && x > navMenuRight - activeXDiff)) {
                        retVal = true;
                    }
                    break;
                }
            }
            if (!retVal) {
                if (isShown() || isDragging) {
                    retVal = true;
                }
            }

            if (retVal) {
                retVal = super.dispatchTouchEvent(ev);
            }
            if (DEBUG_LOG) {
                Log.v(LOG_TAG, "dispatchTouchEvent " + String.valueOf(retVal));
            }
            return retVal;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean retVal = false;
        final int action = MotionEventCompat.getActionMasked(ev);
        // Log.d("onintercept", "action " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);


                float navMenuRight = navigationMenu.getContentWidth() + navigationMenu.getTransX();
                if (DEBUG_LOG) {
                    Log.v("onInterceptTouchEvent", "navMenuRight: " + navMenuRight + " x: " + x + " isShown " + isShown());
                }
                if (navigationMenu.getHandleRect().contains((int) x, (int) y)) {
                    if (DEBUG_LOG) {
                        Log.v("onintercept", "handle clicked");
                    }
                    ivHandle.performClick();
                    retVal = true;
                } else if (Math.abs(x) < activeXDiff || (isShown() && x > navMenuRight - activeXDiff)) {
                    retVal = true;
                }
                break;
            }
        }
        if (DEBUG_LOG) {
            Log.d(LOG_TAG, "onIntercept returns " + String.valueOf(retVal));
        }
        return retVal;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean retValue = false;
        final int action = MotionEventCompat.getActionMasked(ev);
        if (DEBUG_LOG) {
            Log.v("onTouch", "Action: " + action);
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // TODO: add handling of right-type menu
                if (DEBUG_LOG) {
                    Log.v("SideNavView", "down: " + x);
                }

                float navMenuRight = navigationMenu.getContentWidth() + navigationMenu.getTransX();
                if (Math.abs(x) < activeXDiff || (isShown() && x > navMenuRight - activeXDiff)) {
                    isDragging = true;
                    // Remember where we started (for dragging)
                    mLastTouchX = x;
                    mLastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(ev);
                    retValue = true;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isDragging) {
                    velocityTracker.addMovement(ev);
                    // Find the index of the active pointer and fetch its position
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    // Calculate the distance moved
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    if (isShown()) {
                        updateLayout();
                    } else {

                        setDrawerVisible();
                    }

                    // Remember this touch position for the next move event
                    mLastTouchX = x;
                    mLastTouchY = y;

                    retValue = true;
                }

                break;
            }

            case MotionEvent.ACTION_UP: {
                if (isDragging) {
                    velocityTracker.addMovement(ev);
                    mActivePointerId = INVALID_POINTER_ID;
                    velocityTracker.computeCurrentVelocity(1);
                    velocityX = velocityTracker.getXVelocity();
                    if (Math.abs(velocityX) < MIN_VELOCITY) {
                        if (velocityX > 0) {
                            velocityX = MIN_VELOCITY;
                        } else {
                            velocityX = -MIN_VELOCITY;
                        }
                    }
                    velocityTracker.recycle();
                    velocityTracker = null;
                    if (velocityX < 0) {
                        hideMenuWithVelocity();
                        // Log.d("HIDE MENU", String.format("HIDE MENU v=%.2f dx=%f", velocityX,
                        // mPosX));
                    } else if (velocityX > 0) {
                        showMenuWithVelocity();
                        // Log.d("SHOW MENU", String.format("SHOW MENU v=%.2f dx=%f", velocityX,
                        // mPosX));
                    } else {
                        // Log.d("UNDEFINED", "velocity is zero");
                        hideMenuWithVelocity();
                    }
                    isDragging = false;
                    retValue = true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                isDragging = false;
                retValue = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                    retValue = true;
                }
                break;
            }
        }
        if (DEBUG_LOG) {
            Log.v("onTouchEvent", "Touch val " + String.valueOf(retValue) + " action: " + ev.getAction());
        }

        return retValue;
    }

    protected void showMenuWithVelocity() {
        float fromXDelta = navigationMenu.getTransX();
        float toXDelta = 0;
        long durationMillis = getAnimDurationFromVelocity(toXDelta - fromXDelta);
        if (durationMillis > MAX_SHOW_ANIMATION_TIME || durationMillis < 0) {
            durationMillis = MAX_SHOW_ANIMATION_TIME;
        }
        // Log.d("showMenuWithVelocity", String.format("dur: %dms fromX: %f toX: %f",
        // durationMillis, fromXDelta, toXDelta));
        navigationMenu.animTranslation(fromXDelta, toXDelta, durationMillis);
    }

    protected void hideMenuWithVelocity() {
        float fromXDelta = navigationMenu.getTransX();
        float toXDelta = -navigationMenu.getContentWidth();
        long durationMillis = getAnimDurationFromVelocity(toXDelta - fromXDelta);
        if (durationMillis > MAX_HIDE_ANIMATION_TIME || durationMillis < 0) {
            durationMillis = MAX_HIDE_ANIMATION_TIME;
        }
        // Log.d("hideMenuWithVelocity", String.format("dur: %dms fromX: %f toX: %f",
        // durationMillis, fromXDelta, toXDelta));
        navigationMenu.animTranslation(fromXDelta, toXDelta, durationMillis, new AnimationListener() {

            @Override
            public void onAnimationStop() {
                setDrawerInvisible();
            }

            @Override
            public void onAnimationStart() {}
        }, new LinearInterpolator());
    }

    protected void setDrawerVisible() {
        navigationMenu.setVisibility(View.VISIBLE);
        navigationMenu.showMenuContent();
        ViewHelper.setAlpha(outsideView, 0f);
        outsideView.setVisibility(View.VISIBLE);
        if (isDragging) {
            updateLayout();
        }
    }

    protected void setDrawerInvisible(){
        navigationMenu.hideMenuContent();
        outsideView.setVisibility(View.GONE);
        ViewHelper.setAlpha(outsideView, 0f);
    }

    private long getAnimDurationFromVelocity(float distance) {
        return (long) (Math.abs(distance) / velocityX);
    }

    @Override
    public boolean isShown() {
        return navigationMenu.isMenuVisible();
    }

    private void updateLayout() {
        switch (mMode) {
            case LEFT:
                if (navigationMenu.getTransX() + mPosX >= 0) {
                    if (mPosX > 0f) {
                        navigationMenu.setTransX(0f);
                    } else {
                        navigationMenu.moveBy(mPosX, 0);
                    }
                } else if (navigationMenu.getTransX() + mPosX < -navigationMenu.getContentWidth()) {
                    navigationMenu.setTransX(-navigationMenu.getContentWidth());
                } else {
                    navigationMenu.moveBy(mPosX, 0);
                }
                if (DEBUG_LOG) {
                    Log.v("translation", navigationMenu.getContentWidth() + " " + navigationMenu.getTransX());
                }
                mPosX = 0;
                // ViewHelper.setAlpha(outsideView, navigationMenu.getPercentOpen());

                break;
            case RIGHT:
                // TODO: implement for drawer on the right.
                break;
        }
    }
}
