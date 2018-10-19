package com.brunogtavares.imagehandlersample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mindorks.paracamera.Camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String URI_KEY = "URI_KEY";
    private static final String BITMAP_KEY = "BITMAP_KEY";

    @BindView(R.id.iv_image) ImageView mImage;
    @BindView(R.id.tv_not_found) TextView mNotFoundText;
    @BindView(R.id.fab_clear) FloatingActionButton mClearButton;
    @BindView(R.id.bt_choose_image) Button mGetImageButton;
    @BindView(R.id.rv_image_result) RecyclerView mRecyclerView;

    private Uri mImageUri;
    private Bitmap mImageBitmap;

    Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(BITMAP_KEY)) {
                mImageBitmap = savedInstanceState.getParcelable(BITMAP_KEY);
                Glide.with(this).load(mImageBitmap).into(mImage);

            }
            if(savedInstanceState.containsKey(URI_KEY)) {
                mImageUri = Uri.parse(savedInstanceState.getString(URI_KEY));
                Glide.with(this).load(mImageUri).into(mImage);
            }
        }

        mCamera = new Camera.Builder()
                .resetToCorrectOrientation(true) // it will rotate the camera bitmap to the correct orientation from meta data
                .setTakePhotoRequestCode(Camera.REQUEST_TAKE_PHOTO)
                .setDirectory("WorldLandmakrs")
                .setName("landmark_" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(75)
                .build(this);

        mGetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startGalleryChooser();
                                    }
                                })
                        .setNegativeButton(R.string.dialog_select_camera,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        takePicture();
                                    }
                                });
                builder.create().show();
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.deleteImage();
                mImage.setImageResource(0);
                mGetImageButton.setVisibility(View.VISIBLE);
                mNotFoundText.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mImage.setVisibility(View.GONE);
                mClearButton.hide();

            }

        });
    }

    //=================================
    // Camera and Gallery Permissions
    //
    private void startGalleryChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                PERMISSION_REQUEST_CODE);
    }

    private void takePicture() {

        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            requestPermissions();
        }
        else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }

    private void launchCamera() {

        try {
            mCamera.takePicture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions( MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        }
        else {

            ActivityCompat.requestPermissions( MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to use camera, read and write to external storage
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE: {

                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // If you get permission, launch the camera
                    launchCamera();
                }
                else if (grantResults.length > 0 &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    // If you get permission, launch the gallery
                    startGalleryChooser();
                }
                else {

                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    // Get the bitmap and image path onActivityResult of an activity or fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == Camera.REQUEST_TAKE_PHOTO){
            mImageBitmap = mCamera.getCameraBitmap();

            if(mImageBitmap != null) {
                mClearButton.show();
                mGetImageButton.setVisibility(View.GONE);
                mImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(mImageBitmap).into(mImage);
            }else{
                Toast.makeText(this.getApplicationContext(), R.string.photo_not_taken, Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == PERMISSION_REQUEST_CODE) {
            mImageUri = data.getData();

            if(mImageUri != null) {
                mClearButton.show();
                mGetImageButton.setVisibility(View.GONE);
                mImage.setVisibility(View.VISIBLE);

                Glide.with(this).load(mImageUri).into(mImage);

            }
        }
    }

    //
    // *** End Camera and Gallery Permissions ***
    //============================================



    // The bitmap is saved in the app's folder
    //  If the saved bitmap is not required use following code
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera != null) mCamera.deleteImage();
    }
    //
    // *** End Firebase Vision Cloud ***
    //============================================

    //=================================
    // Handling saving state
    //
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            if(mImageBitmap != null) {
                outState.putParcelable(BITMAP_KEY, mImageBitmap);
            }
            if(mImageUri != null) {
                String stringUri = mImageUri.toString();
                outState.putString(URI_KEY, stringUri);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.containsKey(BITMAP_KEY)) {
            mImageBitmap = savedInstanceState.getParcelable(BITMAP_KEY);
        }
        if(savedInstanceState.containsKey(URI_KEY)) {
            mImageUri = Uri.parse(savedInstanceState.getString(URI_KEY));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mImageBitmap != null) {
            Glide.with(this).load(mImageBitmap).into(mImage);
        }
        if(mImageUri != null) {
            Glide.with(this).load(mImageUri).into(mImage);
        }

    }
    //
    // *** End Handling Saving State ***
    //============================================


}
