package com.cabbie.hat.cabbie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cabbie.hat.cabbie.Data.Driver;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ForCustomer extends AppCompatActivity {

    Button logIn, register;

    RelativeLayout rootLayout;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath).build());

        setContentView(R.layout.activity_for_customer);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(ForCustomer.this, MapForCustomer.class));
            finish();
            return;
        }



        auth = FirebaseAuth.getInstance();

        logIn = (Button)findViewById(R.id.signInButton);
        register = (Button)findViewById(R.id.registerButton);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistrationMenu();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogInScreen();
            }
        });

    }

    private void openRegistrationMenu(){

        final AlertDialog.Builder registerWindow = new AlertDialog.Builder(this);
        registerWindow.setTitle("Registration");
        registerWindow.setMessage("Use email for registration");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registration_layout = inflater.inflate(R.layout.registration_layout, null);

        final MaterialEditText editEmail = registration_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = registration_layout.findViewById(R.id.editPassword);
        final MaterialEditText editName = registration_layout.findViewById(R.id.editName);
        final MaterialEditText editPhone = registration_layout.findViewById(R.id.editPhone);

        registerWindow.setView(registration_layout);

        registerWindow.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                if(editEmail.getText().toString().isEmpty()){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(editPassword.getText().toString().length() < 8){
                    Snackbar.make(rootLayout, "Password too short, try again!", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(editName.getText().toString().isEmpty()){
                    Snackbar.make(rootLayout, "Please enter yout name", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(editPhone.getText().toString().isEmpty()){
                    Snackbar.make(rootLayout, "Please enter your phone number", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                Driver driver = new Driver(editEmail.getText().toString(),
                                        editPassword.getText().toString(),
                                        editName.getText().toString(),
                                        editPhone.getText().toString());

                                DatabaseReference customers = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");

                                customers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(driver)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Registration Successful!", Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout, "Registration Failed: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, "Registration Failed: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });

            }
        });

        registerWindow.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        registerWindow.show();

    }

    private void showLogInScreen(){

        final AlertDialog.Builder registerWindow = new AlertDialog.Builder(this);
        registerWindow.setTitle("SIGN IN");
        registerWindow.setMessage("Use email for Signing in:");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.log_in_layout, null);

        final MaterialEditText editEmail = login_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = login_layout.findViewById(R.id.editPassword);

        registerWindow.setView(login_layout);

        registerWindow.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                logIn.setEnabled(false);
                register.setEnabled(false);

                if (editEmail.getText().toString().isEmpty()) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (editPassword.getText().toString().length() < 8) {
                    Snackbar.make(rootLayout, "Password too short, try again!", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                auth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(ForCustomer.this, MapForCustomer.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, "Sign in failed: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                        logIn.setEnabled(true);
                        register.setEnabled(true);
                    }
                });

            }
        });

        registerWindow.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        registerWindow.show();

    }
}