package com.zzq.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;

public class TransformPageIndicator extends View implements PageIndicator {

    private ViewPager mViewPager;
    private OnPageChangeListener mListener;

    // 用来记录测量到的大小两个框 高度
    private int textHeightSmall = 0;
    private int textHeightBig = 0;
    // 当前被选中的
    private int mCurrentBeenSelectedPage = 0;
    // 记录临时的page == mViewPager.getCurrentItem();
    private int mCurrentPage = 0;
    private float mPageOffset = 0;
    // ViewPaer滑动状态
    private int mScrollState = ViewPager.SCROLL_STATE_IDLE;

    // 测量View用到的Bound
    private final Rect mBounds = new Rect();
    private final Paint mPaintBound = new Paint();
    private final Paint mPaintText = new Paint();
    // 滑动方向
    boolean isLeft = false;
    boolean isRight = false;

    // 导航所占比例
    private float leftPercentage = 3f;
    private float centerPercentage = 4f;
    private float rightPercentage = 3f;
    private float percentage = leftPercentage + centerPercentage + rightPercentage;
    // 大的字号
    private float textSizeBig = 40;
    // 小的字号
    private float textSizeSmall = 35;
    // 大的边距
    private float marginTextBig = 20;
    // 小的边距
    private float marginTextSmall = 15;
    // 字体颜色值
    private int textColor = Color.argb(255, 0, 0, 0);// 黑色
    private int textColorTransparent = Color.argb(0, 0, 0, 0);// 全透黑色
    // 背景颜色值
    private int bgColorTransparent = Color.parseColor("#008A9499");
    private int bgColorTranslucent = Color.parseColor("#3F8A9499");// 中间颜色
    private int bgColor = Color.parseColor("#FF676F73");//

    // Touch 相关
    // 是否启用touch事件
    private boolean isUseTouch = true;
    private boolean isUseDrag = false;

    private static final int INVALID_POINTER = -1;
    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;

    public TransformPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaintBound.setAntiAlias(true);
        mPaintText.setAntiAlias(true);
        // 从资源中读取参数
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Bazaar_viewPager);

        leftPercentage = typedArray.getFloat(R.styleable.Bazaar_viewPager_leftPercentage, 3);
        centerPercentage = typedArray.getFloat(R.styleable.Bazaar_viewPager_centerPercentage, 4);
        rightPercentage = typedArray.getFloat(R.styleable.Bazaar_viewPager_rightPercentage, 3);

        percentage = leftPercentage + centerPercentage + rightPercentage;

        textSizeBig = typedArray.getDimension(R.styleable.Bazaar_viewPager_textSizeBig, 40);
        textSizeSmall = typedArray.getDimension(R.styleable.Bazaar_viewPager_textSizeSmall, 35);

        marginTextBig = typedArray.getDimension(R.styleable.Bazaar_viewPager_marginTextBig, 20);
        marginTextSmall = typedArray.getDimension(R.styleable.Bazaar_viewPager_marginTextSmall, 15);

        textColor = typedArray.getColor(R.styleable.Bazaar_viewPager_textColor, Color.parseColor("#FF000000"));
        textColorTransparent = typedArray.getColor(R.styleable.Bazaar_viewPager_textColorTransparent,
                Color.parseColor("#00000000"));
        bgColorTransparent = typedArray.getColor(R.styleable.Bazaar_viewPager_bgColorTransparent, Color.parseColor("#008A9499"));
        bgColorTranslucent = typedArray.getColor(R.styleable.Bazaar_viewPager_bgColorTranslucent, Color.parseColor("#3F8A9499"));
        bgColor = typedArray.getColor(R.styleable.Bazaar_viewPager_bgColor, Color.parseColor("#FF676F73"));

        typedArray.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

    }

    public TransformPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TransformPageIndicator(Context context) {
        this(context, null);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE && mPageOffset == 0) {
            mCurrentBeenSelectedPage = mCurrentPage;
            isLeft = false;
            isRight = false;
        }
        invalidate();
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mCurrentPage < position) {
            isLeft = true; // ++
            isRight = false;
        } else if (mCurrentPage == position) {
            if (mCurrentBeenSelectedPage > position) {
                isLeft = false; // ++
                isRight = true;
            } else {
                if (positionOffset == 0) {
                    isLeft = false; // ++
                    isRight = false;
                } else {
                    isLeft = true; // ++
                    isRight = false;
                }
            }

        } else {// 向右滑动 --
            isLeft = false; // ++
            isRight = true;
        }
        if (position > mCurrentPage) {
            mCurrentBeenSelectedPage = position;
        } else if ((position < mCurrentPage)) {
            if ((mCurrentBeenSelectedPage - position) == 1) {
                if (positionOffset == 0) {
                    mCurrentBeenSelectedPage = position;
                }
            } else if ((mCurrentBeenSelectedPage - position) == 2) {
                mCurrentBeenSelectedPage = position + 1;
            }

        }
        mCurrentPage = position;
        mPageOffset = positionOffset;
        invalidate();
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mCurrentBeenSelectedPage = mCurrentPage;
        }
        invalidate();
        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
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
        mCurrentBeenSelectedPage = item;
        invalidate();
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        float height;
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            mBounds.setEmpty();
            mPaintText.setTextSize(textSizeBig);
            mBounds.bottom = (int) (mPaintText.descent() - mPaintText.ascent());
            height = mBounds.bottom - mBounds.top + getPaddingTop() + getPaddingBottom() + 2 * marginTextBig;
            textHeightBig = (int) height;

            mBounds.setEmpty();
            mPaintText.setTextSize(textSizeSmall);
            mBounds.bottom = (int) (mPaintText.descent() - mPaintText.ascent());
            textHeightSmall = mBounds.bottom - mBounds.top + getPaddingTop() + getPaddingBottom() + (int) (2 * marginTextSmall);
        }
        final int measuredHeight = (int) height;
        setMeasuredDimension(measuredWidth, measuredHeight);
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
        // 获取值
        int currentPage = mCurrentPage;
        int currentBeenSelectedPage = mCurrentBeenSelectedPage;
        float pageOffset = mPageOffset;
        boolean left = isLeft;
        boolean right = isRight;

        if (currentPage == -1 && mViewPager != null) {
            currentPage = mViewPager.getCurrentItem();
        }

        ArrayList<Rect> bounds = calculateAllBounds(mPaintText, currentBeenSelectedPage, pageOffset, left, right);
        final int boundsSize = bounds.size();

        if (currentPage >= boundsSize) {
            setCurrentItem(boundsSize - 1);
            return;
        }

        for (int i = 0; i < currentBeenSelectedPage; i++) {
            drawText(canvas, i, bounds, currentBeenSelectedPage, pageOffset, left, right);
        }
        for (int i = count - 1; i >= currentBeenSelectedPage; i--) {
            drawText(canvas, i, bounds, currentBeenSelectedPage, pageOffset, left, right);
        }
    }

    private void drawText(Canvas canvas, int index, ArrayList<Rect> bounds, int currentBeenSelectedPage, float pageOffset,
                          boolean left, boolean right) {
        float textSize = textSizeSmall;
        int tempBgColor = 0;
        int tempTextColor = 0;

        boolean isNeedDraw = false;

        if (index < currentBeenSelectedPage) {
            if (index == currentBeenSelectedPage - 1) {
                isNeedDraw = true;
                if (left) {
                    textSize = textSizeSmall;
                    tempBgColor = getColorByRatio(bgColorTranslucent, bgColorTransparent, mPageOffset);
                    tempTextColor = getColorByRatio(textColor, textColorTransparent, mPageOffset);
                } else if (right) {
                    textSize = textSizeSmall + (textSizeBig - textSizeSmall) * (1 - pageOffset);
                    tempBgColor = getColorByRatio(bgColorTranslucent, bgColor, (1 - pageOffset));
                    tempTextColor = textColor;
                } else {
                    textSize = textSizeSmall;
                    tempBgColor = bgColorTranslucent;
                    tempTextColor = textColor;
                }
            } else if (index == currentBeenSelectedPage - 2) {
                if (right) {
                    textSize = textSizeSmall;
                    tempBgColor = getColorByRatio(bgColorTransparent, bgColorTranslucent, (1 - pageOffset));
                    tempTextColor = getColorByRatio(textColorTransparent, textColor, (1 - pageOffset));
                    isNeedDraw = true;
                }
            }
        } else if (index == currentBeenSelectedPage) {
            isNeedDraw = true;
            if (left) {
                textSize = textSizeSmall + (textSizeBig - textSizeSmall) * (1 - pageOffset);
                tempBgColor = getColorByRatio(bgColor, bgColorTranslucent, pageOffset);
                tempTextColor = textColor;
            } else if (right) {
                textSize = textSizeSmall + (textSizeBig - textSizeSmall) * (pageOffset);
                tempBgColor = getColorByRatio(bgColor, bgColorTranslucent, (1 - pageOffset));
                tempTextColor = textColor;
            } else {
                textSize = textSizeBig;
                tempBgColor = bgColor;
                tempTextColor = textColor;
            }
        } else if (index > currentBeenSelectedPage) {
            if (index == currentBeenSelectedPage + 1) {
                isNeedDraw = true;
                if (left) {
                    textSize = textSizeSmall + (textSizeBig - textSizeSmall) * (pageOffset);
                    tempBgColor = getColorByRatio(bgColorTranslucent, bgColor, pageOffset);
                    tempTextColor = textColor;
                } else if (right) {
                    textSize = textSizeSmall;
                    tempBgColor = getColorByRatio(bgColorTranslucent, bgColorTransparent, (1 - pageOffset));
                    tempTextColor = getColorByRatio(textColor, textColorTransparent, (1 - pageOffset));
                } else {
                    textSize = textSizeSmall;
                    tempBgColor = bgColorTranslucent;
                    tempTextColor = textColor;
                }
            } else if (index == currentBeenSelectedPage + 2) {
                if (left) {
                    textSize = textSizeSmall;
                    tempBgColor = getColorByRatio(bgColorTransparent, bgColorTranslucent, pageOffset);
                    tempTextColor = getColorByRatio(textColorTransparent, textColor, pageOffset);
                    isNeedDraw = true;
                }
            }
        }

        if (isNeedDraw) {
            // 画矩形
            Rect targetRect = bounds.get(index);
            String testString = getTitle(index).toString();
            mPaintBound.setColor(tempBgColor);
            canvas.drawRect(targetRect, mPaintBound);
            // 画字
            mPaintText.setTextSize(textSize);
            mPaintText.setColor(tempTextColor);
            if (testString.equals("最新")) {
                mPaintText.setFakeBoldText(true);
            }
            FontMetricsInt fontMetrics = mPaintText.getFontMetricsInt();
            int baseline = targetRect.top + (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top) / 2
                    - fontMetrics.top;
            mPaintText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(testString, targetRect.centerX(), baseline, mPaintText);
        }
    }

    public CharSequence getTitle(int i) {
        CharSequence title = mViewPager.getAdapter().getPageTitle(i);
        if (title == null) {
            title = "";
        }
        return title;
    }

    /**
     * 获取背景区域和 字的区域
     */
    private ArrayList<Rect> calculateAllBounds(Paint paint, int currentBeenSelectedPage, float pageOffset, boolean left,
                                               boolean right) {
        ArrayList<Rect> list = new ArrayList<Rect>();
        final int count = mViewPager.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            Rect bounds = calcBounds(i, currentBeenSelectedPage, pageOffset, left, right);
            list.add(bounds);
        }
        return list;
    }

    /**
     * @param index
     * @param currentBeenSelectedPage
     * @param pageOffset
     * @param left
     * @param right
     * @return
     */

    private Rect calcBounds(int index, int currentBeenSelectedPage, float pageOffset, boolean left, boolean right) {
        Rect bounds = new Rect();
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int rectLeft = 0;
        int rectRight = 0;
        int rectTop = 0;
        int rectBottom = 0;

        int leftWidth = (int) (1000 * viewWidth * leftPercentage / (percentage * 1000));// 216
        int centerWidth = (int) (1000 * viewWidth * centerPercentage / (percentage * 1000));// 288
        int rightWidth = (int) (1000 * viewWidth * rightPercentage / (percentage * 1000));// 216

        int leftPercentLocation = (int) (1000 * viewWidth * leftPercentage / (percentage * 1000));// 216
        int centerPercentLocation = (int) (1000 * viewWidth * (leftPercentage + centerPercentage) / (percentage * 1000));// 504
        int rightPercentLocation = (int) (1000 * viewWidth * (leftPercentage + centerPercentage + rightPercentage) / (percentage * 1000));// 720

        if (index < currentBeenSelectedPage - 1) {
            rectTop = (textHeightBig - textHeightSmall);
            rectBottom = viewHeight;
            rectLeft = 0;
            rectRight = leftPercentLocation;
        } else if (index == currentBeenSelectedPage - 1) {
            if (left) {
                rectLeft = 0;
                rectRight = leftPercentLocation;
                rectTop = (textHeightBig - textHeightSmall);
                rectBottom = viewHeight;
            } else if (right) {
                rectLeft = (int) (leftWidth * (1 - pageOffset));// 0-216
                rectRight = (int) (leftPercentLocation + centerWidth * (1 - pageOffset));// 216-504
                rectTop = (int) ((textHeightBig - textHeightSmall) * (pageOffset));
                rectBottom = viewHeight;
            } else {
                rectLeft = 0;
                rectRight = leftPercentLocation;
                rectTop = (textHeightBig - textHeightSmall);
                rectBottom = viewHeight;
            }
        } else if (index == currentBeenSelectedPage) {
            if (left) {
                rectLeft = (int) (leftPercentLocation * (1 - pageOffset));// 216-0
                rectRight = (int) (leftPercentLocation + centerWidth * (1 - pageOffset));// 504-216
                rectTop = (int) ((textHeightBig - textHeightSmall) * (pageOffset));
                rectBottom = viewHeight;
            } else if (right) {
                rectLeft = (int) (leftPercentLocation + centerWidth * (1 - pageOffset));
                rectRight = (int) (centerPercentLocation + rightWidth * (1 - pageOffset));
                rectTop = (int) ((textHeightBig - textHeightSmall) * (1 - pageOffset));
                rectBottom = viewHeight;
            } else {
                rectLeft = leftPercentLocation;
                rectRight = centerPercentLocation;
                rectTop = 0;
                rectBottom = viewHeight;
            }
        } else if (index == currentBeenSelectedPage + 1) {
            if (left) {
                rectLeft = (int) (leftPercentLocation + centerWidth * (1 - pageOffset));// 504-216
                rectRight = (int) (centerPercentLocation + rightWidth * (1 - pageOffset));// 720-504
                rectTop = (int) ((textHeightBig - textHeightSmall) * (1 - pageOffset));
                rectBottom = viewHeight;
            } else if (right) {
                rectLeft = centerPercentLocation;
                rectRight = rightPercentLocation;
                rectTop = (textHeightBig - textHeightSmall);
                rectBottom = viewHeight;
            } else {
                rectLeft = centerPercentLocation;
                rectRight = rightPercentLocation;
                rectTop = (textHeightBig - textHeightSmall);
                rectBottom = viewHeight;
            }
        } else if (index > currentBeenSelectedPage + 1) {
            rectLeft = centerPercentLocation;
            rectRight = rightPercentLocation;
            rectTop = (textHeightBig - textHeightSmall);
            rectBottom = viewHeight;
        }

        bounds.top = rectTop;
        bounds.bottom = rectBottom;
        bounds.left = rectLeft;
        bounds.right = rectRight;

        return bounds;
    }

    /**
     * 根据偏移值来获取 颜色值
     *
     * @param beginColor
     * @param endColor
     * @param ratio
     * @return
     */

    private int getColorByRatio(int beginColor, int endColor, float ratio) {
        int beginRed = Color.red(beginColor);
        int beginGreen = Color.green(beginColor);
        int beginBlue = Color.blue(beginColor);
        int beginAlpha = Color.alpha(beginColor);

        int endRed = Color.red(endColor);
        int endGreen = Color.green(endColor);
        int endBlue = Color.blue(endColor);
        int endAlpha = Color.alpha(endColor);

        int alpha = (int) (endAlpha * ratio + beginAlpha * (1 - ratio));
        int red = (int) (endRed * ratio + beginRed * (1 - ratio));
        int green = (int) (endGreen * ratio + beginGreen * (1 - ratio));
        int blue = (int) (endBlue * ratio + beginBlue * (1 - ratio));

        return Color.argb(alpha, red, green, blue);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isUseTouch) {
            return super.onTouchEvent(ev);
        }
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
                    if (isUseDrag && (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag())) {
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
                    int leftPercentLocation = (int) (1000 * width * leftPercentage / (percentage * 1000));// 216
                    int centerPercentLocation = (int) (1000 * width * (leftPercentage + centerPercentage) / (percentage * 1000));// 504
                    final float eventX = ev.getX();

                    if (eventX < leftPercentLocation) {
                        if (mCurrentPage > 0) {
                            if (action != MotionEvent.ACTION_CANCEL) {
                                mViewPager.setCurrentItem(mCurrentPage - 1);
                            }
                            return true;
                        }
                    } else if (eventX > centerPercentLocation) {
                        if (mCurrentPage < count - 1) {
                            if (action != MotionEvent.ACTION_CANCEL) {
                                mViewPager.setCurrentItem(mCurrentPage + 1);
                            }
                            return true;
                        }
                    } else {
                        // Middle
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
}
