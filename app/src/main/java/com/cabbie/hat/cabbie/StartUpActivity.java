package com.cabbie.hat.cabbie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        Button driverButton = (Button) findViewById(R.id.driverButton);
        Button customerButton = (Button) findViewById(R.id.Customer);

        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartUpActivity.this, ForCustomer.class));
                finish();
                return;
            }
        });

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartUpActivity.this, ForDriver.class));
                finish();
                return;
            }
        });

    }
}
