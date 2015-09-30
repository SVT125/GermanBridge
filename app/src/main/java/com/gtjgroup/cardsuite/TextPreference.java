package com.gtjgroup.cardsuite;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.Preference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TextPreference extends Preference {

    private String name = "";

    public TextPreference(Context context) {
        super(context);
    }

    public TextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        if (name.isEmpty())
            this.setName();

        LinearLayout layout = new LinearLayout(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                400, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 40, 0, 40);

        TextView titleText = new TextView(getContext());
        titleText.setText(getTitle());
        titleText.setTextSize(18);
        titleText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        titleText.setLayoutParams(textParams);

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                400,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editParams.setMargins(300, 5, 100, 5);

        final EditText editText = new EditText(getContext());
        editText.setLayoutParams(editParams);
        editText.setTextSize(16);
        editText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        editText.setText(name);
        editText.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                name = editText.getText().toString();
                updatePreference(name);
            }
        });

        layout.addView(titleText);
        layout.addView(editText);
        return layout;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        String temp = restoreValue ? getPersistedString("player one") : (String)defaultValue;

        if(!restoreValue)
            persistString(temp);

        this.name = temp;
    }

    public void setName() {
        if (this.getKey().equals("player_one_text"))
            this.name = "player one";
        if (this.getKey().equals("player_two_text"))
            this.name = "player two";
        if (this.getKey().equals("player_three_text"))
            this.name = "player three";
        if (this.getKey().equals("player_four_text"))
            this.name = "player four";
    }

    public String getName() {
        return this.name;
    }

    private void updatePreference(String newValue) {
        SharedPreferences.Editor editor =  getEditor();
        editor.putString(getKey(), newValue);
        editor.commit();
    }
}
