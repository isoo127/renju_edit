package com.renju_note.isoo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}; // for permission
    CHandler addCoordinate; // for add coordinate
    Cthread thread; // for add coordinate
    BoardLayout board; // for add coordinate

    private EditText editText;

    SharedPreferences sf;
    SharedPreferences.Editor editor;

    String editTextColor;
    boolean editTextVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifystoragepermissions(this);

        sf = getSharedPreferences("MainData", Activity.MODE_PRIVATE);
        editor = sf.edit();

        editText = findViewById(R.id.editText);

        board = findViewById(R.id.board);
        board.setParent((LinearLayout)(findViewById(R.id.main)));

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

                builder.setTitle("Screenshot");
                builder.setMessage("Are you sure you want to capture this board?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LinearLayout capture = findViewById(R.id.capture);
                        capture.setDrawingCacheEnabled(true);
                        Bitmap bitmap = Bitmap.createBitmap(capture.getDrawingCache());
                        capture.setDrawingCacheEnabled(false);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            long time = System.currentTimeMillis();
                            SimpleDateFormat day = new SimpleDateFormat("yyyymmddhhmmssSSS");
                            String output = day.format(new Date(time));

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DISPLAY_NAME, "renju"+output+".jpeg");
                            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                            values.put(MediaStore.Images.Media.IS_PENDING, 1);

                            final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                            final Uri item = getContentResolver().insert(collection, values);

                            try {
                                ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(item, "w", null);
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                byte[] bitmapData = bytes.toByteArray();
                                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
                                InputStream inputStream = bs;

                                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len = 0;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    byteBuffer.write(buffer, 0, len);
                                }

                                byte[] str2Byte = byteBuffer.toByteArray();

                                FileOutputStream outputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
                                outputStream.write(str2Byte);
                                outputStream.close();
                                inputStream.close();
                                fileDescriptor.close();
                                getContentResolver().update(item, values, null, null);
                                Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                values.clear();
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                getContentResolver().update(item, values, null, null);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "failed to update gallery", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                long time = System.currentTimeMillis();
                                SimpleDateFormat day = new SimpleDateFormat("yyyymmddhhmmssSSS");
                                String output = day.format(new Date(time));
                                String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Screenshots";
                                String file = folder + File.separator + output + ".jpeg";

                                File FolderPath = new File(folder);
                                if (!FolderPath.exists()) {
                                    FolderPath.mkdirs();
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
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.show();
                return true ;
            case R.id.new_board:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

                builder1.setTitle("New board");
                builder1.setMessage("Are you sure you want to load a new board?");

                builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        board.newBoard();
                    }
                });

                builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder1.show();
                return true ;
            case R.id.save_board :
                board.save();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_action, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRatio();
        setSetting();
        //Handler addCoordinate = new Handler();
        addCoordinate = new CHandler();
        thread = new Cthread();
        thread.start();
    }

    class CHandler extends Handler {
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
            //board.removeViewInLayout();
            //((TextView)board.findViewById(board.ids_coord_char[1])).setBackgroundResource(R.drawable.last_white_stone);
        }
    }

    class Cthread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(500);
                Message message = new Message();
                addCoordinate.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme);

        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
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
            if(board.seqTree.getNow().getChlid() == null) {
                board.undo();
            } else if (board.seqTree.getNow().getChlid().getNext() != null) {
                board.undo();
            }
            while (board.seqTree.getNow() != board.seqTree.getHead() && board.seqTree.getNow().getChlid().getNext() == null) {
                board.undo();
            }
        }
        TextView seq = findViewById(R.id.sequence);
        seq.setText(String.valueOf(board.sequence-1));
    }

    public void redoAllClicked(View v) {
        editText.setCursorVisible(false);
        if(board.seqTree.getNow() != null) {
                while (board.seqTree.getNow().getChlid() != null && board.seqTree.getNow().getChlid().getNext() == null) {
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

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    board.delete();
                    TextView seq = findViewById(R.id.sequence);
                    seq.setText(String.valueOf(board.sequence - 1));
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
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

        if(editTextVisible == true) {
            editText.setVisibility(View.VISIBLE);
        } else {
            editText.setVisibility(View.GONE);
        }
    }

    private GradientDrawable makeDrawable(String color) {
        GradientDrawable drawable1 = new GradientDrawable();
        drawable1.setColor(Color.parseColor(color));
        drawable1.setStroke((int)(board.px / 351.3+0.5),Color.parseColor(board.lineColor));
        drawable1.setShape(GradientDrawable.RECTANGLE);
        drawable1.setSize((int)(38*density()),(int)(38*density()));
        return drawable1;
    }

    private void setRatio() {
        WindowManager windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LinearLayout.LayoutParams layoutParamsET = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(size.x / 9.5));
        layoutParamsET.topMargin = (int)(5*density()+0.5);
        layoutParamsET.rightMargin = (int)(5*density()+0.5);
        layoutParamsET.leftMargin = (int)(5*density()+0.5);
        editText.setLayoutParams(layoutParamsET);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 27.4 / density() + 0.5));

        Button b1 = findViewById(R.id.undo_all_button);
        Button b2 = findViewById(R.id.undo_button);
        Button b3 = findViewById(R.id.redo_all_button);
        Button b4 = findViewById(R.id.redo_button);
        ImageButton b5 = findViewById(R.id.delete_button);
        ImageButton b6 = findViewById(R.id.mode_button);
        TextView t = findViewById(R.id.sequence);

        b1.setLayoutParams(new LinearLayout.LayoutParams(size.x * 5 / 32,(int)(size.x / 8.2)));
        b2.setLayoutParams(new LinearLayout.LayoutParams(size.x * 5 / 32,(int)(size.x / 8.2)));
        b3.setLayoutParams(new LinearLayout.LayoutParams(size.x * 5 / 32,(int)(size.x / 8.2)));
        b4.setLayoutParams(new LinearLayout.LayoutParams(size.x * 5 / 32,(int)(size.x / 8.2)));
        b5.setLayoutParams(new LinearLayout.LayoutParams(size.x * 4 / 32,(int)(size.x / 8.2)));
        b6.setLayoutParams(new LinearLayout.LayoutParams(size.x * 4 / 32,(int)(size.x / 8.2)));
        t.setLayoutParams(new LinearLayout.LayoutParams(size.x * 4 / 32,(int)(size.x / 8.2)));
        b1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 22.8 / density() + 0.5));
        b2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 22.8 / density() + 0.5));
        b3.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 22.8 / density() + 0.5));
        b4.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 22.8 / density() + 0.5));
        t.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(int)(size.x / 22.8 / density() + 0.5));

        //Toast.makeText(getApplicationContext(),String.valueOf(density())+" / "+String.valueOf(size.x),Toast.LENGTH_LONG).show();
    }

    private float density() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    ActivityResultLauncher startActivityResultSave = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = null;
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

    ActivityResultLauncher startActivityResultLoad = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        int j = 0;
                        if(board.seqTree.getNow() != null) {
                            for (SeqTree.Node temp = board.seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                                board.removeViewInLayout(findViewById(board.ids_blank[j])); // remove blank
                                j++;
                            }
                        }
                        for(;board.sequence>=1;board.sequence--){
                            board.removeViewInLayout(findViewById(board.ids_stone[board.sequence-1]));
                        }
                        board.sequence = 1;

                        Uri uri = null;
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
                                board.load();
                            } catch (ClassNotFoundException e) {

                            }
                        }
                    }
                }
            }
    );

    ActivityResultLauncher startActivityResultSet = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        board.boardColor = data.getStringExtra("boardColor");
                        editTextColor = data.getStringExtra("editTextColor");
                        board.lineColor = data.getStringExtra("lineColor");
                        editTextVisible = data.getBooleanExtra("editTextVisible", true);
                        board.sequenceVisible = data.getBooleanExtra("sequenceVisible",true);
                        board.num_minus = data.getIntExtra("num_minus",0);

                        for(int i=0;i<board.sequence-1;i++) {
                            if(board.sequenceVisible == true) {
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
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setBackgroundResource(R.drawable.last_black_stone);
                                } else {
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setBackground(board.blackStoneDrawable());
                                }
                            } else {
                                if(i == board.sequence-2) {
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setBackgroundResource(R.drawable.last_white_stone);
                                } else {
                                    ((TextView) (board.findViewById(board.ids_stone[i]))).setBackground(board.whiteStoneDrawable());
                                }
                            }
                        }

                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setColor(Color.parseColor(board.boardColor));
                        drawable.setStroke((int)(board.px / 351.3+0.5),Color.parseColor(board.lineColor));
                        drawable.setShape(GradientDrawable.OVAL);
                        if(board.seqTree.getNow() != null) {
                            if (board.seqTree.getNow().getChlid() != null) {
                                int j = 0;
                                for (SeqTree.Node temp = board.seqTree.getNow().getChlid(); temp != null; temp = temp.getNext()) {
                                    ((TextView) board.findViewById(board.ids_blank[j])).setBackground(drawable);
                                    j++;
                                }
                            }
                        }

                        saveSetting();
                        setSetting();
                        //Toast.makeText(getApplicationContext(),"dd",Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
}
