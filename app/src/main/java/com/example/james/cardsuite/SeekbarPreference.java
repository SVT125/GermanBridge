package com.example.james.cardsuite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekbarPreference extends Preference
        implements SeekBar.OnSeekBarChangeListener {


    public int maximum    = 100;
    public int interval   = 5;

    private float oldValue = 50;
    private TextView monitorBox;


    public SeekbarPreference(Context context) {
        super(context);
    }

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekbarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent){

        LinearLayout layout = new LinearLayout(getContext());

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                300, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 40, 0, 40);

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        barParams.setMargins(200, 5, 100, 5);

        LinearLayout.LayoutParams monitorParams = new LinearLayout.LayoutParams(
                30, LinearLayout.LayoutParams.WRAP_CONTENT);
        monitorParams.gravity = Gravity.CENTER;

        layout.setPadding(15, 5, 10, 5);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView view = new TextView(getContext());
        view.setText(getTitle());
        view.setTextSize(18);
        view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        view.setLayoutParams(textParams);

        SeekBar bar = new SeekBar(getContext());
        bar.setMax(maximum);
        bar.setProgress((int) this.oldValue);
        bar.setLayoutParams(barParams);
        bar.setOnSeekBarChangeListener(this);
        LayerDrawable ld = (LayerDrawable) bar.getProgressDrawable();
        ld.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        float[] outR = new float[] {10,10,10,10,10,10,10,10};
        ShapeDrawable thumb = new ShapeDrawable(new RoundRectShape(outR, null, null));
        thumb.setIntrinsicHeight(50);
        thumb.setIntrinsicWidth(10);
        thumb.getPaint().setColor(Color.LTGRAY);
        bar.setThumb(thumb);

        this.monitorBox = new TextView(getContext());
        this.monitorBox.setTextSize(12);
        this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
        this.monitorBox.setLayoutParams(monitorParams);
        this.monitorBox.setPadding(2, 5, 0, 0);
        this.monitorBox.setText(bar.getProgress()+"");


        layout.addView(view);
        layout.addView(bar);
        layout.addView(this.monitorBox);
        layout.setId(android.R.id.widget_frame);


        return layout;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

        progress = Math.round(((float)progress)/interval)*interval;

        if(!callChangeListener(progress)){
            seekBar.setProgress((int)this.oldValue);
            return;
        }

        seekBar.setProgress(progress);
        this.oldValue = progress;
        this.monitorBox.setText(progress+"");
        updatePreference(progress);

        notifyChanged();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    protected Object onGetDefaultValue(TypedArray ta,int index){

        int dValue = (int)ta.getInt(index,50);

        return validateValue(dValue);
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        int temp = restoreValue ? getPersistedInt(50) : (Integer)defaultValue;

        if(!restoreValue)
            persistInt(temp);

        this.oldValue = temp;
    }


    private int validateValue(int value){

        if(value > maximum)
            value = maximum;
        else if(value < 0)
            value = 0;
        else if(value % interval != 0)
            value = Math.round(((float)value)/interval)*interval;


        return value;
    }


    private void updatePreference(int newValue){

        SharedPreferences.Editor editor =  getEditor();
        editor.putInt(getKey(), newValue);
        editor.commit();
    }

}
