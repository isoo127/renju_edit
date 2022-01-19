package com.renju_note.isoo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private float DENSITY;

    // for permission to process the storage
    private static final String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    /*
    for add coordinate in board
    CHandler addCoordinate : if get msg, add coordinate to board
    Cthread thread : send msg to addCoordinate
     */
    private CHandler addCoordinateHandler;

    class CHandler extends Handler {
        public CHandler(Looper myLooper) {
            super(myLooper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            for(int i=0;i<15;i++) {
                if(board.coord = true) {
                    board.removeViewInLayout(findViewById(board.ids_coord_char[i]));
                    board.removeViewInLayout(findViewById(board.ids_coord_num[i]));
                }
                board.addCoordinate(i,0);
                board.addCoordinate(i,1);
            }
        }
    }

    class CThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(500);
                Message message = new Message();
                addCoordinateHandler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // the main board
    private BoardLayout board;

    // the text box that located on the board
    private EditText editText;

    /*
    for saving basic settings of this application
    SharedPreferences sf : load settings
    SharedPreferences.Editor editor : save settings
     */
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    //part of basic settings
    public String editTextColor;
    public boolean editTextVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifystoragepermissions(this);

        sf = getSharedPreferences("MainData", Activity.MODE_PRIVATE);
        editor = sf.edit();
        editor.apply();

        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // save boxText to seqTree
                board.seqTree.getNow().setBoxText(s.toString());
            }
        });

        board = findViewById(R.id.board);
        board.setParent(findViewById(R.id.main));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DENSITY = density();
    }

    /*
    method that runs on the toolbar
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

                builder.setTitle("Screenshot");
                builder.setMessage("Are you sure you want to capture this board?");

                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    LinearLayout capture = findViewById(R.id.capture);
                    capture.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(capture.getDrawingCache());
                    capture.setDrawingCacheEnabled(false);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        long time = System.currentTimeMillis();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat day = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                        String output = day.format(new Date(time));

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "renju"+output+".jpeg");
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);

                        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                        final Uri item1 = getContentResolver().insert(collection, values);

                        try {
                            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(item1, "w", null);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            byte[] bitmapData = bytes.toByteArray();
                            InputStream inputStream = new ByteArrayInputStream(bitmapData);

                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            int len;
                            while ((len = inputStream.read(buffer)) != -1) {
                                byteBuffer.write(buffer, 0, len);
                            }

                            byte[] str2Byte = byteBuffer.toByteArray();

                            FileOutputStream outputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
                            outputStream.write(str2Byte);
                            outputStream.close();
                            inputStream.close();
                            fileDescriptor.close();
                            getContentResolver().update(item1, values, null, null);
                            Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            values.clear();
                            values.put(MediaStore.Images.Media.IS_PENDING, 0);
                            getContentResolver().update(item1, values, null, null);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "failed to update gallery", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            long time = System.currentTimeMillis();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat day = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                            String output = day.format(new Date(time));
                            String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Screenshots";
                            String file = folder + File.separator + output + ".jpeg";

                            File FolderPath = new File(folder);
                            if (!FolderPath.exists()) {
                                boolean success = FolderPath.mkdirs();
                                if(!success)
                                    System.out.println("failed mkdirs");
                            }

                            OutputStream outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();
                            outputStream.close();
                            getApplicationContext().sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,  Uri.parse("file://"+file)));
                            Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                builder.setNegativeButton("No", (dialogInterface, i) -> {

                });

                builder.show();
                return true ;
            case R.id.new_board:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

                builder1.setTitle("New board");
                builder1.setMessage("Are you sure you want to load a new board?");

                builder1.setPositiveButton("Yes", (dialogInterface, i) -> board.newBoard());

                builder1.setNegativeButton("No", (dialogInterface, i) -> {

                });

                builder1.show();
                return true ;
            case R.id.save_board :
                save();
                return true ;
            case R.id.load_board:
                load();
                return true;
            case R.id.setting:
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivityResultSet.launch(intent);
                return true;
            case R.id.about:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);
                builder2.setTitle("About Renju Edit");
                builder2.setMessage("Developer : isoo (Kang Sang-Min)\n\n" +
                        "Design Advice : jimflower\n\n" +
                        "Special thanks for nuguri, buddy\n" +
                        "August 20th, 2021");
                builder2.show();
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    method that gets item's information to toolbar
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_action, menu);
        return true;
    }

    /*
    method that automatically gets called when the screen is on
     */
    @Override
    protected void onResume() {
        super.onResume();
        setRatio();
        setSetting();
        addCoordinateHandler = new CHandler(Looper.getMainLooper());
        CThread addCoordinateThread = new CThread();
        addCoordinateThread.start();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> finish());

        builder.setNegativeButton("No", (dialogInterface, i) -> {

        });

        builder.show();
    }

    public void backClicked(View v) {
        editText.setCursorVisible(false);
        board.undo();
        TextView seq = findViewById(R.id.sequence);
        seq.setText(String.valueOf(board.sequence-1));
    }

    public void redoClicked(View v) {
        editText.setCursorVisible(false);
        board.redo();
        TextView seq = findViewById(R.id.sequence);
        seq.setText(String.valueOf(board.sequence-1));
    }

    public void backAllClicked(View v) {
        editText.setCursorVisible(false);
        if(board.seqTree.getNow() != null) {
            if(board.seqTree.getNow().getChild() == null) {
                board.undo();
            } else if (board.seqTree.getNow().getChild().getNext() != null) {
                board.undo();
            }
            while (board.seqTree.getNow() != board.seqTree.getHead() && board.seqTree.getNow().getChild().getNext() == null) {
                board.undo();
            }
        }
        TextView seq = findViewById(R.id.sequence);
        seq.setText(String.valueOf(board.sequence-1));
    }

    public void redoAllClicked(View v) {
        editText.setCursorVisible(false);
        if(board.seqTree.getNow() != null) {
                while (board.seqTree.getNow().getChild() != null && board.seqTree.getNow().getChild().getNext() == null) {
                    board.redo();
                }
        }
        TextView seq = findViewById(R.id.sequence);
        seq.setText(String.valueOf(board.sequence-1));
    }

    public void deleteClicked(View v) {
        editText.setCursorVisible(false);
        if(board.sequence != 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

            builder.setTitle("Delete");
            builder.setMessage("Are you sure you want to delete this move?");

            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                board.delete();
                TextView seq = findViewById(R.id.sequence);
                seq.setText(String.valueOf(board.sequence - 1));
            });

            builder.setNegativeButton("No", (dialogInterface, i) -> {
            });

            builder.show();
        }
    }

    public void modeClicked(View v) {
        editText.setCursorVisible(false);
        ImageButton button = findViewById(R.id.mode_button);
        if(board.mode == 0) {
            button.setImageResource(R.drawable.text_mode);
            board.mode = 1;
        } else if(board.mode == 1) {
            button.setImageResource(R.drawable.stone_mode);
            board.mode = 0;
        }
    }

    public void editTextClicked(View v) {
        editText.setCursorVisible(true);
    }

    public void mainClicked(View v) {
        editText.setCursorVisible(false);
    }

    public void boardClicked(View v) {
        editText.setCursorVisible(false);
    }

    public void toolbarClicked(View v) { editText.setCursorVisible(false); }

    public void verifystoragepermissions(Activity activity) {
        //int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If storage permission is not given then request for External Storage Permission
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            //Intent intent= new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            //startActivity(intent);
            ActivityCompat.requestPermissions(activity, permissionstorage, 1);
        }
    }

    private void save() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        intent.putExtra(Intent.EXTRA_TITLE,"write_your_file_name");

        startActivityResultSave.launch(intent);
    }

    private void load() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");

        startActivityResultLoad.launch(intent);
    }

    private void saveSetting() {
        editor.putString("boardColor", board.boardColor);
        editor.putString("editTextColor",editTextColor);
        editor.putString("lineColor", board.lineColor);
        editor.putBoolean("editTextVisible",editTextVisible);
        editor.putBoolean("sequenceVisible",board.sequenceVisible);
        editor.putInt("num_minus",board.num_minus);
        editor.apply();
    }

    private void setSetting() {
        board.boardColor = sf.getString("boardColor","#F2CA94");
        editTextColor = sf.getString("editTextColor","#99BBFF");
        board.lineColor = sf.getString("lineColor","#666666");
        editTextVisible = sf.getBoolean("editTextVisible",true);
        board.sequenceVisible = sf.getBoolean("sequenceVisible",true);
        board.num_minus = sf.getInt("num_minus",0);

        board.setBackgroundColor(Color.parseColor(board.boardColor));
        board.drawLine(new Canvas());

        if(ColorUtils.calculateContrast(Color.parseColor(editTextColor),Color.WHITE) < 1.5f) {
            editText.setBackgroundColor(Color.parseColor(editTextColor));
            editText.setBackground(makeDrawable(editTextColor));
            editText.setTextColor(Color.BLACK);
        } else if(ColorUtils.calculateContrast(Color.parseColor(editTextColor),Color.BLACK) < 1.5f) {
            editText.setBackgroundColor(Color.parseColor(editTextColor));
            editText.setTextColor(Color.WHITE);
        } else {
            editText.setBackgroundColor(Color.parseColor(editTextColor));
            editText.setTextColor(Color.BLACK);
        }

        if(editTextVisible) {
            editText.setVisibility(View.VISIBLE);
        } else {
            editText.setVisibility(View.GONE);
        }
    }

    // make editText drawable
    private GradientDrawable makeDrawable(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setStroke((int)(board.PX / 351.3 + 0.5),Color.parseColor(board.lineColor));
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setSize((int)(38*DENSITY),(int)(38*DENSITY));
        return drawable;
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

        LinearLayout.LayoutParams layoutParamsET = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(size.x / 9.5));
        layoutParamsET.topMargin = (int)(5*DENSITY+0.5);
        layoutParamsET.rightMargin = (int)(5*DENSITY+0.5);
        layoutParamsET.leftMargin = (int)(5*DENSITY+0.5);
        editText.setLayoutParams(layoutParamsET);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 27.4 / DENSITY + 0.5));

        Button b1 = findViewById(R.id.undo_all_button);
        Button b2 = findViewById(R.id.undo_button);
        Button b3 = findViewById(R.id.redo_all_button);
        Button b4 = findViewById(R.id.redo_button);
        ImageButton b5 = findViewById(R.id.delete_button);
        ImageButton b6 = findViewById(R.id.mode_button);
        TextView t = findViewById(R.id.sequence);

        LinearLayout.LayoutParams buttonLayoutParam = new LinearLayout.LayoutParams(size.x * 5 / 32,(int)(size.x / 8.2));
        b1.setLayoutParams(buttonLayoutParam);
        b2.setLayoutParams(buttonLayoutParam);
        b3.setLayoutParams(buttonLayoutParam);
        b4.setLayoutParams(buttonLayoutParam);
        b5.setLayoutParams(buttonLayoutParam);
        b6.setLayoutParams(buttonLayoutParam);
        t.setLayoutParams(buttonLayoutParam);

        int textSize = (int)(size.x / 22.8 / DENSITY + 0.5);
        b1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        b2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        b3.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        b4.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
        t.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);

        //Toast.makeText(getApplicationContext(),String.valueOf(DENSITY)+" / "+String.valueOf(size.x),Toast.LENGTH_LONG).show();
    }

    private float density() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    ActivityResultLauncher<Intent> startActivityResultSave = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri;
                        if (result.getData() != null) {
                            uri = result.getData().getData();
                            try {
                                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                                os.writeObject(board.seqTree);
                                os.close();
                                outputStream.close();
                                Toast.makeText(getApplicationContext(),"save",Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(),"failed to save!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> startActivityResultLoad = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        int j = 0;
                        if(board.seqTree.getNow() != null) {
                            for (SeqTree.Node temp = board.seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                                board.removeViewInLayout(findViewById(board.ids_blank[j])); // remove blank
                                j++;
                            }
                        }
                        for(;board.sequence>=1;board.sequence--){
                            board.removeViewInLayout(findViewById(board.ids_stone[board.sequence-1]));
                        }
                        board.sequence = 1;

                        Uri uri;
                        if (result.getData() != null) {
                            uri = result.getData().getData();
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                ObjectInputStream is = new ObjectInputStream(inputStream);
                                board.seqTree = (SeqTree) is.readObject();
                                is.close();
                                inputStream.close();
                                Toast.makeText(getApplicationContext(),"load",Toast.LENGTH_SHORT).show();
                                board.load();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(),"failed to load!",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                board.load();
                            } catch (ClassNotFoundException ignored) {

                            }
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> startActivityResultSet = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        assert data != null;
                        board.boardColor = data.getStringExtra("boardColor");
                        editTextColor = data.getStringExtra("editTextColor");
                        board.lineColor = data.getStringExtra("lineColor");
                        editTextVisible = data.getBooleanExtra("editTextVisible", true);
                        board.sequenceVisible = data.getBooleanExtra("sequenceVisible",true);
                        board.num_minus = data.getIntExtra("num_minus",0);

                        for(int i=0;i<board.sequence-1;i++) {
                            if(board.sequenceVisible) {
                                if (i + 1 - board.num_minus > 0) {
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setText(String.valueOf(i + 1 - board.num_minus));
                                } else {
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setText("");
                                }
                            } else {
                                ((TextView) (board.findViewById(board.ids_stone[i]))).setText("");
                            }
                        }

                        board.drawLine(new Canvas());

                        for(int i=0;i<board.sequence-1;i++) {
                            if(i%2 == 0) {
                                if(i == board.sequence-2) {
                                    board.findViewById(board.ids_stone[i]).setBackground(board.stoneDrawable(Color.BLACK, board.lastStoneStrokeColor));
                                } else {
                                    board.findViewById(board.ids_stone[i]).setBackground(board.stoneDrawable(Color.BLACK, board.lineColor));
                                }
                            } else {
                                if(i == board.sequence-2) {
                                    board.findViewById(board.ids_stone[i]).setBackground(board.stoneDrawable(Color.WHITE, board.lastStoneStrokeColor));
                                } else {
                                    board.findViewById(board.ids_stone[i]).setBackground(board.stoneDrawable(Color.WHITE, board.lineColor));
                                }
                            }
                        }

                        if(board.seqTree.getNow() != null) {
                            if (board.seqTree.getNow().getChild() != null) {
                                int j = 0;
                                for (SeqTree.Node temp = board.seqTree.getNow().getChild(); temp != null; temp = temp.getNext()) {
                                    board.findViewById(board.ids_blank[j]).setBackground(board.stoneDrawable(Color.parseColor(board.boardColor), board.lineColor));
                                    j++;
                                }
                            }
                        }

                        saveSetting();
                        setSetting();
                    }
                }
            }
    );
}
