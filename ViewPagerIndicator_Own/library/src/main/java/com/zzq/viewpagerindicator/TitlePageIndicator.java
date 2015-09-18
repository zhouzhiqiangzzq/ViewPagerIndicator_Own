/*
 * Copyright (C) 2012 Jake Wharton
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
package com.zzq.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;

/**
 * Draws a line for each page. The current page line is colored differently than
 * the unselected page lines.
 */
public class TitlePageIndicator extends View implements PageIndicator {
    private static final int INVALID_POINTER = -1;

    private final Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mBounds = new Rect();
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mSelectedColor;
    private int mLineColor = Color.RED;
    private int mTextSize;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mScrollState;
    private int mCurrentPage;
    private float mPositionOffset;

    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;

    private int mTextToTop = 0;// 暂时没用
    private int mIndicatorToText = 10;
    private int mIndicatorLineHeight = 15;
    private int mBaseIndicatorLineHeight = 5;
    private int mHorizontalSpace = 50;

    public TitlePageIndicator(Context context) {
        this(context, null);
    }

    public TitlePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TitlePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode())
            return;
        // Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitlePageIndicator, defStyle, 0);
        mSelectedColor = a.getColor(R.styleable.TitlePageIndicator_indicator_selected_color, -1);
        mLineColor = a.getColor(R.styleable.TitlePageIndicator_indicator_line_color, -1);
        mTextSize = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_text_size, 80f);
        mPaintText.setTextSize(mTextSize);

        mTextToTop = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_text_to_top, 0);
        mIndicatorToText = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_to_text, 10);
        mIndicatorLineHeight = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_line_height, 15);
        mBaseIndicatorLineHeight = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_base_indicator_line_height,
                5);
        mHorizontalSpace = (int) a.getDimension(R.styleable.TitlePageIndicator_indicator_horizontal_space, 50);

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure our width in whatever mode specified
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

        // Determine our height
        float height;
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            // Calculate the text bounds
            // 字的高度
            mBounds.setEmpty();
            mBounds.bottom = (int) (mPaintText.descent() - mPaintText.ascent());
            height = mBounds.bottom - mBounds.top + mIndicatorLineHeight + mBaseIndicatorLineHeight + mIndicatorToText;
        }
        final int measuredHeight = (int) height;

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private ArrayList<Rect> calculateAllBounds(Paint paint) {
        ArrayList<Rect> list = new ArrayList<Rect>();
        // For each views (If no values then add a fake one)
        final int count = mViewPager.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            Rect bounds = calcBounds(i, paint);
            list.add(bounds);
        }

        return list;
    }

    private Rect calcBounds(int index, Paint paint) {
        // Calculate the text bounds
        Rect bounds = new Rect();
        CharSequence title = getTitle(index);
        bounds.right = (int) paint.measureText(title, 0, title.length());
        bounds.bottom = (int) (paint.descent() - paint.ascent());
        return bounds;
    }

    private CharSequence getTitle(int i) {
        CharSequence title = mViewPager.getAdapter().getPageTitle(i);
        if (title == null) {
            title = "空的";
        }
        return title;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        ArrayList<Rect> bounds = calculateAllBounds(mPaintText);

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }
        final int paddingLeft = getPaddingLeft();
        final float bottom = getHeight() - getPaddingBottom();

        // 先画字
        mPaintText.setColor(mSelectedColor);
        int textWidthCount = 0;
        int textheightCount = 0;
        for (int i = 0; i < bounds.size(); i++) {
            int textWidth = bounds.get(i).right - bounds.get(i).left;
            int textheight = bounds.get(i).bottom - bounds.get(i).top;
            textWidthCount += textWidth;
            textheightCount += textheight;
        }

        int textSpace = (getWidth() - getPaddingLeft() - getPaddingRight() - textWidthCount) / (bounds.size() + 1);
        for (int i = 0; i < bounds.size(); i++) {
            int textWidth = bounds.get(i).right - bounds.get(i).left;
            int textheight = bounds.get(i).bottom - bounds.get(i).top;

            canvas.drawText(getTitle(i).toString(), textSpace + (textSpace + textWidth) * i, textheight, mPaintText);
        }
        // 底部的
        mPaint.setColor(mLineColor);
        canvas.drawRect(getPaddingLeft(), bottom - mBaseIndicatorLineHeight, getWidth() - getPaddingRight(), bottom, mPaint);
        // 画指示器
        int lineWidth = (getWidth() - getPaddingLeft() - getPaddingRight() - (bounds.size() * 2) * mHorizontalSpace)
                / (bounds.size());
        mPaint.setColor(mSelectedColor);
        final float left = paddingLeft + mHorizontalSpace + (lineWidth + mHorizontalSpace) * (mCurrentPage + mPositionOffset);
        final float right = left + (lineWidth + mHorizontalSpace);
        canvas.drawRect(left, bottom - mIndicatorLineHeight, right, bottom, mPaint);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0)) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x;
                    if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
                        mViewPager.fakeDragBy(deltaX);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging) {
                    final int count = mViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;

                    if ((mCurrentPage > 0) && (ev.getX() < halfWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    } else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage + 1);
                        }
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mViewPager.isFakeDragging())
                    mViewPager.endFakeDrag();
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager) {
            return;
        }
        if (mViewPager != null) {
            // Clear us from the old pager.
            mViewPager.setOnPageChangeListener(null);
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;

        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mPositionOffset = positionOffset;
        invalidate();

        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mPositionOffset = 0;
            invalidate();
        }
        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
