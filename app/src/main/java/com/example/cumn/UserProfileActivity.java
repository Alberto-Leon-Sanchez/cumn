package com.example.cumn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cumn.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private ImageView mProfilePicImageView;
    private Button mSaveButton;

    private ProgressDialog mProgressDialog;

    private Uri mProfilePicUri;
    private Bitmap mProfilePicBitmap;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private FirebaseUser mCurrentUser;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mUsernameEditText = findViewById(R.id.username_edit_text);
        mProfilePicImageView = findViewById(R.id.profile_pic_image_view);
        mSaveButton = findViewById(R.id.save_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Saving profile...");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    private void saveProfile() {
        final String username = mUsernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mProfilePicBitmap == null && mProfilePicUri == null) {
            Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.show();

        if (mProfilePicBitmap != null) {
            // Convert bitmap to uri for storage
            mProfilePicUri = getImageUri(mProfilePicBitmap);
        }

        final StorageReference profilePicRef = mStorage.child("profile_pics").child(mCurrentUser.getUid());

        profilePicRef.putFile(mProfilePicUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    profilePicRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String profilePicUrl = uri.toString();
                            HashMap<String, Object> profileMap = new HashMap<>();
                            profileMap.put("username", username);
                            profileMap.put("profilePicUrl", profilePicUrl);

                            mDatabase.child("users").child(mCurrentUser.getUid()).setValue(profileMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(UserProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                            } else {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(UserProfileActivity.this, "Profile not saved", Toast.LENGTH_SHORT).show();
                                                Log.d("UserProfileActivity", task.getException().getMessage());
                                            }
                                        }
                                    });
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UserProfileActivity.this, "Profile picture not saved", Toast.LENGTH_SHORT).show();
                                    Log.d("UserProfileActivity", e.getMessage());
                                }
                            });
                }
            }

            private Uri getImageUri(Bitmap bitmap) {
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Profile Pic", null);
                return Uri.parse(path);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mProfilePicUri = data.getData();

            try {
                mProfilePicBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mProfilePicUri);
                mProfilePicImageView.setImageBitmap(mProfilePicBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
