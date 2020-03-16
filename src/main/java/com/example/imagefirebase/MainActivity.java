package com.example.imagefirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST =1 ; // use this number to identify the image request
    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById((R.id.button_upload));
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        //we will save the images in a folder called uploads
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");


    }

      public void openFileChooser(View v){
          Intent intent = new Intent();
          intent.setType("image/*"); //this makes sure that we only use the images
          intent.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(intent, PICK_IMAGE_REQUEST); //we use pick image request to make sure that we get the data back
      }

      //this method will return the extension of the file we picked
      //fx: for jpeg imaged will be jpg
      public String getFileExtension(Uri uri){
          ContentResolver cR = getContentResolver();
          MimeTypeMap mime = MimeTypeMap.getSingleton();
          return mime.getExtensionFromMimeType(cR.getType(uri));
      }


      public void uploadFile(View v){
        if(mUploadTask!= null && mUploadTask.isInProgress()){
            Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            //check if we actually picked an image
            if(mImageUri != null){
                //create an unique storage reference
                //.child() is like adding a "/" to uploads, so when we add a file, the path will be "uploads/12334.jpg"
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
                mUploadTask = fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //delays the progress bar in case it's too fast
                                //so that the user can see that it reached 100%
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(0);
                                    }
                                }, 5000);

                                Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
//                                //we use this upload information to create a new entry in our database
//                                Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
//                                        taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
//                                //create a new child in the db with an unique id
//                                String uploadId = mDatabaseRef.push().getKey();
//                                Log.d("ID =>", String.valueOf(uploadId));
//                                Log.d("upload =>", String.valueOf(upload.getImageUrl()));
//                                mDatabaseRef.child(uploadId).setValue(upload);
                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful());
                                    Uri downloadUrl = urlTask.getResult();

                                    Upload upload = new Upload(mEditTextFileName.getText().toString().trim(), downloadUrl.toString());
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //get the progress from the transferred bytes in relation with the total bytes
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mProgressBar.setProgress((int) progress);
                            }
                        });
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }

      }

      //this method will be called when we get the data back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //check for image request, if the uer successfully picked an image, and if we got something back (the uri)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();

            //the same as mImageView.set()
            Picasso.with(this).load(mImageUri).into(mImageView);

        }
    }

    public void openImagesActivity(View v){
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
}
