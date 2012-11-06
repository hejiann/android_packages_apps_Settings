/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import com.android.settings.R;
import com.android.settings.cyanogenmod.PowerWidgetOrderActivity.ViewHolder;

public class TouchInterceptor extends ListView {

    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private int mDragPos;      // which item is being dragged
    private int mFirstDragPos; // where was the dragged item originally
    private int mDragPoint;    // at what offset inside the item did the user grab it
    private int mCoordOffset;  // the difference between screen coordinates and coordinates in this view
    private DragListener mDragListener;
    private DropListener mDropListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    private Rect mTempRect = new Rect();
    private Bitmap mDragBitmap;
    private final int mTouchSlop;
    private int mItemHeightNormal;
    private int mItemHeightExpanded;
    private int mItemHeightHalf;

    private int oldDrag = 0;
    
    public TouchInterceptor(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Resources res = getResources();
        mItemHeightNormal = res.getDimensionPixelSize(R.dimen.normal_height);
        mItemHeightHalf = mItemHeightNormal / 2;
        mItemHeightExpanded = res.getDimensionPixelSize(R.dimen.expanded_height);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragListener != null || mDropListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    int itemnum = pointToPosition(x, y);
                    if (itemnum == AdapterView.INVALID_POSITION) {
                        break;
                    }
                    ViewGroup item = (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());
                    mDragPoint = y - item.getTop();
                    mCoordOffset = ((int)ev.getRawY()) - y;
                    View dragger = item.findViewById(R.id.grabber);
                    Rect r = mTempRect;
                    if(dragger != null) {
	                    dragger.getDrawingRect(r);
	                    // The dragger icon itself is quite small, so pretend the touch area is bigger
	                    if (x < dragger.getRight() + 20) {
	                        item.setDrawingCacheEnabled(true);
	                        // Create a copy of the drawing cache so that it does not get recycled
	                        // by the framework when the list tries to clean up memory
	                        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
	                        startDragging(bitmap, y);
	                        mDragPos = itemnum;
	                        mFirstDragPos = mDragPos;
	                        oldDrag = mDragPos;
	                        mHeight = getHeight();
	                        int touchSlop = mTouchSlop;
	                        mUpperBound = Math.min(y - touchSlop, mHeight / 3);
	                        mLowerBound = Math.max(y + touchSlop, mHeight * 2 /3);
	                        return false;
	                    }
                    }
                    stopDragging();
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * (float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
     * @param paramInt1
     * @param paramInt2
     * @param paramInt3
     * @param paramInt4
     * @return
     */
    private Animation createAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta)
    {
      TranslateAnimation localTranslateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
      localTranslateAnimation.setDuration(150L);
      localTranslateAnimation.setFillAfter(true);
      return localTranslateAnimation;
    }
    
    private int getItemForPosition(int y) {
        int adjustedy = y - mDragPoint - mItemHeightHalf;
        
        int pos = myPointToPosition(0, y);
        return pos;
    }
    
    /*
     * pointToPosition() doesn't consider invisible views, but we
     * need to, so implement a slightly different version.
     */
    private int myPointToPosition(int x, int y) {
        if (y < 0) {
            int pos = myPointToPosition(x, y + mItemHeightNormal);
            return pos;
        }
        Rect frame = mTempRect;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }

    private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }
    
    /*
     * Restore size and visibility for all listitems
     */
    private void unExpandViews(boolean deletion) {
        isFrist = true;
    	ViewHolder holer;
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                    // end hack
                }
                layoutChildren(); // force children to be recreated where needed
                v = getChildAt(i);
                if (v == null) {
                    break;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            holer = (ViewHolder)v.getTag();
            if(holer != null){
	            if(holer.type == 1){
	            	params.height = 48;
	            }else{
	            	params.height = mItemHeightNormal;
	            }
            }
            v.setLayoutParams(params);
            v.setVisibility(View.VISIBLE);
        }
    }

    /* Adjust visibility and size to make it appear as though
     * an item is being dragged around and other items are making
     * room for it:
     * If dropping the item would result in it still being in the
     * same place, then make the dragged listitem's size normal,
     * but make the item invisible.
     * Otherwise, if the dragged listitem is still on screen, make
     * it as small as possible and expand the item below the insert
     * point.
     * If the dragged item is not on screen, only expand the item
     * below the current insertpoint.
     */
    boolean isFrist = true;
    private void doExpansion() {
    	ViewHolder holer;
    	int childnum = mDragPos - getFirstVisiblePosition();
        
        View first = getChildAt(mFirstDragPos - getFirstVisiblePosition());
        
        if(oldDrag < mDragPos){//down drag
            int startChildDrag = oldDrag - getFirstVisiblePosition();
            for(int i = startChildDrag+1 ; i <= childnum ; i++){
                View vv = null;
                Animation aa = null;
                if(mDragPos <= mFirstDragPos){
                    vv = getChildAt(i-1);
                    aa = createAnimation(0, 0, mItemHeightNormal, 0);
                }else{
                    vv = getChildAt(i);
                    aa = createAnimation(0, 0, 0, -mItemHeightNormal);
                }
                if(vv.equals(first)){
                    if(first!=null){
                        vv.setVisibility(View.INVISIBLE); 
                        vv.clearAnimation();
                    }
                }else{
                    vv.startAnimation(aa);
                }
                ViewHolder holder = (ViewHolder)vv.getTag();
                if(holder!=null && holder.type == 1){
                    if(mDragPos > mFirstDragPos){
                        if(getChildAt(i + 1)!=null && (getChildAt(i+1).getTag()!=null&&((ViewHolder)getChildAt(i+1).getTag()).type!=1)){
//                            getChildAt(i + 1).setBackgroundResource(R.drawable.bg);
                        }
                    }else if(mDragPos < mFirstDragPos){
                        if(getChildAt(i - 1)!=null && (getChildAt(i-1).getTag()!=null&&((ViewHolder)getChildAt(i-1).getTag()).type!=1)){
//                            getChildAt(i - 1).setBackgroundResource(R.drawable.bg2);
                        }
                    }
                }
            }
            oldDrag = mDragPos;
        }
        
        if(oldDrag > mDragPos){//up drag
            int startChildDrag = oldDrag - getFirstVisiblePosition();
            for(int i = startChildDrag - 1 ; i >= childnum ; i--){
                View vv = null;
                Animation aa = null;
                if(mDragPos >= mFirstDragPos){
                    vv = getChildAt(i+1);
                    aa = createAnimation(0, 0, -mItemHeightNormal, 0);
                }else{
                    vv = getChildAt(i);
                    aa = createAnimation(0, 0, 0, mItemHeightNormal);
                }
                if(vv.equals(first)){
                    if(first!=null){
                        vv.setVisibility(View.INVISIBLE); 
                        vv.clearAnimation();
                    }
                }else{
                    vv.startAnimation(aa);
                }
                ViewHolder holder = (ViewHolder)vv.getTag();
                if(holder!=null && holder.type == 1){
                    if(mDragPos > mFirstDragPos){
                        if(getChildAt(i + 1)!=null && (getChildAt(i+1).getTag()!=null&&((ViewHolder)getChildAt(i+1).getTag()).type!=1)){
//                            getChildAt(i + 1).setBackgroundResource(R.drawable.bg);
                        }
                    }else if(mDragPos < mFirstDragPos){
                        if(getChildAt(i - 1)!=null && (getChildAt(i-1).getTag()!=null&&((ViewHolder)getChildAt(i-1).getTag()).type!=1)){
//                            getChildAt(i - 1).setBackgroundResource(R.drawable.bg2);
                        }
                    }
                }
            }
            oldDrag = mDragPos;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((mDragListener != null || mDropListener != null) && mDragView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Rect r = mTempRect;
                    mDragView.getDrawingRect(r);
                    stopDragging();
                    if (mDropListener != null && mDragPos >= 0 && mDragPos < getCount()) {
                        mDropListener.drop(mFirstDragPos, mDragPos);
                    }
                    unExpandViews(false);
                    break;

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
                    
                    int itemnum = getItemForPosition(y);
                    if (itemnum >= 0) {
                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            if (mDragListener != null) {
                                mDragListener.drag(mDragPos, itemnum);
                            }
                            mDragPos = itemnum;
                            doExpansion();
                            View fristView =  getChildAt(mFirstDragPos - getFirstVisiblePosition());
                            if(fristView!=null){
                                fristView.setVisibility(View.INVISIBLE);
                            }
                        }
                        int speed = 0;
                        adjustScrollBounds(y);
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = y < mUpperBound / 2 ? -16 : -4;
                        }
                        if (speed != 0) {
                            int ref = pointToPosition(0, mHeight / 2);
                            if (ref == AdapterView.INVALID_POSITION) {
                                //we hit a divider or an invisible view, check somewhere else
                                ref = pointToPosition(0, mHeight / 2 + getDividerHeight() + 64);
                            }
                            View v = getChildAt(ref - getFirstVisiblePosition());
                            if (v!= null) {
                                int pos = v.getTop();
                                setSelectionFromTop(ref, pos - speed);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }
    
    private void startDragging(Bitmap bm, int y) {
        stopDragging();
        
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPoint + mCoordOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        Context context = getContext();
        ImageView v = new ImageView(context);
        int backGroundColor = context.getResources().getColor(R.color.dragndrop_background);
        v.setBackgroundColor(backGroundColor);
        v.setImageBitmap(bm);
        mDragBitmap = bm;

        mWindowManager = (WindowManager)context.getSystemService("window");
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    private void dragView(int x, int y) {
        mWindowParams.y = y - mDragPoint + mCoordOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
    }

    private void stopDragging() {
        if (mDragView != null) {
            final WindowManager wm = (WindowManager)getContext().getSystemService("window");
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
                mDragBitmap = null;
            }
        }
    }

    public void setDragListener(DragListener l) {
        mDragListener = l;
    }

    public void setDropListener(DropListener l) {
        mDropListener = l;
    }

    public interface DragListener {
        void drag(int from, int to);
    }

    public interface DropListener {
        void drop(int from, int to);
    }
}
