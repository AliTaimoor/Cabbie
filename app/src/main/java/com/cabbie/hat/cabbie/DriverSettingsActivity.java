package com.cabbie.hat.cabbie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverSettingsActivity extends AppCompatActivity {

    Button confirm, back;
    EditText name, phone, password, car;
    ImageView profilePhoto;

    private RadioGroup radioGroup;

    private Uri resultUri;

    private String userId, customerName, customerPhoneNo, customerPassword, customerCar, profilePhotoUrl, service;

    private FirebaseAuth auth;
    private DatabaseReference driverDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        confirm = (Button) findViewById(R.id.confirm);
        back = (Button) findViewById(R.id.back);

        profilePhoto = (ImageView)findViewById(R.id.profileImage);

        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        password = (EditText) findViewById(R.id.password);
        car = (EditText) findViewById(R.id.car);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.check(R.id.faisalMovers);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        driverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        getUserInfo();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    private void getUserInfo(){

        driverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name") != null){
                        customerName = map.get("name").toString();
                        name.setText(customerName);
                    }
                    if(map.get("phoneNo") != null){
                        customerPhoneNo = map.get("phoneNo").toString();
                        phone.setText(customerPhoneNo);
                    }
                    if(map.get("password") != null){
                        customerPassword = map.get("password").toString();
                        password.setText(customerPassword);
                    }
                    if(map.get("car") != null){
                        customerCar = map.get("car").toString();
                        car.setText(customerCar);
                    }
                    if(map.get("service") != null){
                        service = map.get("service").toString();
                        switch (service){
                            case "Cab":
                                radioGroup.check(R.id.faisalMovers);
                                break;

                            case "CabPlus":
                                radioGroup.check(R.id.uberJet);
                                break;

                            case "CabZplus":
                                radioGroup.check(R.id.uberHelicopter);
                                break;
                        }
                    }
                    if(map.get("profileImageUrl") != null){
                        profilePhotoUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profilePhotoUrl).into(profilePhoto);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveUserInformation() {

        int selectedButtonId = radioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectedButtonId);

        if(radioButton.getText() == null) return;

        service = radioButton.getText().toString();

        customerName = name.getText().toString();
        customerPassword = password.getText().toString();
        customerPhoneNo = phone.getText().toString();
        customerCar = car.getText().toString();

        Map userInfo = new HashMap();

        userInfo.put("name", customerName);
        userInfo.put("password", customerPassword);
        userInfo.put("phoneNo", customerPhoneNo);
        userInfo.put("car", customerCar);
        userInfo.put("service", service);


        driverDatabase.updateChildren(userInfo);

        if(resultUri != null){
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            driverDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }
        else finish();

        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;profilePhoto.setImageURI(resultUri);
        }

    }
}
