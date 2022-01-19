package com.renju_note.isoo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private float DENSITY;

    // for saving settings
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;


    // part of basic settings
    public String boardColor;
    public String editTextColor;
    public String lineColor;
    public boolean editTextVisible;
    public boolean sequenceVisible;
    public int num_minus;


    // components of setting screen
    private CheckBox seqV;
    private CheckBox editV;
    private Button numM;
    private Button boardC;
    private Button boardLC;
    private Button editC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sf = getSharedPreferences("SetData", Activity.MODE_PRIVATE);
        editor = sf.edit();
        editor.apply();

        seqV = findViewById(R.id.show_sequence);
        editV = findViewById(R.id.show_textbox);
        numM = findViewById(R.id.start_point);
        boardC = findViewById(R.id.board_color);
        boardLC = findViewById(R.id.boardLine_color);
        editC = findViewById(R.id.textbox_color);

        DENSITY = density();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSetting();
        setRatio();
    }

    private void saveSetting() {
        editTextVisible = editV.isChecked();
        sequenceVisible = seqV.isChecked();

        editor.putString("boardColor",boardColor);
        editor.putString("editTextColor",editTextColor);
        editor.putString("lineColor",lineColor);
        editor.putBoolean("editTextVisible",editTextVisible);
        editor.putBoolean("sequenceVisible",sequenceVisible);
        editor.putInt("num_minus",num_minus);
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    private void setSetting() {
        boardColor = sf.getString("boardColor", "#F2CA94");
        editTextColor = sf.getString("editTextColor", "#99BBFF");
        lineColor = sf.getString("lineColor","#666666");
        editTextVisible = sf.getBoolean("editTextVisible", true);
        sequenceVisible = sf.getBoolean("sequenceVisible", true);
        num_minus = sf.getInt("num_minus", 0);

        seqV.setChecked(sequenceVisible);
        editV.setChecked(editTextVisible);

        numM.setText("Start point of sequence  :  "+ num_minus);

        boardC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(boardColor),null);

        boardLC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(lineColor),null);

        editC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(editTextColor),null);
    }

    // make color preview drawable
    private GradientDrawable makeDrawable(String color) {
        GradientDrawable drawable1 = new GradientDrawable();
        drawable1.setColor(Color.parseColor(color));
        drawable1.setStroke((int)(1.2 * DENSITY),Color.parseColor("#666666"));
        drawable1.setShape(GradientDrawable.OVAL);
        drawable1.setSize((int)(38*DENSITY),(int)(38*DENSITY));
        return drawable1;
    }

    private void sendData() {
        Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
        intent.putExtra("boardColor",boardColor);
        intent.putExtra("editTextColor",editTextColor);
        intent.putExtra("lineColor",lineColor);
        intent.putExtra("editTextVisible",editTextVisible);
        intent.putExtra("sequenceVisible",sequenceVisible);
        intent.putExtra("num_minus",num_minus);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        saveSetting();
        sendData();
    }

    public void backButtonClicked(View v) {
        saveSetting();
        sendData();
    }

    private float density() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    @SuppressLint("SetTextI18n")
    public void startPointButtonClicked(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Choose start point");
        NumberPicker num = new NumberPicker(this);
        num.setMaxValue(225);
        num.setMinValue(0);
        alert.setView(num);
        alert.setPositiveButton("Apply", (dialog, whichButton) -> {
            num_minus = num.getValue();
            numM.setText("Start point of sequence  :  "+ num_minus);
        });
        alert.show();
    }

    public void boardColorButtonClicked(View v) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker);
        dialog.show();

        View show_color = dialog.findViewById(R.id.show_color);
        show_color.setBackgroundColor(Color.parseColor(boardColor));

        SeekBar r_bar = dialog.findViewById(R.id.r_bar);
        SeekBar g_bar = dialog.findViewById(R.id.g_bar);
        SeekBar b_bar = dialog.findViewById(R.id.b_bar);

        r_bar.setProgress(Color.red(Color.parseColor(boardColor)));
        g_bar.setProgress(Color.green(Color.parseColor(boardColor)));
        b_bar.setProgress(Color.blue(Color.parseColor(boardColor)));

        r_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(progress,g_bar.getProgress(),b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        g_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),progress,b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),g_bar.getProgress(),progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button apply = dialog.findViewById(R.id.apply_button);
        apply.setOnClickListener(v1 -> {
            ColorDrawable viewColor = (ColorDrawable)show_color.getBackground();
            boardC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(String.format("#%06X", (0xFFFFFF & viewColor.getColor()))),null);
            boardColor = String.format("#%06X", (0xFFFFFF & viewColor.getColor()));
            dialog.dismiss();
        });
    }

    public void textBoxColorButtonClicked(View v) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker);
        dialog.show();

        View show_color = dialog.findViewById(R.id.show_color);
        show_color.setBackgroundColor(Color.parseColor(editTextColor));

        SeekBar r_bar = dialog.findViewById(R.id.r_bar);
        SeekBar g_bar = dialog.findViewById(R.id.g_bar);
        SeekBar b_bar = dialog.findViewById(R.id.b_bar);

        r_bar.setProgress(Color.red(Color.parseColor(editTextColor)));
        g_bar.setProgress(Color.green(Color.parseColor(editTextColor)));
        b_bar.setProgress(Color.blue(Color.parseColor(editTextColor)));

        r_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(progress,g_bar.getProgress(),b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        g_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),progress,b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),g_bar.getProgress(),progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button apply = dialog.findViewById(R.id.apply_button);
        apply.setOnClickListener(v1 -> {
            ColorDrawable viewColor = (ColorDrawable)show_color.getBackground();
            editC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(String.format("#%06X", (0xFFFFFF & viewColor.getColor()))),null);
            editTextColor = String.format("#%06X", (0xFFFFFF & viewColor.getColor()));
            dialog.dismiss();
        });
    }

    public void boardLineColorButtonClicked(View v) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker);
        dialog.show();

        View show_color = dialog.findViewById(R.id.show_color);
        show_color.setBackgroundColor(Color.parseColor(lineColor));

        SeekBar r_bar = dialog.findViewById(R.id.r_bar);
        SeekBar g_bar = dialog.findViewById(R.id.g_bar);
        SeekBar b_bar = dialog.findViewById(R.id.b_bar);

        r_bar.setProgress(Color.red(Color.parseColor(lineColor)));
        g_bar.setProgress(Color.green(Color.parseColor(lineColor)));
        b_bar.setProgress(Color.blue(Color.parseColor(lineColor)));

        r_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(progress,g_bar.getProgress(),b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        g_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),progress,b_bar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                show_color.setBackgroundColor(Color.rgb(r_bar.getProgress(),g_bar.getProgress(),progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button apply = dialog.findViewById(R.id.apply_button);
        apply.setOnClickListener(v1 -> {
            ColorDrawable viewColor = (ColorDrawable)show_color.getBackground();
            boardLC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(String.format("#%06X", (0xFFFFFF & viewColor.getColor()))),null);
            lineColor = String.format("#%06X", (0xFFFFFF & viewColor.getColor()));
            dialog.dismiss();
        });
    }

    @SuppressLint("SetTextI18n")
    public void resetButtonClicked(View v) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.MyAlertDialogTheme);

        builder.setTitle("Reset your settings");
        builder.setMessage("Are you sure you want to reset your settings?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            boardColor = "#F2CA94";
            editTextColor = "#99BBFF";
            lineColor = "#666666";
            editTextVisible = true;
            sequenceVisible = true;
            num_minus =  0;

            seqV.setChecked(sequenceVisible);
            editV.setChecked(editTextVisible);
            numM.setText("Start point of sequence  :  "+ num_minus);
            boardC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(boardColor),null);
            boardLC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(lineColor),null);
            editC.setCompoundDrawablesWithIntrinsicBounds(null,null,makeDrawable(editTextColor),null);
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {

        });

        builder.show();
    }

    /*
    dp -> px : (int)(dp * density + 0.5) = px
    (int)(size.x(px) / num / density + 0.5)(dp) : the ratio based on the width
     */
    private void setRatio() {
        WindowManager windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        LinearLayout bar = findViewById(R.id.custom_bar);
        LinearLayout display_area = findViewById(R.id.display_area);
        LinearLayout custom_area = findViewById(R.id.customize_area);

        LinearLayout.LayoutParams params_bar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(size.x / 7.6));
        params_bar.bottomMargin = (int)(20*DENSITY+0.5);
        params_bar.topMargin = (int)(5*DENSITY+0.5);
        bar.setLayoutParams(params_bar);

        LinearLayout.LayoutParams params_area = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size.x / 2);
        params_area.leftMargin = (int)(3*DENSITY+0.5);
        params_area.rightMargin = (int)(3*DENSITY+0.5);
        params_area.bottomMargin = (int)(20*DENSITY+0.5);

        display_area.setLayoutParams(params_area);
        custom_area.setLayoutParams(params_area);

        Button back_bt = findViewById(R.id.back_button);
        Button reset_bt = findViewById(R.id.reset_button);

        back_bt.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 16.4 / DENSITY + 0.5));
        reset_bt.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 15.2 / DENSITY + 0.5));

        int textSize = (int)(size.x / 19.6 / DENSITY + 0.5);
        seqV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        editV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        numM.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        boardC.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        boardLC.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        editC.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);

        TextView title = findViewById(R.id.setting_title);
        TextView display_title = findViewById(R.id.display_title);
        TextView custom_title = findViewById(R.id.customize_title);

        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        display_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        custom_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
    }
}