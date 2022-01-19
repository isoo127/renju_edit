package com.renju_note.isoo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.jetbrains.annotations.NotNull;

public class BoardLayout extends ConstraintLayout {
    // for debuging (means coordinate that users add stones)
    private float debug_x, debug_y;

    // main paint for drawing lines
    private Paint paint;
    // board size by pixel
    public final int PX;
    // distance between dots
    public final float LINE;
    public final float DENSITY;

    // parent of board (MainActivity)
    private LinearLayout parent;

    // id for number coordinate
    int[] ids_coord_num;
    // id for word coordinate
    int[] ids_coord_char;
    // id for stone text view
    int[] ids_stone;
    // id for child text view
    int[] ids_blank;

    // boolean that coordinate exist
    boolean coord;

    // stones number + 1
    int sequence;

    SeqTree seqTree;

    // mode which is for touch event
    int mode = 0;

    // string that child's text
    String string;

    // part of basic settings
    String boardColor;
    String lineColor = "#666666";
    boolean sequenceVisible;
    int num_minus;

    // last stone's stroke color
    String lastStoneStrokeColor = "#D32560";

    public void setParent(LinearLayout L) {
        parent = L;
    }

    public BoardLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public BoardLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int dp_margin = 10;
        DENSITY = density();
        int margin = (int) (dp_margin * DENSITY + 0.5);
        PX = size.x - margin;

        LINE = (float)(PX / 16);

        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth((int)(PX / 351.3 + 0.5));

        ids_coord_num = new int[15];
        ids_coord_char = new int[15];
        ids_stone = new int[225];
        ids_blank = new int[225];

        sequence = 1;

        coord = false;

        seqTree = new SeqTree();

        SharedPreferences sf = getContext().getSharedPreferences("MainData", Activity.MODE_PRIVATE);
        boardColor = sf.getString("boardColor","#F2CA94");
        lineColor = sf.getString("lineColor","#666666");
        sequenceVisible = sf.getBoolean("sequenceVisible",true);
        num_minus = sf.getInt("num_minus",0);

        paint.setColor(Color.parseColor(lineColor));
    }

    // draw board line when application started
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i = 1; i <= 15; i++) {
            canvas.drawLine(LINE*i+LINE/2,LINE-LINE/2,LINE*i+LINE/2,LINE*15-LINE/2,paint);
            canvas.drawLine(LINE+LINE/2,LINE*i-LINE/2, LINE*15+LINE/2,LINE*i-LINE/2,paint);
        }
        canvas.drawCircle(LINE*8+LINE/2,LINE*8-LINE/2,(int)(PX / 150.5 + 0.5),paint);
        canvas.drawCircle(LINE*4+LINE/2,LINE*12-LINE/2,(int)(PX / 150.5+ 0.5),paint);
        canvas.drawCircle(LINE*12+LINE/2,LINE*12-LINE/2,(int)(PX / 150.5+ 0.5),paint);
        canvas.drawCircle(LINE*4+LINE/2,LINE*4-LINE/2,(int)(PX / 150.5+ 0.5),paint);
        canvas.drawCircle(LINE*12+LINE/2,LINE*4-LINE/2,(int)(PX / 150.5+ 0.5),paint);

        canvas.drawLine(LINE/2,LINE*15,LINE/2,LINE*16,paint);
        canvas.drawLine(0,LINE*15+LINE/2,LINE,LINE*15+LINE/2,paint);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        try {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if ((event.getX() + LINE / 2) % LINE <= LINE / 2)
                    x = event.getX() - ((event.getX() + LINE / 2) % LINE);
                else x = event.getX() - ((event.getX() + LINE / 2) % LINE) + LINE;

                if ((event.getY() - LINE / 2) % LINE <= LINE / 2)
                    y = event.getY() - ((event.getY() - LINE / 2) % LINE);
                else y = event.getY() - ((event.getY() - LINE / 2) % LINE) + LINE;
                // --------------------------------------------------------------------------- set x, y
                if ((int) ((x - LINE / 2) / LINE + 0.5) - 1 >= 0 && (int) ((y + LINE / 2) / LINE + 0.5) - 1 < 15) { // if x,y is in board
                    debug_x = ((x - LINE / 2) / LINE);
                    debug_y = ((y + LINE / 2) / LINE);
                    if (seqTree.getNow_board()[(int) ((x - LINE / 2) / LINE + 0.5) - 1][(int) ((y + LINE / 2) / LINE + 0.5) - 1] == 0) { // if x,y in board and there is no stone
                        //-----------------------------------------------------------------------
                        if (mode == 0) {
                            if (sequence != 1) {
                                if (sequence % 2 == 0)
                                    this.findViewById(ids_stone[sequence - 2]).setBackground(stoneDrawable(Color.BLACK,lineColor));
                                else
                                    this.findViewById(ids_stone[sequence - 2]).setBackground(stoneDrawable(Color.WHITE,lineColor));
                            }
                            // ----------------------------------------------------------------------- change last move drawable
                            addNumber(x, y, sequence);
                            sequence++;

                            seqTree.next(seqTree.getNow(), (int) ((x - LINE / 2) / LINE + 0.5) - 1, (int) ((y + LINE / 2) / LINE + 0.5) - 1, sequence);

                            int j = 0;
                            for (SeqTree.Node temp = seqTree.getNow().getParent().getChild(); temp != null; temp = temp.getNext()) {
                                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                                j++;
                            }

                            if (seqTree.getNow().getChild() != null) {
                                j = 0;
                                for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                                    float c_x = (temp.getX() + 1) * LINE + LINE / 2;
                                    float c_y = (temp.getY() + 1) * LINE - LINE / 2;
                                    addChild(c_x, c_y, j, temp.getText());
                                    j++;
                                }
                            }

                            EditText editText = parent.findViewById(R.id.editText);
                            editText.setText(seqTree.getNow().getBoxText());
                        } else if (mode == 1) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext(),R.style.MyAlertDialogTheme2);
                            alert.setTitle("Put words");
                            alert.setMessage("The maximum number of characters is 3.\n\nThere is a risk of missing text if it is too long.\n");
                            final EditText word = new EditText(getContext());
                            word.setGravity(Gravity.CENTER);
                            word.setHint("Put words here");
                            InputFilter[] FilterArray = new InputFilter[1];
                            FilterArray[0] = new InputFilter.LengthFilter(3);
                            word.setFilters(FilterArray);
                            alert.setView(word);
                            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                                string = word.getText().toString();
                                int c_x = (int) ((x - LINE / 2) / LINE + 0.5) - 1;
                                int c_y = (int) ((y + LINE / 2) / LINE + 0.5) - 1;

                                if (seqTree.getNow().getChild() == null) {
                                    seqTree.createChild(seqTree.getNow(), c_x, c_y);
                                    seqTree.getNow().getChild().setText(string);
                                    addChild(x, y, 0, seqTree.getNow().getChild().getText());
                                } else {
                                    int j = 0;
                                    for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                                        if (temp.getX() == c_x && temp.getY() == c_y) {
                                            temp.setText(string);
                                            ((TextView) (findViewById(ids_blank[j]))).setText(temp.getText());
                                            break;
                                        }
                                        if (temp.getNext() == null) {
                                            seqTree.createNext(temp, seqTree.getNow(), c_x, c_y);
                                            temp.getNext().setText(string);
                                            addChild(x, y, j + 1, temp.getNext().getText());
                                            break;
                                        }
                                        j++;
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
                            });
                            alert.show();

                        }
                        //------------------------------------------------------------------------
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            TextView debug = parent.findViewById(R.id.debug);
            debug.setText("developer mode\n"+" / "+ debug_x +" / "+ debug_y +" / "+ PX +" / "+ LINE);
        }
        TextView seq = parent.findViewById(R.id.sequence);
        seq.setText(String.valueOf(sequence-1));
        return super.onTouchEvent(event);
    }

    // set size of board by depending on the size of machine
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(PX,PX);
    }

    public void addCoordinate(int i, int mode) {
        LayoutParams size = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ConstraintSet constraintSet = new ConstraintSet();
        TextView num;

        if(mode == 0) {
            num = new TextView(getContext());
            num.setText(String.valueOf(i + 1));
            num.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(LINE/2.125/DENSITY));
            num.setGravity(Gravity.CENTER);
            num.setId(generateViewId());
            num.setTextColor(Color.parseColor(lineColor));
            num.setTypeface(Typeface.MONOSPACE);
            ids_coord_num[i] = num.getId();

            this.addView(num,i,size);

            constraintSet.clone(this);
            constraintSet.connect(num.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int) (LINE * (15 - i) - (int)(8.5*DENSITY) -LINE/2));
            constraintSet.connect(num.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int) (LINE*0.5 - (3.5*DENSITY*num.getText().length()) ));
            constraintSet.applyTo(this);
        }
        else if(mode == 1) {
            num = new TextView(getContext());
            num.setText(String.valueOf((char)(i + 65)));
            num.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(LINE/2.125/DENSITY));
            num.setGravity(Gravity.CENTER);
            num.setId(generateViewId());
            num.setTextColor(Color.parseColor(lineColor));
            num.setTypeface(Typeface.MONOSPACE);
            ids_coord_char[i] = num.getId();

            this.addView(num,i,size);

            constraintSet.clone(this);
            constraintSet.connect(num.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int) (LINE*15.5 - (int)(8.5*DENSITY) ));
            constraintSet.connect(num.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int) (LINE*(i+1) - (3.5*DENSITY*num.getText().length()) +LINE/2));
            constraintSet.applyTo(this);
        }
        coord = true;
    }

    public static int generateViewId() {
        return View.generateViewId();
    }

    private float density() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    // add stone
    private void addNumber(float x, float y, int index) {
        LayoutParams size = new LayoutParams((int)(LINE-2),(int)(LINE-2));
        ConstraintSet constraintSet = new ConstraintSet();
        TextView seq = new TextView(getContext());

        if(sequenceVisible) {
            if(index - num_minus > 0) {
                seq.setText(String.valueOf(index - num_minus));
            } else {
                seq.setText("");
            }
        } else {
            seq.setText("");
        }

        if(index%2 == 0) seq.setTextColor(Color.parseColor("#000000"));
        else seq.setTextColor(Color.parseColor("#FFFFFF"));

        seq.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        seq.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(LINE/2.125/DENSITY));
        seq.setGravity(Gravity.CENTER);
        seq.setId(generateViewId());
        ids_stone[index-1] = seq.getId();

        if(index%2 == 0) seq.setBackground(stoneDrawable(Color.WHITE,lastStoneStrokeColor));
        else seq.setBackground(stoneDrawable(Color.BLACK,lastStoneStrokeColor));

        this.addView(seq,index,size);

        constraintSet.clone(this);
        constraintSet.connect(seq.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int)y - (int)(LINE/2 - 1));
        constraintSet.connect(seq.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int)x - (int)(LINE/2 - 1));
        constraintSet.applyTo(this);
    }

    private void addChild(float x, float y, int index, String string) {
        LayoutParams size = new LayoutParams((int)(LINE-2),(int)(LINE-2));
        ConstraintSet constraintSet = new ConstraintSet();
        TextView seq = new TextView(getContext());

        seq.setText(string);
        seq.setLines(1);
        seq.setTextColor(Color.parseColor("#1D40FF"));
        seq.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        seq.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(LINE/2.125/DENSITY));
        seq.setGravity(Gravity.CENTER);
        seq.setId(generateViewId());
        ids_blank[index] = seq.getId();

        seq.setBackground(stoneDrawable(Color.parseColor(boardColor), lineColor));

        this.addView(seq,index,size);

        constraintSet.clone(this);
        constraintSet.connect(seq.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int)y - (int)(LINE/2 - 1));
        constraintSet.connect(seq.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int)x - (int)(LINE/2 - 1));
        constraintSet.applyTo(this);
    }

    public void undo() {
        if(sequence!=1) {
            int j = 0;
            for(SeqTree.Node temp = seqTree.getNow().getChild();temp != null;temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }
            seqTree.undo(seqTree.getNow());
            this.removeViewInLayout(findViewById(ids_stone[sequence - 2]));

            if (seqTree.getNow().getChild() != null) {
                j = 0;
                for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                    float c_x = (temp.getX()+1) * LINE + LINE/2;
                    float c_y = (temp.getY()+1) * LINE - LINE/2;
                    addChild(c_x, c_y, j, temp.getText());
                    j++;
                }
            }
            if(sequence!=2) {
                if (sequence % 2 == 0)
                    this.findViewById(ids_stone[sequence - 3]).setBackground(stoneDrawable(Color.WHITE,lastStoneStrokeColor));
                else
                    this.findViewById(ids_stone[sequence - 3]).setBackground(stoneDrawable(Color.BLACK,lastStoneStrokeColor));
            }

            sequence--;

            EditText editText = parent.findViewById(R.id.editText);
            editText.setText(seqTree.getNow().getBoxText());
        }
    }

    public void redo() {
        if (seqTree.getNow().getChild() != null && seqTree.getNow().getChild().getNext() == null) {
            seqTree.redo(seqTree.getNow(), sequence);
            if (sequence != 1) {
                if (sequence % 2 == 0)
                    this.findViewById(ids_stone[sequence - 2]).setBackground(stoneDrawable(Color.BLACK,lineColor));
                else
                    this.findViewById(ids_stone[sequence - 2]).setBackground(stoneDrawable(Color.WHITE,lineColor));
            }

            float x = (seqTree.getNow().getX()+1) * LINE + LINE/2;
            float y = (seqTree.getNow().getY()+1) * LINE - LINE/2;
            addNumber(x, y, sequence);
            sequence++;

            int j = 0;
            for(SeqTree.Node temp = seqTree.getNow().getParent().getChild();temp != null;temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }

            if (seqTree.getNow().getChild() != null) {
                j = 0;
                for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                    float c_x = (temp.getX()+1) * LINE + LINE/2;
                    float c_y = (temp.getY()+1) * LINE - LINE/2;
                    addChild(c_x, c_y, j,temp.getText());
                    j++;
                }
            }

            EditText editText = parent.findViewById(R.id.editText);
            editText.setText(seqTree.getNow().getBoxText());
        }
    }

    public void delete() {
        if(sequence!=1) {

            int j = 0;
            for(SeqTree.Node temp = seqTree.getNow().getChild();temp != null;temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }
            seqTree.delete(seqTree.getNow());
            this.removeViewInLayout(findViewById(ids_stone[sequence - 2]));

            if (seqTree.getNow().getChild() != null) {
                j = 0;
                for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                    float c_x = (temp.getX()+1) * LINE + LINE/2;
                    float c_y = (temp.getY()+1) * LINE - LINE/2;
                    addChild(c_x, c_y, j, temp.getText());
                    j++;
                }
            }
            if(sequence!=2) {
                if (sequence % 2 == 0)
                    this.findViewById(ids_stone[sequence - 3]).setBackground(stoneDrawable(Color.WHITE,lastStoneStrokeColor));
                else
                    this.findViewById(ids_stone[sequence - 3]).setBackground(stoneDrawable(Color.BLACK,lastStoneStrokeColor));
            }

            sequence--;

            EditText editText = parent.findViewById(R.id.editText);
            editText.setText(seqTree.getNow().getBoxText());
        }
    }

    public void load() {
        if (seqTree.getHead().getChild() != null) {
            int j = 0;
            for (SeqTree.Node temp = seqTree.getHead().getChild(); temp != null; temp = temp.getNext()) {
                float c_x = (temp.getX()+1) * LINE + LINE/2;
                float c_y = (temp.getY()+1) * LINE - LINE/2;
                addChild(c_x, c_y, j,temp.getText());
                j++;
            }
        }
        seqTree.setNow(seqTree.getHead());
        seqTree.setNow_boardTo0();

        TextView seq = parent.findViewById(R.id.sequence);
        seq.setText("0");

        EditText editText = parent.findViewById(R.id.editText);
        if(seqTree.getText_box() != null) {
            seqTree.getNow().setBoxText(seqTree.getText_box());
        }
        editText.setText(seqTree.getNow().getBoxText());
    }

    public void newBoard() {
        int j = 0;

        for (SeqTree.Node temp = seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
            this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
            j++;
        }

        for(;sequence>=1;sequence--){
            this.removeViewInLayout(findViewById(ids_stone[sequence-1]));
        }
        sequence = 1;
        seqTree = new SeqTree();
        TextView seq = parent.findViewById(R.id.sequence);
        seq.setText("0");
        EditText editText = parent.findViewById(R.id.editText);
        editText.setText("");
        Toast.makeText(getContext(),"new file",Toast.LENGTH_SHORT).show();
    }

    public GradientDrawable stoneDrawable(int stoneColor, String strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(stoneColor);
        drawable.setStroke((int)(PX / 351.3 + 0.5),Color.parseColor(strokeColor));
        drawable.setShape(GradientDrawable.OVAL);
        return drawable;
    }

    // draw board line when user changed a line color
    public void drawLine(Canvas canvas) {
        super.draw(canvas);
        paint.setColor(Color.parseColor(lineColor));

        for(int i = 1; i <= 15; i++) {
            canvas.drawLine(LINE*i+LINE/2,LINE-LINE/2,LINE*i+LINE/2,LINE*15-LINE/2,paint);
            canvas.drawLine(LINE+LINE/2,LINE*i-LINE/2, LINE*15+LINE/2,LINE*i-LINE/2,paint);
        }
        canvas.drawCircle(LINE*8+LINE/2,LINE*8-LINE/2,(int)(PX / 150.5 + 0.5),paint);
        canvas.drawCircle(LINE*4+LINE/2,LINE*12-LINE/2,(int)(PX / 150.5 + 0.5),paint);
        canvas.drawCircle(LINE*12+LINE/2,LINE*12-LINE/2,(int)(PX / 150.5 + 0.5),paint);
        canvas.drawCircle(LINE*4+LINE/2,LINE*4-LINE/2,(int)(PX / 150.5 + 0.5),paint);
        canvas.drawCircle(LINE*12+LINE/2,LINE*4-LINE/2,(int)(PX / 150.5 + 0.5),paint);

        canvas.drawLine(LINE/2,LINE*15,LINE/2,LINE*16,paint);
        canvas.drawLine(0,LINE*15+LINE/2,LINE,LINE*15+LINE/2,paint);
    }
}
