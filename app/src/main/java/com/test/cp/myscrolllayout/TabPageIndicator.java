package com.test.cp.myscrolllayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabPageIndicator extends LinearLayout implements ViewPager.OnPageChangeListener, OnClickListener {

    //    private LinearLayout mTabContainer;
    private ViewPager mViewPager;

    private int mMode;
    private int mTabPadding;
    private int mTextAppearance;

    private int mIndicatorOffset;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorMode;

    private int mUnderLineHeight;

    private Paint mPaint;
    private Paint mUnderLinePaint;

    public static final int MODE_SCROLL = 0;
    public static final int MODE_FIXED = 1;

    private int mSelectedPosition;
    private boolean mScrolling = false;

    private Runnable mTabAnimSelector;

    private ViewPager.OnPageChangeListener mListener;

    private static final int MATCH_PARENT = -1;
    private static final int WRAP_CONTENT = -2;

    private DataSetObserver mObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }

    };

    public TabPageIndicator(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public TabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setHorizontalScrollBarEnabled(false);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);

        mUnderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderLinePaint.setStyle(Style.FILL);

//        mTabContainer = new LinearLayout(context);
//        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
//        mTabContainer.setGravity(Gravity.CENTER);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);

        if (isInEditMode())
            addTemporaryTab();
    }

    public void applyStyle(int resId) {
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPageIndicator, defStyleAttr, defStyleRes);
        int indicatorColor;
        int underLineColor;
        try {
            mTabPadding = a.getDimensionPixelSize(R.styleable.TabPageIndicator_tpi_tabPadding, 12);
            indicatorColor = a.getColor(R.styleable.TabPageIndicator_tpi_indicatorColor, Color.WHITE);
            mIndicatorMode = a.getInt(R.styleable.TabPageIndicator_tpi_indicatorMode, MATCH_PARENT); /* MATCH_PARENT = -1  &&  WRAP_CONTENT = -2 */
            mIndicatorHeight = a.getDimensionPixelSize(R.styleable.TabPageIndicator_tpi_indicatorHeight, 2);
            underLineColor = a.getColor(R.styleable.TabPageIndicator_tpi_underLineColor, Color.LTGRAY);
            mUnderLineHeight = a.getDimensionPixelSize(R.styleable.TabPageIndicator_tpi_underLineHeight, 1);
            mTextAppearance = a.getResourceId(R.styleable.TabPageIndicator_android_textAppearance, 0);
            mMode = a.getInteger(R.styleable.TabPageIndicator_tpi_mode, MODE_SCROLL);
        } finally {
            a.recycle();
        }
        removeAllViews();

//        if (mMode == MODE_SCROLL) {
//            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
////            addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        } else if (mMode == MODE_FIXED) {
//            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
////            addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        }

        mPaint.setColor(indicatorColor);
        mUnderLinePaint.setColor(underLineColor);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-post the selector we saved
        if (mTabAnimSelector != null)
            post(mTabAnimSelector);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);
    }

    private CheckedTextView getTabView(int position) {
//        return (CheckedTextView) mTabContainer.getChildAt(position);
        return (CheckedTextView) getChildAt(position);
    }

    private void animateToTab(final int position) {
        final CheckedTextView tv = getTabView(position);
        if (tv == null)
            return;

        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);

        mTabAnimSelector = new Runnable() {
            public void run() {
                if (!mScrolling)
                    switch (mIndicatorMode) {
                        case MATCH_PARENT:
                            updateIndicator(tv.getLeft(), tv.getWidth());
                            break;
                        case WRAP_CONTENT:
                            int textWidth = getTextWidth(tv);
                            updateIndicator(tv.getLeft() + tv.getWidth() / 2 - textWidth / 2, textWidth);
                            break;
                    }
//                scrollTo(tv.getLeft() - (getWidth() - tv.getWidth()) / 2 + getPaddingLeft(), 0);
                mTabAnimSelector = null;
            }
        };

        post(mTabAnimSelector);
    }

    /**
     * Set a listener will be called when the current page is changed.
     *
     * @param listener The {@link android.support.v4.view.ViewPager.OnPageChangeListener} will be called.
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    /**
     * Set the ViewPager associate with this indicator view.
     *
     * @param view The ViewPager view.
     */
    public void setViewPager(ViewPager view) {
        if (mViewPager == view)
            return;

        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
            PagerAdapter adapter = view.getAdapter();
            if (adapter != null)
                adapter.unregisterDataSetObserver(mObserver);
        }

        PagerAdapter adapter = view.getAdapter();
        if (adapter == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");

        adapter.registerDataSetObserver(mObserver);

        mViewPager = view;
        view.setOnPageChangeListener(this);

        notifyDataSetChanged();
    }

    /**
     * Set the ViewPager associate with this indicator view and the current position;
     *
     * @param view            The ViewPager view.
     * @param initialPosition The current position.
     */
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    private void updateIndicator(int offset, int width) {
        mIndicatorOffset = offset;
        mIndicatorWidth = width;
        invalidate();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        // draw underline
        canvas.drawRect(0, getHeight() - mUnderLineHeight, getWidth(), getHeight(), mUnderLinePaint); // must do it first

        int x = mIndicatorOffset + getPaddingLeft();
        canvas.drawRect(x, getHeight() - mIndicatorHeight, x + mIndicatorWidth, getHeight(), mPaint);

        if (isInEditMode())
            canvas.drawRect(getPaddingLeft(), getHeight() - mIndicatorHeight, getPaddingLeft() + getChildAt(0).getWidth(), getHeight(), mPaint);
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mScrolling = false;
            TextView tv = getTabView(mSelectedPosition);
            if (tv != null)
                switch (mIndicatorMode) {
                    case MATCH_PARENT:
                        updateIndicator(tv.getLeft(), tv.getMeasuredWidth());
                        break;
                    case WRAP_CONTENT:
                        int textWidth = getTextWidth(tv);
                        updateIndicator(tv.getLeft() + tv.getWidth() / 2 - textWidth / 2, textWidth);
                        break;
                }
        } else
            mScrolling = true;

        if (mListener != null)
            mListener.onPageScrollStateChanged(state);
    }

    private int getTextWidth(TextView tv) {
        return (int) tv.getPaint().measureText(tv.getText().toString());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mListener != null)
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);

        CheckedTextView tv_scroll = getTabView(position);
        CheckedTextView tv_next = getTabView(position + 1);

        if (tv_scroll != null && tv_next != null) {
            int width_scroll = mIndicatorMode == MATCH_PARENT ? tv_scroll.getWidth() : getTextWidth(tv_scroll);
            int width_next = mIndicatorMode == MATCH_PARENT ? tv_next.getWidth() : getTextWidth(tv_next);
            float distance = mIndicatorMode == MATCH_PARENT ? (width_scroll + width_next) / 2f : width_scroll / 2 + tv_scroll.getWidth() / 2 + tv_next.getWidth() / 2 - width_next / 2;

            int width = (int) (width_scroll + (width_next - width_scroll) * positionOffset + 0.5f);
            int offset = mIndicatorMode == MATCH_PARENT ?
                    (int) (tv_scroll.getLeft() + width_scroll / 2f + distance * positionOffset - width / 2f + 0.5f)
                    : (int) (tv_scroll.getLeft() + tv_scroll.getWidth() / 2 - width_scroll / 2 + distance * positionOffset + 0.5f);
            updateIndicator(offset, width);
        }
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentItem(position);
        if (mListener != null)
            mListener.onPageSelected(position);
    }

    @Override
    public void onClick(android.view.View v) {
        int position = (Integer) v.getTag();
        if (position == mSelectedPosition && mListener != null)
            mListener.onPageSelected(position);

        mViewPager.setCurrentItem(position, true);
    }

    /**
     * Set the current page of this TabPageIndicator.
     *
     * @param position The position of current page.
     */
    public void setCurrentItem(int position) {
        if (mSelectedPosition != position) {
            CheckedTextView tv = getTabView(mSelectedPosition);
            if (tv != null)
                tv.setChecked(false);
        }

        mSelectedPosition = position;
        CheckedTextView tv = getTabView(mSelectedPosition);
        if (tv != null)
            tv.setChecked(true);

        animateToTab(position);
    }

    /**
     *
     */
    private void notifyDataSetChanged() {
        removeAllViews();

        PagerAdapter adapter = mViewPager.getAdapter();
        final int count = adapter.getCount();

        if (mSelectedPosition > count)
            mSelectedPosition = count - 1;

        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if (title == null)
                title = "NULL";

            CheckedTextView tv = new CheckedTextView(getContext());
            tv.setCheckMarkDrawable(null);
            tv.setText(title);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(getContext(), mTextAppearance);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setOnClickListener(this);
            tv.setTag(i);

            if (mMode == MODE_SCROLL) {
                tv.setPadding(mTabPadding, 0, mTabPadding, 0);
                addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (mMode == MODE_FIXED) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1f;
                addView(tv, params);
            }

        }

        setCurrentItem(mSelectedPosition);
        requestLayout();
    }

    private void notifyDataSetInvalidated() {
        PagerAdapter adapter = mViewPager.getAdapter();
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            TextView tv = getTabView(i);
            CharSequence title = adapter.getPageTitle(i);
            if (title == null)
                title = "NULL";

            tv.setText(title);
        }

        requestLayout();
    }

    private void addTemporaryTab() {
        for (int i = 0; i < 3; i++) {
            CharSequence title = null;
            if (i == 0)
                title = "流行新品";
            else if (i == 1)
                title = "最近上新";
            else if (i == 2)
                title = "人气热销";

            CheckedTextView tv = new CheckedTextView(getContext());
            tv.setCheckMarkDrawable(null);
            tv.setText(title);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(getContext(), mTextAppearance);
            tv.setSingleLine(true);
            tv.setTag(i);
            tv.setChecked(i == 0);
            if (mMode == MODE_SCROLL) {
                tv.setPadding(mTabPadding, 0, mTabPadding, 0);
                addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (mMode == MODE_FIXED) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1f;
                addView(tv, params);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mSelectedPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mSelectedPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

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