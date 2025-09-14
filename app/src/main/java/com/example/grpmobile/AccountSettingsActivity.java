package com.example.grpmobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Added for TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AccountSettingsActivity extends AppCompatActivity {

    private TextView tvUsernameDisplay; // Added
    private TextView tvEmailDisplay;    // Added
    private DBHelper dbHelper;          // Added
    private String currentUsername;     // Added

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Retrieve the username passed from UserActivity
        currentUsername = getIntent().getStringExtra("USERNAME");

        // Initialize TextViews
        tvUsernameDisplay = findViewById(R.id.tvUsernameDisplay);
        tvEmailDisplay = findViewById(R.id.tvEmailDisplay);

        // Set Username and Email
        if (currentUsername != null && !currentUsername.isEmpty()) {
            tvUsernameDisplay.setText(currentUsername);
            String email = dbHelper.getUserEmail(currentUsername);
            if (email != null && !email.isEmpty()) {
                tvEmailDisplay.setText(email);
            } else {
                tvEmailDisplay.setText("Email not found");
            }
        } else {
            tvUsernameDisplay.setText("N/A");
            tvEmailDisplay.setText("N/A");
        }

        Button btnRequestCampaign = findViewById(R.id.btnRequestCampaign);
        Button btnAboutUs = findViewById(R.id.btnAboutUs);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnBack = findViewById(R.id.btnBack);

        btnRequestCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountSettingsActivity.this, RequestCampaignActivity.class);
                // Pass username and email to RequestCampaignActivity
                intent.putExtra("USERNAME", currentUsername);
                if (tvEmailDisplay.getText() != null && !tvEmailDisplay.getText().toString().equals("Email not found") && !tvEmailDisplay.getText().toString().equals("N/A")) {
                    intent.putExtra("USER_EMAIL", tvEmailDisplay.getText().toString());
                }
                startActivity(intent);
            }
        });

        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountSettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountSettingsActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Ensure flags are robust
                startActivity(intent);
                finishAffinity(); // Ensures all activities in this task are finished
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simply finish this activity to go back to UserActivity
            }
        });
    }
}
