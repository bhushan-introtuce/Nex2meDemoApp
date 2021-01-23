package co.introtuce.nex2me.test;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.an.deviceinfo.device.model.Battery;
import com.an.deviceinfo.permission.PermissionManager;
import com.an.deviceinfo.permission.PermissionUtils;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.UUID;

import co.introtuce.nex2me.test.ui.CollingAct;

public class ModelTestActivity extends AppCompatActivity implements PermissionManager.PermissionCallback
        , View.OnClickListener, ModelEventListioner, CollingEventListioner {

    public static final String[] models = {
            "", "small_fp16.binarypb", "small_fp32.binarypb", // Small model graphs
            "medium_fp16.binarypb", "medium_fp32.binarypb", // Medium Model graph
            "large_fp16.binarypb", "large_fp32.binarypb"// Lare models graph
    };

//    public static int initTIme = 3000;
//
//    public static final int[] running = {
//            1, 3, 5, 10, 15
//    };
//
//    public static final int[] cooling = {
//            0, 1, 3, 5
//    };

    private static final String TAG = "ModelTestAct";
   // private FrameLayout container;
    private PermissionManager permissionManager;
    private PermissionUtils permissionUtils;
//    private ModelTestFragment fragment;
//    private PowerSpinnerView modelsMenu, runtimeMenu, coolingMenu;
//    private TextView controlButton;
//    private int modelOption = 0, runtimeOption = 0, coolingOption = 0;

    //For preferences
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "co.introtuce.nex2me.test";

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        try {
            System.loadLibrary("opencv_java3");
        } catch (java.lang.UnsatisfiedLinkError e) {
            // Some example apps (e.g. template matching) require OpenCV 4.
            System.loadLibrary("opencv_java4");
        }
    }

    private Dialog alertBox;


    TextView test1, test2, test3, allTests;
    private String testid;
    private Thread thread;
    private int m_run_time;
    private int m_cool_time;
    private String m_graph_name;
    private boolean is_test_running = false;
    private int test_count = 0;
    private Fragment oldFrag;
    private Fragment old_cooling_Frag;
    int c_test_no = 1;
    ImageView ivMessage;


    @Override
    protected void onResume() {
        super.onResume();
        askPermission();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_test);

        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);
//
//        if (mPreferences.getBoolean("firstrun", true)) {
//            updateUUid();
//           // mPreferences.edit().putBoolean("firstrun", false).commit();
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        test1 = findViewById(R.id.btn_test_1);
        test2 = findViewById(R.id.btn_test_2);
        test3 = findViewById(R.id.btn_test_3);
        //container = findViewById(R.id.container);
        allTests = findViewById(R.id.btn_all_tests);
        ivMessage = findViewById(R.id.iv_info);

        ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertBox = new Dialog(ModelTestActivity.this);
                alertBox.setContentView(R.layout.custom_message_box);
                TextView tvOk = alertBox.findViewById(R.id.tv_ok_alert);
                DisplayMetrics displayMetrics = new DisplayMetrics();
               getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                alertBox.getWindow().setLayout(width - 20, ViewGroup.LayoutParams.WRAP_CONTENT);
                alertBox.show();
                tvOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertBox.dismiss();

                    }
                });
            }
        });
        //time_remaning = findViewById(R.id.time_remaning);

//        test1.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                if (test1.getText().toString().equalsIgnoreCase("Stop")) {
//                    showInitialView("message");
//                    testid = "Test 2 >>" + UUID.randomUUID().toString();
//                } else {
//                    startFragment(models[2]);
//                    testid = "Test 2 >>" + UUID.randomUUID().toString();
//                    test1.setText("Stop");
//                }
//            }
//        });


        test1.setOnClickListener(this);
        test2.setOnClickListener(this);
        test3.setOnClickListener(this);
        allTests.setOnClickListener(this);

//        modelsMenu = findViewById(R.id.models);
//        controlButton = findViewById(R.id.control);
//        controlButton.setOnClickListener(this);
//        controlButton.setVisibility(View.GONE);
//        modelsMenu.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
//            @Override
//            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
//                //toast(item + " selected!");
//                Log.d(TAG, "Selected " + newItem);
//                modelOption = newIndex;
//            }
//        });
//
//
//        coolingMenu = findViewById(R.id.cooling);
//        coolingMenu.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
//            @Override
//            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
//                //toast(item + " selected!");
//                Log.d(TAG, "Selected " + newItem);
//                coolingOption = newIndex;
//            }
//        });
//
//        runtimeMenu = findViewById(R.id.runtime);
//        runtimeMenu.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
//            @Override
//            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
//                //toast(item + " selected!");
//                Log.d(TAG, "Selected " + newItem);
//                runtimeOption = newIndex;
//            }
//        });

        permissionManager = new PermissionManager(this);
        permissionUtils = new PermissionUtils(this);
        showInitialView("Device Testing");
        test1.setText("Start Test 1");
        test2.setText("Start Test 2");
        test3.setText("Start Test 3");
        Log.d(TAG, "Activity creating");

    }

    private void showInitialView(String message) {
        is_test_running = false;
        test_count = 0;
       // Log.d("debug>>", "child Count >>" + container.getChildCount());

//        if(time_remaning.getVisibility()==View.VISIBLE)
//            time_remaning.setVisibility(View.GONE);

        if (thread != null) {
            if (thread.isAlive())
                thread.interrupt();

        }

        resetButtonText();

//        InitialFragment fragment = new InitialFragment();
//        fragment.setCustomeMessage(message);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.container, fragment); // give your fragment container id in first parameter
//        transaction.addToBackStack(null);
//        //transaction.remove(fragment);
//        // if written, this transaction will be added to backstack
//        transaction.commit();


    }

    private void resetButtonText() {
        test1.setText("Start Test 1");
        test2.setText("Start Test 2");
        test3.setText("Start Test 3");
        allTests.setText("Start All Tests");

    }

    private int position = 0;

    private void askPermission() {
        String permission = null;
        switch (position) {
            case 0:
                permission = Manifest.permission.CAMERA;
                break;
            case 1:
                permission = Manifest.permission.READ_PHONE_STATE;
                break;
            case 2:
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            case 3:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
        }
        if (permission != null) getPermission(permission);
        else {
            //initialize();
        }
    }

    private void getPermission(String permission) {
        PermissionUtils permissionUtils = new PermissionUtils(this);
        if (!permissionUtils.isPermissionGranted(permission)) {

            showPrevDialog("Nex2Me need to access camera captured frames to run deep learning models in the background.", permission);

        } else {
            //initialize();
        }
    }

    private void showPrevDialog(String msg, String permission) {
        alertBox = new Dialog(this);
        alertBox.setContentView(R.layout.custom_alert_box);
        TextView tvmessage = alertBox.findViewById(R.id.tv_alert_message);
        TextView tvTitle = alertBox.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("Nex2Me");
        TextView tvOk = alertBox.findViewById(R.id.tv_ok_alert);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        alertBox.getWindow().setLayout(width - 20, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertBox.show();
        tvmessage.setText(msg);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBox.dismiss();
                permissionManager.showPermissionDialog(permission)
                        .withCallback(ModelTestActivity.this)
                        .withDenyDialogEnabled(true)
                        .withDenyDialogMsg("Nex2Me need to access camera captured frames to run deep learning models in the background.")
                        .build();
            }
        });

    }

    private void unsetGraph() {
        Log.d("debug", "Un Setting Last Modal");
        mPreferences.edit().putBoolean("is_last_graph", false).commit();
    }

    public void showMessage(String msg) {
        alertBox = new Dialog(this);
        alertBox.setContentView(R.layout.custom_alert_box);
        TextView tvmessage = alertBox.findViewById(R.id.tv_alert_message);
        TextView tvTitle = alertBox.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("Nex2Me");
        TextView tvOk = alertBox.findViewById(R.id.tv_ok_alert);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        alertBox.getWindow().setLayout(width - 20, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertBox.show();
        tvmessage.setText(msg);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBox.dismiss();

            }
        });
    }

    private void startFragment(String graphName) {

        Intent i = new Intent(ModelTestActivity.this,
                ModelRunActivity.class);

        i.putExtra("graph_name", graphName);
        i.putExtra("c_time", m_cool_time);
        i.putExtra("r_time", m_run_time);
        i.putExtra("test_id", testid);
        i.putExtra("test_no",c_test_no+"");

        startActivityForResult(i, 111);
        overridePendingTransition(0, 0);


//        Log.d(TAG, "Starting fragment");
//        ModelTestFragment fragment = new ModelTestFragment();
//        fragment.setModelEventListioner(this);
//        ModelTestFragment.BINARY_GRAPH_NAME = graphName;
//
//        if (ModelTestFragment.TEST_NO_ID.equalsIgnoreCase(" ") ||
//                !(ModelTestFragment.TEST_NO_ID.equalsIgnoreCase(testid))) {
//
//            ModelTestFragment.TEST_NO_ID = testid;
//        }
//        ModelTestFragment.TIME = m_run_time;
//        ModelTestFragment.C_TIME = m_cool_time;
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


        //if(transaction.remove(oldfrag))
//        try {
//            transaction.remove(old_cooling_Frag);
//            Log.d("debug>>","Removed Fragment"+old_cooling_Frag.toString());
//
//        }catch (Exception e)
//        {
//            Log.d("debug>>",e.toString());
//        }
//        transaction.replace(R.id.container, fragment); // give your fragment container id in first parameter
//        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
//        transaction.commit();
    }

    @Override
    protected void onPause() {
        alertBox = null;
        super.onPause();
    }

//    private void initialize() {
//        controlButton.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onPermissionGranted(String[] permissions, int[] grantResults) {

        position = position + 1;
        askPermission();
        //initialize();
    }

    @Override
    public void onPermissionDismissed(String permission) {

    }

    @Override
    public void onPositiveButtonClicked(DialogInterface dialog, int which) {
        /**
         * You can choose to open the
         * app settings screen
         * * */
        permissionUtils.openAppSettings();
        Toast.makeText(this, "Please open setting.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNegativeButtonClicked(DialogInterface dialog, int which) {
        /**
         * The user has denied the permission!
         * You need to handle this in your code
         * * */
        Toast.makeText(this, "User has denied the permissions", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        if (isNetworkAvailable()) {
            switch (v.getId()) {
                case R.id.btn_test_1:
                    if (new Battery(this).getBatteryPercent() < 10) {
                        showMessage("Charge phone more than 10% to run the test");

                    } else {
                        if (test1.getText().toString().equalsIgnoreCase("Start Test 1")) {

                            if (is_test_running) {
                                showMessage("Another test is running please let it complete first ");
                            } else {
                                m_run_time = 1;
                                m_cool_time = 0;
                                c_test_no = 1;
                                testid = "Test 1 >>" + UUID.randomUUID();
                                m_graph_name = "small_fp16.binarypb";
                                test1.setText("Stop Test 1");
                                startFragment(m_graph_name);
                                is_test_running = true;
                                startCountDown(6);

                            }

                        } else {
                            if (test1.getText().toString().equalsIgnoreCase("Stop Test 1")) {
                                showInitialView("Device Testing");
                                test1.setText("Start Test 1");
                                // is_test_running = false;
                            } else {
                                showMessage("Something Went Wrong");
                            }

                        }
                    }
                    break;

                case R.id.btn_test_2:
                    if (new Battery(this).getBatteryPercent() < 30) {
                        showMessage("Charge phone more than 30% to run the test");

                    } else {
                        if (test2.getText().toString().equalsIgnoreCase("Start Test 2")) {

                            if (is_test_running) {
                                showMessage("Another test is running please let it complete first ");
                            } else {
                                m_run_time = 3;
                                m_cool_time = 1;
                                c_test_no = 2;
                                testid = "Test 2 >>" + UUID.randomUUID();
                                m_graph_name = "small_fp16.binarypb";
                                test2.setText("Stop Test 2");
                                startFragment(m_graph_name);
                                is_test_running = true;
                                startCountDown(24);

                            }

                        } else {
                            if (test2.getText().toString().equalsIgnoreCase("Stop Test 2")) {
                                showInitialView("Device Testing");
                                test2.setText("Start Test 2");
                                // is_test_running = false;
                            } else {
                                showMessage("Something Went Wrong");
                            }

                        }
                    }
                    break;

                case R.id.btn_test_3:
                    if (new Battery(this).getBatteryPercent() < 50) {
                        showMessage("Charge phone more than 50% to run the test");
                    } else {
                        if (test3.getText().toString().equalsIgnoreCase("Start Test 3")) {

                            if (is_test_running) {
                                showMessage("Another test is running please let it complete first ");
                            } else {
                                m_run_time = 5;
                                m_cool_time = 3;
                                c_test_no = 3;
                                testid = "Test 3 >>" + UUID.randomUUID();
                                m_graph_name = "small_fp16.binarypb";
                                test3.setText("Stop Test 3");
                                startFragment(m_graph_name);
                                is_test_running = true;
                                startCountDown(48);

                            }

                        } else {
                            if (test3.getText().toString().equalsIgnoreCase("Stop Test 3")) {
                                showInitialView("Device Testing");
                                test3.setText("Start Test 3");
                                // is_test_running = false;
                            } else {
                                showMessage("Something Went Wrong");
                            }

                        }
                    }
                    break;

                case R.id.btn_all_tests:
                    if (new Battery(this).getBatteryPercent() < 60) {
                        showMessage("Charge phone more than 60% to run the test");

                    } else {
                        if (allTests.getText().toString().equalsIgnoreCase("Start All Tests")) {

                            if (is_test_running) {
                                showMessage("Another test is running please let it complete first ");
                            } else {
                                test_count = 1;
                                m_run_time = 1;
                                m_cool_time = 0;
                                c_test_no = 4;
                                testid = "Test 1 >>" + UUID.randomUUID();
                                m_graph_name = "small_fp16.binarypb";
                                allTests.setText("Stop All Tests");
                                startFragment(m_graph_name);
                                is_test_running = true;
                                startCountDown(78);

                            }

                        } else {
                            if (allTests.getText().toString().equalsIgnoreCase("Stop All Tests")) {
                                showInitialView("Device Testing");
                                allTests.setText("Start All Tests");
                                // is_test_running = false;
                            } else {
                                showMessage("Something Went Wrong");
                            }
                        }
                    }
                    break;

//                case R.id.btn_test_2:
//                    if (test2.getText().toString().equalsIgnoreCase("Stop test 2")) {
//                        test2.setText("Start Test 2");
//                        onEndTest();
//
//                    } else {
//                        showMessage("Another test is running please let it complete first ");
//                        // Toast.makeText(this, "Another Test is Running ", Toast.LENGTH_SHORT).show();
//                    }
//
//                    break;
//
//                case R.id.btn_test_3:
//                    if (test3.getText().toString().equalsIgnoreCase("Stop test 3")) {
//                        test3.setText("Start Test 3");
//                        onEndTest();
//
//                    } else {
//                        showMessage("Another test is running please let it complete first ");
//                        //Toast.makeText(this, "Another Test is Running ", Toast.LENGTH_SHORT).show();
//                    }
//                    break;

            }
        } else {
            showMessage("Please Connect To Internet");
        }


//        if (isTestRunning) {
//            switch (v.getId()) {
//                case R.id.btn_test_1:
//
//                    if (test1.getText().toString().equalsIgnoreCase("Stop test 1")) {
//                        test1.setText("Start Test 1");
//                        onEndTest();
//
//                    } else {
//                        showMessage("Another test is running please let it complete first ");
//                        //Toast.makeText(this, "Another Test is Running ", Toast.LENGTH_SHORT).show();
//                    }
//
//                    break;
//
//                case R.id.btn_test_2:
//                    if (test2.getText().toString().equalsIgnoreCase("Stop test 2")) {
//                        test2.setText("Start Test 2");
//                        onEndTest();
//
//                    } else {
//                        showMessage("Another test is running please let it complete first ");
//                       // Toast.makeText(this, "Another Test is Running ", Toast.LENGTH_SHORT).show();
//                    }
//
//                    break;
//
//                case R.id.btn_test_3:
//                    if (test3.getText().toString().equalsIgnoreCase("Stop test 3")) {
//                        test3.setText("Start Test 3");
//                        onEndTest();
//
//                    } else {
//                        showMessage("Another test is running please let it complete first ");
//                        //Toast.makeText(this, "Another Test is Running ", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//            }
//            controlButton.setText("Start Test");
//
//        } else {
//            endTest = false;
//            switch (v.getId()) {
//                case R.id.btn_test_1:
//                    test1.setText("Stop Test 1");
//                    testid = "Test 1 >>" + UUID.randomUUID().toString();
//                    //All moidals , 1 minit Runtime ,0 minit colling Time
//                   // onStartTest(0, 0, 0);
//                    startFragment(models[1],1,0);
//                    startCountDown(6);
//                    break;
//
//                case R.id.btn_test_2:
//                    test2.setText("Stop Test 2");
//                    testid = "Test 2 >>" + UUID.randomUUID().toString();
//                    list.clear();
//                    list.add("small_fp16");
//                    list.add("small_fp32");
//                    list.add("medium_fp16");
//                    list.add("medium_fp32");
//                    list.add("large_fp16");
//                    list.add("large_fp32");
//                    //All moidals , 3 minit Runtime ,1 minit colling Time
//                    onStartTest(0, 1, 1);
//                    startCountDown(24);
//                    break;
//
//                case R.id.btn_test_3:
//                    test3.setText("Stop Test 3");
//                    testid = "Test 3 >>" + UUID.randomUUID().toString();
//                    list.clear();
//                    list.add("small_fp16");
//                    list.add("small_fp32");
//                    list.add("medium_fp16");
//                    list.add("medium_fp32");
//                    list.add("large_fp16");
//                    list.add("large_fp32");
//                    //All moidals , 5 minit Runtime ,3 minit colling Time
//                    onStartTest(0, 2, 2);
//                    startCountDown(48);
//                    break;
//            }
//            controlButton.setText("Stop Test");
//
//        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void startCountDown(int time) {

        long c_time = System.currentTimeMillis();
        long t_time = (long) time * 60000;

        long f_time = c_time + t_time;

//        if (time_remaning.getVisibility() == View.GONE) {
//            time_remaning.setVisibility(View.VISIBLE);
//            time_remaning.setText(time + " minutes remaining");
//        }

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                long d_time = f_time - System.currentTimeMillis();

                                long minutes = (d_time / 1000) / 60;
                                long seconds = (d_time / 1000) % 60;

                                //  time_remaning.setText(minutes + " minutes " + seconds + " seconds remaining ");
                                if (minutes == 0 && seconds == 1) {
                                    thread.interrupt();
                                    //onEndTest();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Log.d("Exception>>", e.toString());
                }
            }
        };
        thread.start();
    }

//    private boolean isTestRunning = false;
//    private boolean pauseStatus = false;

//    @Override
//    public void onStartTest(int modelMode, int runtime, int sleepTime) {
//        if (isTestRunning) {
//            Toast.makeText(this, "Test already running", Toast.LENGTH_LONG).show();
//            return;
//        }
//        isTestRunning = true;
//        if (modelMode == ALL) {
//            Thread thread = new Thread(sequenceRunnable);
//            thread.start();
//        } else {
//            startFragment(models[modelOption], runtime, sleepTime);
//            try {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        showInitialView("Device Testing");
//                        test1.setText("Start Test 1");
//                        test2.setText("Start Test 2");
//                        test3.setText("Start Test 3");
//                        time_remaning.setVisibility(View.GONE);
//                        if(thread.isAlive())
//                            thread.interrupt();
//
//                        isTestRunning = false;
//                        endTest  = true;
//                        controlButton.setText("Start Test");
//                    }
//                }, running[runtimeOption] * 1000 * 60);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    @Override
//    public void onPauseTest() {
//        pauseStatus = true;
//    }

//    private boolean endTest = false;
//
//    @Override
//    public void onEndTest() {
//        showInitialView("Device Testing");
//        test1.setText("Start Test 1");
//        test2.setText("Start Test 2");
//        test3.setText("Start Test 3");
//        time_remaning.setVisibility(View.GONE);
//        isTestRunning = false;
//        unsetGraph();
//        if(thread.isAlive())
//            thread.interrupt();
//
//        endTest = true;
//    }
//
//    private int i = 0;
//    Runnable sequenceRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (endTest) {
//                showInitialView("Device Testing");
//                test1.setText("Start Test 1");
//                test2.setText("Start Test 2");
//                test3.setText("Start Test 3");
//                time_remaning.setVisibility(View.GONE);
//                if(thread.isAlive())
//                    thread.interrupt();
//                isTestRunning = false;
//                endTest  = true;
//
//                return;
//            }
//            if (!pauseStatus) {
//                for (i = 1; i < models.length; i++) {
//                    if (endTest) {
//                        break;
//                    }
//                    try {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(TAG, "Starting at " + i);
//                                startFragment(models[i], 10000, 10000);
//                            }
//                        });
//                        Log.d(TAG, "Sleeping for " + running[runtimeOption] + "min");
//                        Thread.sleep(running[runtimeOption] * 1000 * 60); // in MS
//                        Log.d(TAG, "Wakeup from sleep " + i);
//
//
//                        //Check whether it is last model
//                        if (i == models.length - 1) {
//                            showInitialView("Device Testing");
//                            time_remaning.setVisibility(View.GONE);
//                            test1.setText("Start Test 1");
//                            test2.setText("Start Test 2");
//                            test3.setText("Start Test 3");
//                            isTestRunning = false;
//                            if(thread.isAlive())
//                                thread.interrupt();
//                            endTest  = true;
//                            controlButton.setText("Start Test");
//                        } else {
//                            if (cooling[coolingOption] != 0) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showInitialView("Cooling device");
//                                        test1.setText("Start Test 1");
//                                        test2.setText("Start Test 2");
//                                        test3.setText("Start Test 3");
//                                        isTestRunning = false;
//                                        endTest  = true;
//                                        time_remaning.setVisibility(View.GONE);
//                                        if(thread.isAlive())
//                                            thread.interrupt();
//
//                                    }
//                                });
//                                Thread.sleep(cooling[coolingOption] * 1000 * 60); // in MS
//                            }
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "On Back pressed");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onModelEnds(String graphName, int coolTime) {

        if (graphName.equalsIgnoreCase("large_fp32")) {

            if (test_count == 0) {
                showInitialView("Device Testing");

            } else {
                switch (test_count) {
                    case 1:
                        is_test_running = true;
                        m_run_time = 3;
                        m_cool_time = 1;
                        testid = "Test 2 >>" + UUID.randomUUID();
                        test_count++;
                        startFragment("small_fp16.binarypb");
                        break;
                    case 2:
                        is_test_running = true;
                        m_run_time = 5;
                        m_cool_time = 3;
                        testid = "Test 3 >>" + UUID.randomUUID();
                        test_count++;
                        startFragment("small_fp16.binarypb");
                        break;
                    default:
                        showInitialView("Device Testing");

                }
            }

        } else {
            showCollingView(graphName, coolTime);
        }


//        showInitialView("Paused...");
//
//        Toast.makeText(this, "Model Ended", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                String graphName = data.getStringExtra("graph_name");
                int coolTime = data.getIntExtra("c_time", 0);

                if (graphName.equalsIgnoreCase("large_fp32")) {

                    if (test_count == 0) {
                        showInitialView("Device Testing");

                    } else {
                        switch (test_count) {
                            case 1:
                                is_test_running = true;
                                m_run_time = 3;
                                m_cool_time = 1;
                                testid = "Test 2 >>" + UUID.randomUUID();
                                test_count++;
                                startFragment("small_fp16.binarypb");
                                break;
                            case 2:
                                is_test_running = true;
                                m_run_time = 5;
                                m_cool_time = 3;
                                testid = "Test 3 >>" + UUID.randomUUID();
                                test_count++;
                                startFragment("small_fp16.binarypb");
                                break;
                            default:
                                showInitialView("Device Testing");

                        }
                    }

                } else {
                    showCollingView(graphName, coolTime);
                }

            } else {
                showInitialView("Device Testing");
            }
        }

        if (requestCode == 112) {
            if (resultCode == RESULT_OK) {
                String graphName = data.getStringExtra("graph_name");
                int coolTime = data.getIntExtra("c_time", 0);

                switch (graphName) {
                    case "small_fp16":
                        startFragment("small_fp32.binarypb");
                        break;
                    case "small_fp32":
                        startFragment("medium_fp16.binarypb");
                        break;
                    case "medium_fp16":
                        startFragment("medium_fp32.binarypb");
                        break;
                    case "medium_fp32":
                        startFragment("large_fp16.binarypb");
                        break;
                    case "large_fp16":
                        startFragment("large_fp32.binarypb");
                        break;
                    case "large_fp32":
                        showInitialView("Device Testing");
                        break;
                    default:
                        showInitialView("Device Testing");

                }

            } else {
                showInitialView("Device Testing");
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showCollingView(String graphName, int coolTime) {


        Intent i = new Intent(ModelTestActivity.this,
                CollingAct.class);

        i.putExtra("graph_name", graphName);
        i.putExtra("c_time", m_cool_time);
        i.putExtra("test_no",c_test_no+"");
//        i.putExtra("r_time",m_run_time);
//        i.putExtra("test_id",testid);

        startActivityForResult(i, 112);
        overridePendingTransition(0, 0);

//        CoolingFragment fragment = new CoolingFragment();
//        fragment.setM_graph_name(graphName);
//        fragment.setCool_time(coolTime);
//        fragment.setCollingEventListioner(this::onCollingEnd);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////        try {
////            transaction.remove(oldFrag);
////            Log.d("debug>>","Removed Fragment>>"+oldFrag.toString());
////        }catch (Exception e)
////        {
////            Log.d("debug>>",e.toString());
////        }
//        transaction.replace(R.id.container, fragment); // give your fragment container id in first parameter
//        transaction.addToBackStack(null);
//        //transaction.remove(fragment);
//        // if written, this transaction will be added to backstack
//        transaction.commit();
    }

    @Override
    public void onCollingEnd(String graphName, Fragment fragment) {
        old_cooling_Frag = (Fragment) fragment;
        switch (graphName) {
            case "small_fp16":
                startFragment("small_fp32.binarypb");
                break;
            case "small_fp32":
                startFragment("medium_fp16.binarypb");
                break;
            case "medium_fp16":
                startFragment("medium_fp32.binarypb");
                break;
            case "medium_fp32":
                startFragment("large_fp16.binarypb");
                break;
            case "large_fp16":
                startFragment("large_fp32.binarypb");
                break;
            case "large_fp32":
                //showInitialView("Device Testing");
                break;
            default:
                //showInitialView("Device Testing");

        }
    }
}