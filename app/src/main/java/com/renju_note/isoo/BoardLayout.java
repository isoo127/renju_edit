package com.renju_note.isoo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
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
import androidx.core.graphics.ColorUtils;

import org.jetbrains.annotations.NotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardLayout extends ConstraintLayout {
    private float debug_x, debug_y;

    private Paint paint;
    private final int dp_margin = 10;
    public int px;
    private float line;
    private LinearLayout parent;
    int[] ids_coord_num;
    int[] ids_coord_char;
    int[] ids_stone;
    int[] ids_blank;
    boolean coord;
    int sequence;
    SeqTree seqTree;

    int mode = 0;

    String string;

    String boardColor;
    String lineColor = "#666666";
    boolean sequenceVisible;
    int num_minus;

    public void setParent(LinearLayout L) {
        parent = L;
    }

    public BoardLayout(@NonNull @NotNull Context context) {
        super(context);
        init(context);
    }

    public BoardLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        int margin = (int)(dp_margin * density() + 0.5);
        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        px = size.x;
        px = px - margin;

        paint = new Paint();
        paint.setStrokeWidth((int)(px / 351.3 + 0.5));

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

    @Override
    protected void onDraw(Canvas canvas) { // draw board
        super.onDraw(canvas);
        line = (float)(px)/16;

        for(int i = 1; i <= 15; i++) {
            canvas.drawLine(line*i+line/2,line-line/2,line*i+line/2,line*15-line/2,paint);
            canvas.drawLine(line+line/2,line*i-line/2, line*15+line/2,line*i-line/2,paint);
        }
        canvas.drawCircle(line*8+line/2,line*8-line/2,(int)(px / 150.5 + 0.5),paint);
        canvas.drawCircle(line*4+line/2,line*12-line/2,(int)(px / 150.5+ 0.5),paint);
        canvas.drawCircle(line*12+line/2,line*12-line/2,(int)(px / 150.5+ 0.5),paint);
        canvas.drawCircle(line*4+line/2,line*4-line/2,(int)(px / 150.5+ 0.5),paint);
        canvas.drawCircle(line*12+line/2,line*4-line/2,(int)(px / 150.5+ 0.5),paint);

        canvas.drawLine(line/2,line*15,line/2,line*16,paint);
        canvas.drawLine(0,line*15+line/2,line,line*15+line/2,paint);

        //addNumber((int)line,(int)line);
        //canvas.drawText("3",line,line,paint); // 7, 29
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        try {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if ((event.getX() + line / 2) % line <= line / 2)
                    x = event.getX() - ((event.getX() + line / 2) % line);
                else x = event.getX() - ((event.getX() + line / 2) % line) + line;

                if ((event.getY() - line / 2) % line <= line / 2)
                    y = event.getY() - ((event.getY() - line / 2) % line);
                else y = event.getY() - ((event.getY() - line / 2) % line) + line;
                // --------------------------------------------------------------------------- set x, y
                if ((int) ((x - line / 2) / line + 0.5) - 1 >= 0 && (int) ((y + line / 2) / line + 0.5) - 1 < 15) { // if x,y is in board
                    debug_x = (float)(((x - line / 2) / line));
                    debug_y = (float)(((y + line / 2) / line));
                    if (seqTree.getNow_board()[(int) ((x - line / 2) / line + 0.5) - 1][(int) ((y + line / 2) / line + 0.5) - 1] == 0) { // if x,y in board is no stone
                        //-----------------------------------------------------------------------

                        if (mode == 0) {
                            if (sequence != 1) {
                                if (sequence % 2 == 0)
                                    ((TextView) (this.findViewById(ids_stone[sequence - 2]))).setBackground(blackStoneDrawable());
                                else
                                    ((TextView) (this.findViewById(ids_stone[sequence - 2]))).setBackground(whiteStoneDrawable());
                            }
                            // ----------------------------------------------------------------------- change last move drawable
                            addNumber(x, y, sequence);
                            sequence++;

                            seqTree.next(seqTree.getNow(), (int) ((x - line / 2) / line + 0.5) - 1, (int) ((y + line / 2) / line + 0.5) - 1);

                            int j = 0;
                            for (SeqTree.Node temp = seqTree.getNow().getParent().getChlid(); temp != null; temp = temp.getNext()) {
                                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                                j++;
                            }

                            if (seqTree.getNow().getChlid() != null) {
                                j = 0;
                                for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                                    float c_x = (temp.getX() + 1) * line + line / 2;
                                    float c_y = (temp.getY() + 1) * line - line / 2;
                                    addChild(c_x, c_y, j, temp.getText());
                                    j++;
                                }
                            }
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
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    string = word.getText().toString();
                                    int c_x = (int) ((x - line / 2) / line + 0.5) - 1;
                                    int c_y = (int) ((y + line / 2) / line + 0.5) - 1;

                                    if (seqTree.getNow() == null) {
                                        seqTree.setNow(seqTree.getHead());
                                    }

                                    if (seqTree.getNow().getChlid() == null) {
                                        seqTree.createChild(seqTree.getNow(), c_x, c_y);
                                        seqTree.getNow().getChlid().setText(string);
                                        addChild(x, y, 0, seqTree.getNow().getChlid().getText());
                                    } else {
                                        int j = 0;
                                        for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
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
                                }
                            });
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });
                            alert.show();

                        }

                        //------------------------------------------------------------------------
                    }
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

            TextView debug = parent.findViewById(R.id.debug);
            debug.setText("developer mode\n"+" / "+String.valueOf(debug_x)+" / "+String.valueOf(debug_y)+" / "+String.valueOf(px)+" / "+String.valueOf(line));
        }
        TextView seq = parent.findViewById(R.id.sequence);
        seq.setText(String.valueOf(sequence-1));
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // set size of board by depending on the size of machine
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int margin = (int)(dp_margin * density() + 0.5);

        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        px = size.x;
        px = px - margin;

        setMeasuredDimension(px,px);
    }

    public void addCoordinate(int i, int mode) {
        LayoutParams size = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ConstraintSet constraintSet = new ConstraintSet();
        TextView num;

        if(mode == 0) {
            num = new TextView(getContext());
            num.setText(String.valueOf(i + 1));
            num.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(line/2.125/density()));
            num.setGravity(Gravity.CENTER);
            num.setId(generateViewId());
            num.setTextColor(Color.parseColor(lineColor));
            num.setTypeface(Typeface.MONOSPACE);
            ids_coord_num[i] = num.getId();

            this.addView(num,i,size);

            constraintSet.clone(this);
            constraintSet.connect(num.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int) (line * (15 - i) - (int)(8.5*density()) -line/2));
            constraintSet.connect(num.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int) (line*0.5 - (3.5*density()*num.getText().length()) ));
            constraintSet.applyTo(this);
        }
        else if(mode == 1) {
            num = new TextView(getContext());
            num.setText(String.valueOf((char)(i + 65)));
            num.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(line/2.125/density()));
            num.setGravity(Gravity.CENTER);
            num.setId(generateViewId());
            num.setTextColor(Color.parseColor(lineColor));
            num.setTypeface(Typeface.MONOSPACE);
            ids_coord_char[i] = num.getId();

            this.addView(num,i,size);

            constraintSet.clone(this);
            constraintSet.connect(num.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int) (line*15.5 - (int)(8.5*density()) ));
            constraintSet.connect(num.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int) (line*(i+1) - (3.5*density()*num.getText().length()) +line/2));
            constraintSet.applyTo(this);
        }
        coord = true;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return generateViewIdSdk17Under();
        } else {
            return View.generateViewId();
        }
    }

    private static int generateViewIdSdk17Under() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1;
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    private float density() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    private void addNumber(float x,float y,int index) {
        LayoutParams size = new LayoutParams((int)(line-2),(int)(line-2));
        ConstraintSet constraintSet = new ConstraintSet();
        TextView seq = new TextView(getContext());

        if(sequenceVisible == true) {
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
        seq.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(line/2.125/density()));
        seq.setGravity(Gravity.CENTER);
        seq.setId(generateViewId());
        ids_stone[index-1] = seq.getId();

        if(index%2 == 0) seq.setBackgroundResource(R.drawable.last_white_stone);
        else seq.setBackgroundResource(R.drawable.last_black_stone);

        this.addView(seq,index,size);

        constraintSet.clone(this);
        constraintSet.connect(seq.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int)y - (int)(line/2 - 1));
        constraintSet.connect(seq.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int)x - (int)(line/2 - 1));
        constraintSet.applyTo(this);
    }

    private void addChild(float x,float y,int index, String string) {
        LayoutParams size = new LayoutParams((int)(line-2),(int)(line-2));
        ConstraintSet constraintSet = new ConstraintSet();
        TextView seq = new TextView(getContext());

        seq.setText(string);
        seq.setLines(1);
        seq.setTextColor(Color.parseColor("#1D40FF"));
        seq.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        seq.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(line/2.125/density()));
        seq.setGravity(Gravity.CENTER);
        seq.setId(generateViewId());
        ids_blank[index] = seq.getId();

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(boardColor));
        drawable.setStroke((int)(px / 351.3 + 0.5),Color.parseColor(lineColor));
        drawable.setShape(GradientDrawable.OVAL);

        seq.setBackground(drawable);

        this.addView(seq,index,size);

        constraintSet.clone(this);
        constraintSet.connect(seq.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, (int)y - (int)(line/2 - 1));
        constraintSet.connect(seq.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT, (int)x - (int)(line/2 - 1));
        constraintSet.applyTo(this);
    }

    public void undo() {
        if(sequence!=1) {

            int j = 0;
            for(SeqTree.Node temp = seqTree.getNow().getChlid();temp != null;temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }
            seqTree.undo(seqTree.getNow());
            this.removeViewInLayout(findViewById(ids_stone[sequence - 2]));

            if (seqTree.getNow().getChlid() != null) {
                j = 0;
                for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                    float c_x = (temp.getX()+1) * line + line/2;
                    float c_y = (temp.getY()+1) * line - line/2;
                    addChild(c_x, c_y, j,temp.getText());
                    j++;
                }
            }
            if(sequence!=2) {
                if (sequence % 2 == 0)
                    ((TextView) (this.findViewById(ids_stone[sequence - 3]))).setBackgroundResource(R.drawable.last_white_stone);
                else
                    ((TextView) (this.findViewById(ids_stone[sequence - 3]))).setBackgroundResource(R.drawable.last_black_stone);
            }

            sequence--;


        }
    }

    public void redo() {
        if(seqTree.getNow() != null) {
            if (seqTree.getNow().getChlid() != null && seqTree.getNow().getChlid().getNext() == null) {
                seqTree.redo(seqTree.getNow());
                if (sequence != 1) {
                    if (sequence % 2 == 0)
                        ((TextView) (this.findViewById(ids_stone[sequence - 2]))).setBackground(blackStoneDrawable());
                    else
                        ((TextView) (this.findViewById(ids_stone[sequence - 2]))).setBackground(whiteStoneDrawable());
                }
                // ----------------------------------------------------------------------- change last move drawable
                float x = (seqTree.getNow().getX()+1) * line + line/2;
                float y = (seqTree.getNow().getY()+1) * line - line/2;
                addNumber(x, y, sequence);
                sequence++;

                int j = 0;
                for(SeqTree.Node temp = seqTree.getNow().getParent().getChlid();temp != null;temp = temp.getNext()) {
                    this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                    j++;
                }

                if (seqTree.getNow().getChlid() != null) {
                    j = 0;
                    for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                        float c_x = (temp.getX()+1) * line + line/2;
                        float c_y = (temp.getY()+1) * line - line/2;
                        addChild(c_x, c_y, j,temp.getText());
                        j++;
                    }
                }
            }
        }
    }

    public void delete() {
        if(sequence!=1) {

            int j = 0;
            for(SeqTree.Node temp = seqTree.getNow().getChlid();temp != null;temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }
            seqTree.delete(seqTree.getNow());
            this.removeViewInLayout(findViewById(ids_stone[sequence - 2]));

            if (seqTree.getNow().getChlid() != null) {
                j = 0;
                for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                    float c_x = (temp.getX()+1) * line + line/2;
                    float c_y = (temp.getY()+1) * line - line/2;
                    addChild(c_x, c_y, j, temp.getText());
                    j++;
                }
            }
            if(sequence!=2) {
                if (sequence % 2 == 0)
                    ((TextView) (this.findViewById(ids_stone[sequence - 3]))).setBackgroundResource(R.drawable.last_white_stone);
                else
                    ((TextView) (this.findViewById(ids_stone[sequence - 3]))).setBackgroundResource(R.drawable.last_black_stone);
            }

            sequence--;

        }
    }

    public void save() {
        EditText editText = parent.findViewById(R.id.editText);
        seqTree.setText_box(editText.getText().toString());
//        try {
//            FileOutputStream fos = getContext().openFileOutput("renju", Context.MODE_PRIVATE);
//            ObjectOutputStream os = new ObjectOutputStream(fos);
//            os.writeObject(this.seqTree);
//            os.close();
//            fos.close();
//        } catch (Exception e){
//            Log.e("save", "save: ",e);
//        }
    }

    public void load() {
//        try{
//            FileInputStream fis = getContext().openFileInput("renju");
//            ObjectInputStream is = new ObjectInputStream(fis);
//            seqTree = (SeqTree)is.readObject();
//            is.close();
//            fis.close();
//        } catch (Exception e) {
//            Log.e("load", "load: ",e);
//        }

        if (seqTree.getHead().getChlid() != null) {
            int j = 0;
            for (SeqTree.Node temp = seqTree.getHead().getChlid(); temp != null; temp = temp.getNext()) {
                float c_x = (temp.getX()+1) * line + line/2;
                float c_y = (temp.getY()+1) * line - line/2;
                addChild(c_x, c_y, j,temp.getText());
                j++;
            }
        }
        seqTree.setNow(seqTree.getHead());
        seqTree.setNow_boardTo0();
        TextView seq = parent.findViewById(R.id.sequence);
        seq.setText("0");
        EditText editText = parent.findViewById(R.id.editText);
        editText.setText(seqTree.getText_box());
    }

    public void newBoard() {
        int j = 0;
        if(seqTree.getNow() != null) {
            for (SeqTree.Node temp = seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                this.removeViewInLayout(findViewById(ids_blank[j])); // remove blank
                j++;
            }
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

    public GradientDrawable blackStoneDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#000000"));
        drawable.setStroke((int)(px / 351.3 + 0.5),Color.parseColor(lineColor));
        drawable.setShape(GradientDrawable.OVAL);
        return drawable;
    }

    public GradientDrawable whiteStoneDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#FFFFFF"));
        drawable.setStroke((int)(px / 351.3 + 0.5),Color.parseColor(lineColor));
        drawable.setShape(GradientDrawable.OVAL);
        return drawable;
    }

    public void drawLine(Canvas canvas) {
        super.draw(canvas);
        paint.setColor(Color.parseColor(lineColor));
        line = (float)(px)/16;

        for(int i = 1; i <= 15; i++) {
            canvas.drawLine(line*i+line/2,line-line/2,line*i+line/2,line*15-line/2,paint);
            canvas.drawLine(line+line/2,line*i-line/2, line*15+line/2,line*i-line/2,paint);
        }
        canvas.drawCircle(line*8+line/2,line*8-line/2,7,paint);
        canvas.drawCircle(line*4+line/2,line*12-line/2,7,paint);
        canvas.drawCircle(line*12+line/2,line*12-line/2,7,paint);
        canvas.drawCircle(line*4+line/2,line*4-line/2,7,paint);
        canvas.drawCircle(line*12+line/2,line*4-line/2,7,paint);

        canvas.drawLine(line/2,line*15,line/2,line*16,paint);
        canvas.drawLine(0,line*15+line/2,line,line*15+line/2,paint);
    }
}
