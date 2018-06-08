/*
 * Copyright 2018 Keval Patel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance wit
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *  the specific language governing permissions and limitations under the License.
 */

package com.kevalpatel2106.rulerpicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.joda.time.LocalTime;

import static com.kevalpatel2106.rulerpicker.RulerView.MINUTES_DELTA;
import static com.kevalpatel2106.rulerpicker.RulerView.gapsInHour;


/**
 * Created by Kevalpatel2106 on 29-Mar-2018.
 * <p>
 * <li>Diagram:</li>
 * Observable ScrollView
 * |------------------|---------------------\--/----------------------|------------------|<br/>
 * |                  |                      \/                       |                  |<br/>
 * |                  |                                               |                  |<br/>
 * |  Left Spacer     |                 RulerView                     |  Right Spacer    |<br/>
 * |                  |                                               |                  |<br/>
 * |                  |                                               |                  |<br/>
 * |------------------|-----------------------------------------------|------------------|<br/>
 *
 * @see <a href="https://github.com/dwfox/DWRulerView>Original Repo</a>
 */
public final class RulerValuePicker extends LinearLayout implements ObservableHorizontalScrollView.ScrollChangedListener {

    public static final String TAG = RulerValuePicker.class.getName();

    RulerPickerState state = RulerPickerState.IDLE;

    /**
     * Left side empty view to add padding to the ruler.
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    private View mLeftSpacer;

    /**
     * Right side empty view to add padding to the ruler.
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    private View mRightSpacer;

    /**
     * Ruler view with values.
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    private RulerView mRulerView;

    /**
     * {@link ObservableHorizontalScrollView}, that will host all three components.
     *
     * @see #mLeftSpacer
     * @see #mRightSpacer
     * @see #mRulerView
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    private ObservableHorizontalScrollView mHorizontalScrollView;

    @Nullable
    private RulerValuePickerListener mListener;

    int centerValue;
    int startValue;
    int endValue;

    private int mNotchColor = Color.WHITE;

    private Button button;

    /* start picker variables */
    private Picker startPicker;

    /* end picker variables */
    private Picker endPicker;


    /**
     * Public constructor.
     */
    public RulerValuePicker(@NonNull final Context context) {
        super(context);
        init(null);
    }

    /**
     * Public constructor.
     */
    public RulerValuePicker(@NonNull final Context context,
                            @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     * Public constructor.
     */
    public RulerValuePicker(@NonNull final Context context,
                            @Nullable final AttributeSet attrs,
                            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Public constructor.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RulerValuePicker(@NonNull final Context context,
                            @Nullable final AttributeSet attrs,
                            final int defStyleAttr,
                            final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * Initialize the view and parse the {@link AttributeSet}.
     *
     * @param attributeSet {@link AttributeSet} to parse or null if no attribute parameters set.
     */
    private void init(@Nullable AttributeSet attributeSet) {

        //Add all the children
        addChildViews();

        if (attributeSet != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet,
                    R.styleable.RulerValuePicker,
                    0,
                    0);

            try { //Parse params
                if (a.hasValue(R.styleable.RulerValuePicker_notch_color)) {
                    mNotchColor = a.getColor(R.styleable.RulerValuePicker_notch_color, Color.WHITE);
                }

                if (a.hasValue(R.styleable.RulerValuePicker_ruler_text_color)) {
                    setTextColor(a.getColor(R.styleable.RulerValuePicker_ruler_text_color, Color.WHITE));
                }

                if (a.hasValue(R.styleable.RulerValuePicker_ruler_text_size)) {
                    setTextSize((int) a.getDimension(R.styleable.RulerValuePicker_ruler_text_size, 14));
                }

                if (a.hasValue(R.styleable.RulerValuePicker_indicator_color)) {
                    setIndicatorColor(a.getColor(R.styleable.RulerValuePicker_indicator_color, Color.WHITE));
                }

                if (a.hasValue(R.styleable.RulerValuePicker_indicator_width)) {
                    setIndicatorWidth(a.getDimensionPixelSize(R.styleable.RulerValuePicker_indicator_width,
                            4));
                }

                if (a.hasValue(R.styleable.RulerValuePicker_indicator_interval)) {
                    setIndicatorIntervalDistance(a.getDimensionPixelSize(R.styleable.RulerValuePicker_indicator_interval,
                            4));
                }

                if (a.hasValue(R.styleable.RulerValuePicker_long_height_height_ratio)
                        || a.hasValue(R.styleable.RulerValuePicker_short_height_height_ratio)) {

                    setIndicatorHeight(a.getFraction(R.styleable.RulerValuePicker_long_height_height_ratio,
                            1, 1, 0.6f),
                            a.getFraction(R.styleable.RulerValuePicker_short_height_height_ratio,
                                    1, 1, 0.4f));
                }

            } finally {
                a.recycle();
            }
        }

        startPicker = new Picker(true);
        startPicker.setCircleColor(Color.BLUE);

        endPicker = new Picker(false);
        endPicker.setCircleColor(Color.RED);

    }


    /**
     * Programmatically add the children to the view.
     * <p>
     * <li>The main view contains the {@link android.widget.HorizontalScrollView}. That allows
     * {@link RulerView} to scroll horizontally.</li>
     * <li>{@link #mHorizontalScrollView} contains {@link LinearLayout} that will act as the container
     * to hold the children inside the horizontal view.</li>
     * <li>{@link LinearLayout} container will contain three children.
     * <ul><b>Left spacer:</b> Width of this view will be the half width of the view. This will add staring at the start of the ruler.</ul>
     * <ul><b>Right spacer:</b> Width of this view will be the half width of the view. This will add ending at the end of the ruler.</ul>
     * <ul><b>{@link RulerView}:</b> Ruler view will contain the ruler with indicator.</ul>
     * </li>
     */
    @SuppressLint("ClickableViewAccessibility")
    private void addChildViews() {

        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER);

        button = new Button(getContext());
        button.setText(R.string.confirm_time);
        button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RulerPickerState oldState = state;
                if(state == RulerPickerState.PICKING_END) {
                    state = RulerPickerState.END_PICKED;
                }
                if(state == RulerPickerState.PICKING_START) {
                    state = RulerPickerState.START_PICKED;
                }
                if(oldState != state) {
                    invalidate();
                }

                if(mListener != null) {

                    if(oldState == RulerPickerState.PICKING_START) {
                        mListener.onTimePicked(getStartTime(), true);
                    } else {
                        mListener.onTimePicked(getEndTime(), false);
                    }

                }
            }
        });


        mHorizontalScrollView = new ObservableHorizontalScrollView(getContext(), this);
        mHorizontalScrollView.setHorizontalScrollBarEnabled(false); //Don't display the scrollbar

//        mHorizontalScrollView.setScrollEnabled(false);
        mHorizontalScrollView.setOnCustomTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return false;
            }
        });

        final LinearLayout rulerContainer = new LinearLayout(getContext());

        //Add left spacing to the container
        mLeftSpacer = new View(getContext());
        rulerContainer.addView(mLeftSpacer);

        //Add ruler to the container
        mRulerView = new RulerView(getContext());
        rulerContainer.addView(mRulerView);

        //Add right spacing to the container
        mRightSpacer = new View(getContext());
        rulerContainer.addView(mRightSpacer);

        //Add this container to the scroll view.
        mHorizontalScrollView.removeAllViews();
        mHorizontalScrollView.addView(rulerContainer);

        //Add scroll view to this view.
        removeAllViews();
        addView(mHorizontalScrollView);
        addView(button);


        Log.d(TAG, "rullerContainerWidth:" + rulerContainer.getWidth());
        Log.d(TAG, "rullerContainerHeight:" + rulerContainer.getHeight());


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* check to show button */
        if(state == RulerPickerState.PICKING_START || state == RulerPickerState.PICKING_END) {
            button.setVisibility(VISIBLE);
            mHorizontalScrollView.setScrollEnabled(true);
        } else {
            button.setVisibility(GONE);
            mHorizontalScrollView.setScrollEnabled(false);
        }

        if(state != RulerPickerState.PICKING_END) {
            int startPickerX = getWidth()/2 - mRulerView.getIndicatorIntervalWidth() * (centerValue - startValue);
            startPicker.draw(canvas, this, startPickerX, mRulerView.mHalfHeight, state != RulerPickerState.IDLE);
        }

        if(state != RulerPickerState.PICKING_START) {
            int endPickerX = getWidth()/2 + mRulerView.getIndicatorIntervalWidth() * (endValue - centerValue);
            endPicker.draw(canvas, this, endPickerX, mRulerView.mHalfHeight, state != RulerPickerState.IDLE);
        }

    }

    @Override
    protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);

        if (isChanged) {
            final int width = getWidth();

            //Set width of the left spacer to the half of this view.
            final ViewGroup.LayoutParams leftParams = mLeftSpacer.getLayoutParams();
            leftParams.width = width / 2;
            mLeftSpacer.setLayoutParams(leftParams);

            //Set width of the right spacer to the half of this view.
            final ViewGroup.LayoutParams rightParams = mRightSpacer.getLayoutParams();
            rightParams.width = width / 2;
            mRightSpacer.setLayoutParams(rightParams);


            invalidate();
        }
    }


    /**
     * Scroll the ruler to the given value.
     *
     * @param value Value to select. Value must be between {@link #getMinValue()} and {@link #getMaxValue()}.
     *              If the value is less than {@link #getMinValue()}, {@link #getMinValue()} will be
     *              selected.If the value is greater than {@link #getMaxValue()}, {@link #getMaxValue()}
     *              will be selected.
     */
    public void selectValue(final int value) {
        mHorizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int valuesToScroll;
                if (value < mRulerView.getMinValue()) {
                    valuesToScroll = 0;
                } else if (value > mRulerView.getMaxValue()) {
                    valuesToScroll = mRulerView.getMaxValue() - mRulerView.getMinValue();
                } else {
                    valuesToScroll = value - mRulerView.getMinValue();
                }

                mHorizontalScrollView.smoothScrollBy(
                        valuesToScroll * mRulerView.getIndicatorIntervalWidth(), 0);
            }
        }, 400);
    }

    /**
     * method for setting this picker hour and minute using a LocalTime object
     * @param time LocalTime object
     * @return this TimePicker instance
     */
    public RulerValuePicker setStartTimePicker(LocalTime time) {
        int newValue = newValueWithHour(time.getHourOfDay(), startValue);
        newValue = newValueWithMinute(newValue, time.getMinuteOfHour());

        startValue = newValue;
        startPicker.setRulerValue(startValue);
        startPicker.setTimeValue(mRulerView.getTimeValue(startValue));

        invalidate();

        return this;
    }

    /**
     *  method for setting the hour value
     *  @param hour the desired hour to be set
     *  @return An instance of this TimePickerSpinner
     */
    int newValueWithHour(int hour, int value) {

        /* check if hour is not greater than 23 */
        if(hour > 23) {
            hour = 23;
        }

        /* calculate value according to current minute*/
        int newValue = hour * gapsInHour;
        return decideNewValue(newValue);

    }

    /**
     * method for setting the minute value
     * @param minutes desired minutes to be set
     * @return An instance of this TimePickerSpinner
     */
    int newValueWithMinute(int value, int minutes) {
        if(minutes > 60) {
            minutes = 55;
        }

        int minuteGap = (int) Math.ceil(minutes / MINUTES_DELTA);
        int newValue = value + minuteGap;
        return decideNewValue(newValue);

    }

    /**
     * method for setting the new value
     * check if new value is higher or lower than min/max value
     * @param newValue New Value to be checked and set
     */
    private int decideNewValue(int newValue) {
        if(newValue > getMaxValue()) {
            return getMaxValue();
        } else if(newValue < getMinValue()) {
            return getMinValue();
        }
        return newValue;
    }

    /**
     * @return Get the current selected value.
     */
    public int getCurrentValue() {
        int absoluteValue = mHorizontalScrollView.getScrollX() / mRulerView.getIndicatorIntervalWidth();
        int value = mRulerView.getMinValue() + absoluteValue;

        if (value > mRulerView.getMaxValue()) {
            return mRulerView.getMaxValue();
        } else if (value < mRulerView.getMinValue()) {
            return mRulerView.getMinValue();
        } else {
            return value;
        }
    }

    @Override
    public void onScrollChanged() {

        /* update values */
        centerValue = getCurrentValue();

        if(centerValue + gapsInHour < mRulerView.getMaxValue()) {
            endValue = centerValue + gapsInHour;
        } else {
            endValue = mRulerView.getMaxValue();
        }

        if(centerValue - gapsInHour > mRulerView.getMinValue()) {
            startValue = centerValue - gapsInHour;
        } else {
            startValue = getMinValue();
        }

        startPicker.setRulerValue(startValue);
        endPicker.setRulerValue(endValue);
        startPicker.setTimeValue(mRulerView.getTimeValue(startValue));
        endPicker.setTimeValue(mRulerView.getTimeValue(endValue));

        if (mListener != null) mListener.onIntermediateValueChange(getStartTime(), getEndTime());

    }

    @Override
    public void onScrollStopped() {
        makeOffsetCorrection(mRulerView.getIndicatorIntervalWidth());
        if (mListener != null) {
            mListener.onValueChange(getStartTime(), getEndTime());
        }
    }

    private void makeOffsetCorrection(final int indicatorInterval) {
        int offsetValue = mHorizontalScrollView.getScrollX() % indicatorInterval;
        if (offsetValue < indicatorInterval / 2) {
            mHorizontalScrollView.scrollBy(-offsetValue, 0);
        } else {
            mHorizontalScrollView.scrollBy(indicatorInterval - offsetValue, 0);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.value = getCurrentValue();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        selectValue(ss.value);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getActionMasked();
        float initialX = event.getX();
        float initialY = event.getY();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                if(state != RulerPickerState.PICKING_START && state != RulerPickerState.PICKING_END) {
                    handleIdleStateClick(initialX, initialY);
                }
                break;
        }

        return true;
    }

    void handleIdleStateClick(float x, float y) {
        if(startPicker.isInside(x, y)) {
            state = RulerPickerState.PICKING_START;
        } else if(endPicker.isInside(x,y)) {
            state = RulerPickerState.PICKING_END;
        }
        invalidate();
    }



    //**********************************************************************************//
    //******************************** GETTERS/SETTERS *********************************//
    //**********************************************************************************//

    /**
     * @param notchColorRes Color resource of the notch to display. Default color os {@link Color#WHITE}.
     * @see #setNotchColor(int)
     * @see #getNotchColor()
     */
    public void setNotchColorRes(@ColorRes final int notchColorRes) {
        setNotchColor(ContextCompat.getColor(getContext(), notchColorRes));
    }

    /**
     * @return Integer color of the notch. Default color os {@link Color#WHITE}.
     * @see #setNotchColor(int)
     * @see #setNotchColorRes(int)
     */
    @ColorInt
    public int getNotchColor() {
        return mNotchColor;
    }

    /**
     * @param notchColor Integer color of the notch to display. Default color os {@link Color#WHITE}.
     * @see #getNotchColor()
     */
    public void setNotchColor(@ColorInt final int notchColor) {
        mNotchColor = notchColor;
        invalidate();
    }

    /**
     * @return Color integer value of the ruler text color.
     * @see #setTextColor(int)
     * @see #setTextColorRes(int)
     */
    @CheckResult
    @ColorInt
    public int getTextColor() {
        return mRulerView.getTextColor();
    }

    /**
     * Set the color of the text to display on the ruler.
     *
     * @param color Color integer value.
     * @see #getTextColor()
     * @see RulerView#mTextColor
     */
    public void setTextColor(@ColorInt final int color) {
        mRulerView.setTextColor(color);
    }

    /**
     * Set the color of the text to display on the ruler.
     *
     * @param color Color resource id.
     * @see RulerView#mTextColor
     */
    public void setTextColorRes(@ColorRes final int color) {
        setTextColor(ContextCompat.getColor(getContext(), color));
    }

    /**
     * @return Size of the text of ruler in dp.
     * @see #setTextSize(int)
     * @see #setTextSizeRes(int)
     * @see RulerView#mTextColor
     */
    @CheckResult
    public float getTextSize() {
        return mRulerView.getTextSize();
    }

    /**
     * Set the size of the text to display on the ruler.
     *
     * @param dimensionDp Text size dimension in dp.
     * @see #getTextSize()
     * @see RulerView#mTextSize
     */
    public void setTextSize(final int dimensionDp) {
        mRulerView.setTextSize(dimensionDp);
    }

    /**
     * Set the size of the text to display on the ruler.
     *
     * @param dimension Text size dimension resource.
     * @see #getTextSize()
     * @see RulerView#mTextSize
     */
    public void setTextSizeRes(@DimenRes final int dimension) {
        setTextSize((int) getContext().getResources().getDimension(dimension));
    }

    /**
     * @return Color integer value of the indicator color.
     * @see #setIndicatorColor(int)
     * @see #setIndicatorColorRes(int)
     * @see RulerView#mIndicatorColor
     */
    @CheckResult
    @ColorInt
    public int getIndicatorColor() {
        return mRulerView.getIndicatorColor();
    }

    /**
     * Set the indicator color.
     *
     * @param color Color integer value.
     * @see #getIndicatorColor()
     * @see RulerView#mIndicatorColor
     */
    public void setIndicatorColor(@ColorInt final int color) {
        mRulerView.setIndicatorColor(color);
    }

    /**
     * Set the indicator color.
     *
     * @param color Color resource id.
     * @see #getIndicatorColor()
     * @see RulerView#mIndicatorColor
     */
    public void setIndicatorColorRes(@ColorRes final int color) {
        setIndicatorColor(ContextCompat.getColor(getContext(), color));
    }

    /**
     * @return Width of the indicator in pixels.
     * @see #setIndicatorWidth(int)
     * @see #setIndicatorWidthRes(int)
     * @see RulerView#mIndicatorWidthPx
     */
    @CheckResult
    public float getIndicatorWidth() {
        return mRulerView.getIndicatorWidth();
    }

    /**
     * Set the width of the indicator line in the ruler.
     *
     * @param widthPx Width in pixels.
     * @see #getIndicatorWidth()
     * @see RulerView#mIndicatorWidthPx
     */
    public void setIndicatorWidth(final int widthPx) {
        mRulerView.setIndicatorWidth(widthPx);
    }

    /**
     * Set the width of the indicator line in the ruler.
     *
     * @param width Dimension resource for indicator width.
     * @see #getIndicatorWidth()
     * @see RulerView#mIndicatorWidthPx
     */
    public void setIndicatorWidthRes(@DimenRes final int width) {
        setIndicatorWidth(getContext().getResources().getDimensionPixelSize(width));
    }

    /**
     * @return Get the minimum value displayed on the ruler.
     * @see RulerView#mMinValue
     */
    @CheckResult
    public int getMinValue() {
        return mRulerView.getMinValue();
    }

    /**
     * @return Get the maximum value displayed on the ruler.
     * @see RulerView#mMaxValue
     */
    @CheckResult
    public int getMaxValue() {
        return mRulerView.getMaxValue();
    }

    public LocalTime getStartTime() {
        return new LocalTime(startValue / gapsInHour, (startValue % gapsInHour) * MINUTES_DELTA);
    }

    public LocalTime getEndTime() {
        return new LocalTime(endValue / gapsInHour, (endValue % gapsInHour) * MINUTES_DELTA);
    }


    /**
     * @return Get distance between two indicator in pixels.
     * @see #setIndicatorIntervalDistance(int)
     * @see RulerView#mIndicatorInterval
     */
    @CheckResult
    public int getIndicatorIntervalWidth() {
        return mRulerView.getIndicatorIntervalWidth();
    }

    /**
     * Set the spacing between two vertical lines/indicators. Default value is 14 pixels.
     *
     * @param indicatorIntervalPx Distance in pixels. This cannot be negative number or zero.
     * @see RulerView#mIndicatorInterval
     */
    public void setIndicatorIntervalDistance(final int indicatorIntervalPx) {
        mRulerView.setIndicatorIntervalDistance(indicatorIntervalPx);
    }

    /**
     * @return Ratio of long indicator height to the ruler height.
     * @see #setIndicatorHeight(float, float)
     * @see RulerView#mLongIndicatorHeightRatio
     */
    @CheckResult
    public float getLongIndicatorHeightRatio() {
        return mRulerView.getLongIndicatorHeightRatio();
    }

    /**
     * @return Ratio of short indicator height to the ruler height.
     * @see #setIndicatorHeight(float, float)
     * @see RulerView#mShortIndicatorHeight
     */
    @CheckResult
    public float getShortIndicatorHeightRatio() {
        return mRulerView.getShortIndicatorHeightRatio();
    }

    /**
     * Set the height of the long and short indicators.
     *
     * @param longHeightRatio  Ratio of long indicator height to the ruler height. This value must
     *                         be between 0 to 1. The value should greater than {@link #getShortIndicatorHeightRatio()}.
     *                         Default value is 0.6 (i.e. 60%). If the value is 0, indicator won't
     *                         be displayed. If the value is 1, indicator height will be same as the
     *                         ruler height.
     * @param shortHeightRatio Ratio of short indicator height to the ruler height. This value must
     *                         be between 0 to 1. The value should less than {@link #getLongIndicatorHeightRatio()}.
     *                         Default value is 0.4 (i.e. 40%). If the value is 0, indicator won't
     *                         be displayed. If the value is 1, indicator height will be same as
     *                         the ruler height.
     * @see #getLongIndicatorHeightRatio()
     * @see #getShortIndicatorHeightRatio()
     */
    public void setIndicatorHeight(final float longHeightRatio,
                                   final float shortHeightRatio) {
        mRulerView.setIndicatorHeight(longHeightRatio, shortHeightRatio);
    }

    /**
     * Set the {@link RulerValuePickerListener} to get callbacks when the value changes.
     *
     * @param listener {@link RulerValuePickerListener}
     */
    public void setValuePickerListener(@Nullable final RulerValuePickerListener listener) {
        mListener = listener;
    }

    /**
     * User interface state that is stored by RulerView for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        private int value = 0;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            value = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(value);
        }
    }
}
