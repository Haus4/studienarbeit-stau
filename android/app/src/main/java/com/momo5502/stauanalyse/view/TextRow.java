package com.momo5502.stauanalyse.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TextRow extends TableRow {
    private TextView textView;

    public TextRow(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextSize(17);
        textView.setPadding(10, 30, 10, 30);
        addView(textView);

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        this.setBackgroundResource(outValue.resourceId);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        View line = new View(getContext());
        line.setBackgroundColor(Color.rgb(210, 210, 210));
        line.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        ((TableLayout)getParent()).addView(line);
    }
}
