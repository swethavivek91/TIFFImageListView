package com.app.listapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.app.listapp.tiff.TiffBitmapFactory;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private Activity mActivity;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private CoordinatorLayout mCLayout;
    private ListView mListView;
    private ArrayAdapter<DetailedInfo> mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);
        mContext = getApplicationContext();
        mActivity = MainActivity.this;
        mListView = (ListView) findViewById(R.id.list_view);
        if(!hasPermission(READ_EXTERNAL_STORAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0)
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        else{
            initiateAdapter();
        }
    }

    private List<DetailedInfo> getJsonData() {
        try {
            InputStream in = getResources().getAssets().open("testData.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Gson gson = new Gson();
            MovieDetails details = gson.fromJson(br, MovieDetails.class);
            return details.getTestData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setImage(ImageView view, String img) {
        Bitmap bitmap = TiffBitmapFactory.decodePath(img);
        view.setImageBitmap(bitmap);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }else{
                    initiateAdapter();
                }

                break;
        }

    }

    private void initiateAdapter() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final List<DetailedInfo> testData = getJsonData();

        mAdapter = new ArrayAdapter<DetailedInfo>(this, R.layout.cardview, testData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                DetailedInfo info = testData.get(position);
                LayoutInflater inflater = mActivity.getLayoutInflater();
                View itemView = inflater.inflate(R.layout.cardview, null, true);
                ConstraintLayout relativeLayout = (ConstraintLayout) itemView.findViewById(R.id.relLayout);
                TextView mName = relativeLayout.findViewById(R.id.name);
                TextView lRelease = relativeLayout.findViewById(R.id.release);
                TextView lType = relativeLayout.findViewById(R.id.type);
                TextView lPlanned = relativeLayout.findViewById(R.id.plan);
                TextView lDuration = relativeLayout.findViewById(R.id.duration);
                TextView lCreated = relativeLayout.findViewById(R.id.created);
                TextView lUpdated = relativeLayout.findViewById(R.id.updated);
                TextView lShort = relativeLayout.findViewById(R.id.shortdescription);
                TextView lDesc = relativeLayout.findViewById(R.id.description);
                ImageView mImage = relativeLayout.findViewById(R.id.imageView);

                if (!info.getPosterLink().isEmpty()) {
                    setImage(mImage, path + "/listapp/" + info.getPosterLink());
                }
                if (!info.getName().isEmpty()) {
                    mName.setText(info.getName());
                }
                if (info.getReleaseYear() != 0) {
                    lRelease.setText("Year: " + Integer.toString(info.getReleaseYear()));
                } else {
                    lRelease.setVisibility(View.GONE);
                }
                if (!info.getPaymentPlan().isEmpty()) {
                    lPlanned.setText("Plan: " + info.getPaymentPlan());
                } else {
                    lPlanned.setVisibility(View.GONE);
                }
                if (!info.getType().isEmpty()) {
                    lType.setText("Type: " + info.getType());
                } else {
                    lType.setVisibility(View.GONE);
                }
                if (!info.getVideoDuration().isEmpty()) {
                    lDuration.setText("Duration: " + info.getVideoDuration());
                } else {
                    lDuration.setVisibility(View.GONE);
                }
                if (!info.getCreatedOn().isEmpty()) {
                    lCreated.setText("Created on: " + info.getCreatedOn());
                } else {
                    lCreated.setVisibility(View.GONE);
                }
                if (!info.getUpdatedOn().isEmpty()) {
                    lUpdated.setText("Updated on: " + info.getUpdatedOn());
                } else {
                    lUpdated.setVisibility(View.GONE);
                }
                if (!info.getShortDescription().isEmpty()) {
                    lShort.setText("ShortDescription: " + info.getShortDescription());
                } else {
                    lShort.setVisibility(View.GONE);
                }
                if (!info.getDescription().isEmpty()) {
                    lDesc.setText("Description: " + info.getDescription());
                } else {
                    lDesc.setVisibility(View.GONE);
                }
                try {
                    Log.d("", getResources().getAssets().list("TestImage.tif").toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return itemView;
            }
        };

        // Data bind the list view with array adapter items
        mListView.setAdapter(mAdapter);

        // Set an item click listener for list view
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}
