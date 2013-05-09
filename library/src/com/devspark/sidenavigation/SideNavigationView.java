package com.devspark.sidenavigation;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.devspark.sidenavigation.views.DraggableLinearLayout;

/**
 * View of displaying side navigation.
 *
 * @author e.shishkin
 *
 */
public class SideNavigationView extends LinearLayout {
    private static final String LOG_TAG = SideNavigationView.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;

    private int activeXDiff = 20;

    private DraggableLinearLayout navigationMenu;
    private LinearLayout menuContent;
    private View menuContentView;
    private View outsideView;

    private ISideNavigationCallback callback;
    private ArrayList<SideNavigationItem> menuItems;
    private Mode mMode = Mode.LEFT;

    VelocityTracker velocityTracker;

    private float velocityX;

    float mLastTouchX = 0f;
    float mLastTouchY = 0f;

    int mActivePointerId = 0;

    float mPosX = 0f;
    float mPosY = 0f;

    boolean isDragging = false;

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
        menuContent = (LinearLayout) findViewById(R.id.side_navigation_content);
        outsideView = findViewById(R.id.side_navigation_outside_view);
        outsideView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
            }
        });
        // menuContent.setOnItemClickListener(new OnItemClickListener() {
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // if (callback != null) {
        // callback.onSideNavigationItemClick(menuItems.get(position).getId());
        // }
        // hideMenu();
        // }
        // });
    }

    /**
     * Setup of {@link ISideNavigationCallback} for callback of item click.
     *
     * @param callback
     */
    public void setMenuClickCallback(ISideNavigationCallback callback) {
        this.callback = callback;
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

    /**
     * Show side navigation menu.
     */
    public void showMenu() {
        outsideView.setVisibility(View.VISIBLE);
        outsideView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.side_navigation_fade_in));
        // show navigation menu with animation
        int animRes;
        switch (mMode) {
            case LEFT:
                animRes = R.anim.side_navigation_in_from_left;
                break;
            case RIGHT:
                animRes = R.anim.side_navigation_in_from_right;
                break;

            default:
                animRes = R.anim.side_navigation_in_from_left;
                break;
        }
        navigationMenu.setVisibility(View.VISIBLE);
        navigationMenu.startAnimation(AnimationUtils.loadAnimation(getContext(), animRes));
    }

    /**
     * Hide side navigation menu.
     */
    public void hideMenu() {
        if (isDragging) {
            return;
        }
        outsideView.setVisibility(View.GONE);
        outsideView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.side_navigation_fade_out));
        // hide navigation menu with animation
        int animRes;
        switch (mMode) {
            case LEFT:
                animRes = R.anim.side_navigation_out_to_left;
                break;
            case RIGHT:
                animRes = R.anim.side_navigation_out_to_right;
                break;

            default:
                animRes = R.anim.side_navigation_out_to_left;
                break;
        }
        navigationMenu.setVisibility(View.GONE);
        navigationMenu.startAnimation(AnimationUtils.loadAnimation(getContext(), animRes));
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
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // TODO: add handling of right-type menu

                // Log.d("SideNavView", "down: " + x);
                if (Math.abs(x) < activeXDiff) {
                    isDragging = true;
                    // Remember where we started (for dragging)
                    mLastTouchX = x;
                    mLastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(ev);
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
                        // invalidate();
                    } else if (mPosX > activeXDiff) {

                        setDrawerVisible();
                    }

                    // Remember this touch position for the next move event
                    mLastTouchX = x;
                    mLastTouchY = y;
                }

                break;
            }

            case MotionEvent.ACTION_UP: {
                if (isDragging) {
                    velocityTracker.addMovement(ev);
                    mActivePointerId = INVALID_POINTER_ID;
                    if (ev.getPointerCount() == 1) {
                        velocityTracker.computeCurrentVelocity(1);
                        velocityX = velocityTracker.getXVelocity();
                        velocityTracker.recycle();
                        velocityTracker = null;
                        if (velocityX < 0) {
                            hideMenuWithVelocity();
                            Log.e("HIDE MENU", String.format("HIDE MENU v=%.2f dx=%.2f", velocityX, mPosX));
                        } else {
                            showMenuWithVelocity();
                            Log.e("SHOW MENU", String.format("SHOW MENU v=%.2f dx=%.2f", velocityX, mPosX));
                        }
                    }
                    isDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                isDragging = false;
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
                }
                break;
            }
        }
        return true;
    }

    protected void showMenuWithVelocity() {
        float fromXDelta = navigationMenu.getTransX();
        float toXDelta = 0;
        long durationMillis = getAnimDurationFromVelocity(fromXDelta);
        if (durationMillis > 1000 || durationMillis < 0) {
            durationMillis = 800;
        }
        Log.e("duration", durationMillis + "ms");
        navigationMenu.animTranslation(fromXDelta, toXDelta, durationMillis, null);
        //
        // TranslateAnimation anim = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
        // anim.setDuration(durationMillis);
        // anim.setInterpolator(new DecelerateInterpolator());
        // anim.setFillAfter(true);
        //
        // navigationMenu.startAnimation(anim);
    }

    protected void hideMenuWithVelocity() {

    }

    protected void setDrawerVisible() {
        if (!isShown()) {
            // navigationMenu.setTransX(-navigationMenu.getWidth());
            // RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
            // navigationMenu.getLayoutParams();
            // params.leftMargin = -params.width;
            // navigationMenu.setLayoutParams(params);
            // navigationMenu.moveBy(-params.width, 0);
//            params.leftMargin = -params.width;
//            menuContent.setLayoutParams(params);
        }
        navigationMenu.setVisibility(View.VISIBLE);
        outsideView.setVisibility(View.VISIBLE);
    }

    protected void setDrawerInvisible(){
        navigationMenu.setVisibility(View.GONE);
        outsideView.setVisibility(View.GONE);
    }

    private long getAnimDurationFromVelocity(float distance) {
        return (long) (Math.abs(distance) / velocityX);
    }

    @Override
    public boolean isShown() {
        return navigationMenu.isShown();
    }

    private void updateLayout() {
        switch (mMode) {
            case LEFT:

                if (navigationMenu.getTransX() >= 0) {
                    navigationMenu.setTransX(0f);
                } else if (navigationMenu.getTransX() < -navigationMenu.getWidth() - activeXDiff) {
                    Log.e("translation", navigationMenu.getWidth() + " " + navigationMenu.getTransX());
                    navigationMenu.setTransX(-navigationMenu.getWidth());
                    // setDrawerInvisible();
                } else {
                    navigationMenu.moveBy(mPosX, 0);
                }
                mPosX = 0;


//                TranslateAnimation anim = new TranslateAnimation(0, mPosX, 0, 0);
//                anim.setDuration(0);
//                anim.setFillAfter(true);
//                navigationMenu.startAnimation(anim);
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) menuContent.getLayoutParams();
//                params.leftMargin = (int) (params.leftMargin + mPosX);
//                if (params.leftMargin > 0) {
//                    params.leftMargin = 0;
//                } else if (params.leftMargin < -params.width + activeXDiff) {
//                    params.leftMargin = -params.width;
//                    setDrawerInvisible();
//                }
//                mPosX = 0f;
//                menuContent.setLayoutParams(params);


                break;
            case RIGHT:
                // TODO: implement for drawer on the right.
                break;
        }
    }
}
