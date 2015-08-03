package com.bbsymphony.muslimprayers.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by Ahmed on 8/3/2015.
 */
public class EditTextCustom extends EditTextPreference {

    public EditTextCustom(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);

    }

    public EditTextCustom(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public EditTextCustom(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Handler delayedRun = new Handler();
        delayedRun.post(new Runnable() {
            @Override
            public void run() {
                EditText textBox = getEditText();
                textBox.setSelection(textBox.getText().length());
            }
        });
    }
}