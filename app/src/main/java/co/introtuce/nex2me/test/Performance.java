package co.introtuce.nex2me.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

public class Performance extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<MegaSuperLog> dataList = new ArrayList<>();

    FirebaseDatabase database;
    DatabaseReference myRef;
    String uid;
    TextView tvEmpty;
    ProgressBar pb;

    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "co.introtuce.nex2me.test";
    private String TAG = "PerformaceAct";
    private TestDataAdapter testDataAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        database = FirebaseDatabase.getInstance();
        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        uid = mPreferences.getString("u_id", " ");
        myRef = database.getReference(uid);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
        pb = findViewById(R.id.pb1);

        myRef.addValueEventListener(postListener);


    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            // Post post = dataSnapshot.getValue(Post.class);
            Gson json = new Gson();
            Log.d("SnapShot>>Data", dataSnapshot.toString());

            if (dataSnapshot.getValue() == null) {
                pb.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                for (DataSnapshot doc :
                        dataSnapshot.getChildren()) {
                    if (doc != null) {

                        Log.d("mainDEta>>", doc.toString());

                        String data = json.toJson(doc.getValue());

                        Log.d("Test>>", data);

                        try {
                            String key = doc.getKey();

                            if(key.split(">>")[0].equalsIgnoreCase("All Tests ")) {

                                //data = "["+data+"]";
                                // doc.getChildren()

                                for (DataSnapshot doc2 : doc.getChildren()) {
                                    String data2 = json.toJson(doc2.getValue());
                                    MegaSuperLog lod = json.fromJson(data2, MegaSuperLog.class);
//                    SuperLog  lod = new SuperLog();
                                    lod.setTestId(doc2.getKey() + " ");
                                    lod.setFrom_all(true);
                                    dataList.add(lod);
                                    tvEmpty.setVisibility(View.GONE);
                                    pb.setVisibility(View.GONE);
                                }

//                                LinkedTreeMap list = json.fromJson(data,LinkedTreeMap.class);
//
//
//                               // ListLogs listOfSuperLogs = (ListLogs) list.values();
//
//                                for (Object e : list.values()) {
//                                    MegaSuperLog obj = (MegaSuperLog) e;
//                                    obj.setTestId(obj.getTestId()+ " ");
//                                    dataList.add(obj);
                            } else {

                                MegaSuperLog lod = json.fromJson(data, MegaSuperLog.class);
//                    SuperLog  lod = new SuperLog();
                                lod.setTestId(doc.getKey() + " ");
                                dataList.add(lod);
                                tvEmpty.setVisibility(View.GONE);
                                pb.setVisibility(View.GONE);

                            }


                        } catch (Exception e) {
                            Log.d(TAG, "Exception>>" + e.toString());
                        }


                        // Log.d("Xlass>>",lod.getSmall_fp32().getDevice().getDevice()+">>");
                    } else {
                        pb.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }

                }
                setRecyclerView();
            }
            //Log.d(TAG, dataSnapshot.toString());


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            pb.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    private void setRecyclerView() {
        testDataAdapter = new TestDataAdapter(Performance.this);
        testDataAdapter.setDataList(dataList);
        Log.d(TAG, "Size>>" + dataList.size());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Performance.this);
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //  contacts.addItemDecoration(new VerticalSpaceItemDecoration(20));
        recyclerView.setAdapter(testDataAdapter);

    }
}