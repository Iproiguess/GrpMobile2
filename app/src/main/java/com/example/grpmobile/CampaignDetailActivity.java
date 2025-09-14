package com.example.grpmobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils; // Added for image loading logic
import android.util.Log;      // Added for logging errors in image loading
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
// import androidx.core.view.EdgeToEdge;
// import androidx.core.graphics.Insets;
// import androidx.core.view.ViewCompat;
// import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class CampaignDetailActivity extends AppCompatActivity {

    private ImageView ivCampaignDetailImage;
    private TextView tvCampaignDetailTitle;
    private TextView tvCampaignDetailDescription;
    private TextView tvCampaignDetailLocation;
    private TextView tvCampaignDetailDate;
    private TextView tvCampaignDetailDonationGoal;
    private ProgressBar pbCampaignDetailProgress;
    private Button btnBack;

    // Constants for logging
    private static final String TAG = "CampaignDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_campaign_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_campaign_detail);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        ivCampaignDetailImage = findViewById(R.id.ivCampaignDetailImage);
        tvCampaignDetailTitle = findViewById(R.id.tvCampaignDetailTitle);
        tvCampaignDetailDescription = findViewById(R.id.tvCampaignDetailDescription);
        tvCampaignDetailLocation = findViewById(R.id.tvCampaignDetailLocation);
        tvCampaignDetailDate = findViewById(R.id.tvCampaignDetailDate);
        tvCampaignDetailDonationGoal = findViewById(R.id.tvCampaignDetailDonationGoal);
        pbCampaignDetailProgress = findViewById(R.id.pbCampaignDetailProgress);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String location = intent.getStringExtra("location");
            String date = intent.getStringExtra("date");
            String imageUriString = intent.getStringExtra("imageUriString");
            int imageResId = intent.getIntExtra("imageResId", 0);
            double currentDonation = intent.getDoubleExtra("currentDonation", 0.0);
            double targetDonation = intent.getDoubleExtra("targetDonation", 0.0);

            tvCampaignDetailTitle.setText(title);
            tvCampaignDetailDescription.setText(description);
            tvCampaignDetailLocation.setText(location);
            tvCampaignDetailDate.setText(date);

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));
            String formattedCurrentDonation = currencyFormat.format(currentDonation);
            String formattedTargetDonation = currencyFormat.format(targetDonation);

            tvCampaignDetailDonationGoal.setText(String.format(Locale.getDefault(), "Goal: %s / %s",
                    formattedCurrentDonation, formattedTargetDonation));

            if (targetDonation > 0) {
                int progress = (int) ((currentDonation / targetDonation) * 100);
                pbCampaignDetailProgress.setProgress(Math.min(progress, 100));
            } else {
                pbCampaignDetailProgress.setProgress(0);
            }

            // --- Image Loading Logic without Glide ---
            if (!TextUtils.isEmpty(imageUriString)) {
                try {
                    Uri imageUri = Uri.parse(imageUriString);
                    ivCampaignDetailImage.setImageURI(imageUri);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image URI: " + imageUriString, e);
                    // Fallback to imageResId if URI loading fails or is invalid
                    if (imageResId != 0) {
                        ivCampaignDetailImage.setImageResource(imageResId);
                    } else {
                        // If no valid imageResId as fallback, use a default placeholder
                        ivCampaignDetailImage.setImageResource(R.drawable.ic_launcher_background); // Example placeholder
                    }
                }
            } else if (imageResId != 0) {
                // If no URI string, but there is an imageResId
                ivCampaignDetailImage.setImageResource(imageResId);
            } else {
                // If neither URI string nor imageResId is available, set a default placeholder
                ivCampaignDetailImage.setImageResource(R.drawable.ic_launcher_background); // Example placeholder
            }
            // --- End of Image Loading Logic ---

        } else {
            Toast.makeText(this, "Could not load campaign details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Handle window insets
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_campaign_detail), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}