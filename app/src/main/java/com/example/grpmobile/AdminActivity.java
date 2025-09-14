package com.example.grpmobile;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements CampaignReviewAdapter.OnCampaignActionListener {

    private static final String TAG = "AdminActivity"; // Added for consistent logging

    private RecyclerView recyclerViewCampaignReviews;
    private CampaignReviewAdapter campaignReviewAdapter;
    private List<CampaignReviewItem> pendingCampaignsList;
    private DBHelper dbHelper;
    private TextView tvNoPendingCampaigns;
    private Button btnLogoutAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Log.d(TAG, "onCreate called");

        dbHelper = new DBHelper(this);
        pendingCampaignsList = new ArrayList<>();

        recyclerViewCampaignReviews = findViewById(R.id.recyclerViewAdminCampaigns);
        tvNoPendingCampaigns = findViewById(R.id.tvNoPendingCampaigns);
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin);

        recyclerViewCampaignReviews.setLayoutManager(new LinearLayoutManager(this));
        campaignReviewAdapter = new CampaignReviewAdapter(pendingCampaignsList, this);
        recyclerViewCampaignReviews.setAdapter(campaignReviewAdapter);

        if (btnLogoutAdmin != null) {
            btnLogoutAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Calling logAllCampaigns from DBHelper and loading pending campaigns.");
        dbHelper.logAllCampaigns(); // For comprehensive debugging of DB state
        loadPendingCampaignsFromDb();
    }

    private void loadPendingCampaignsFromDb() {
        Log.d(TAG, "loadPendingCampaignsFromDb: Clearing pendingCampaignsList.");
        pendingCampaignsList.clear();
        Cursor cursor = null; // Initialize to null

        try { // Wrap cursor operations in try-catch-finally
            cursor = dbHelper.getPendingCampaigns();

            if (cursor != null) {
                Log.d(TAG, "loadPendingCampaignsFromDb: Cursor received. Count from DBHelper: (check previous logs for 'getPendingCampaigns' from DBHelper). Actual count in AdminActivity: " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "loadPendingCampaignsFromDb: cursor.moveToFirst() was TRUE. Processing rows...");
                    int itemsAdded = 0;
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_TITLE));
                        String description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_DESCRIPTION));
                        String location = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_LOCATION));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_DATE));
                        String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_IMAGE_URI));
                        String submitter = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_SUBMITTER_USERNAME));
                        String contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_CONTACT_EMAIL));
                        double donationGoal = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_DONATION_GOAL));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CAMPAIGN_STATUS));

                        pendingCampaignsList.add(new CampaignReviewItem(id, title, description, location, date,
                                imageUri, submitter, contactEmail, donationGoal, status));
                        itemsAdded++;
                        Log.d(TAG, "loadPendingCampaignsFromDb: Added item: " + title + " (Total items added in this load: " + itemsAdded + ")");

                    } while (cursor.moveToNext());
                } else {
                    Log.w(TAG, "loadPendingCampaignsFromDb: cursor.moveToFirst() was FALSE. No rows to process, even if count > 0.");
                }
            } else {
                Log.e(TAG, "loadPendingCampaignsFromDb: Cursor was null after calling dbHelper.getPendingCampaigns().");
            }
        } catch (Exception e) {
            Log.e(TAG, "loadPendingCampaignsFromDb: Exception while processing cursor.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
                Log.d(TAG, "loadPendingCampaignsFromDb: Cursor closed.");
            }
        }

        Log.d(TAG, "loadPendingCampaignsFromDb: Size of pendingCampaignsList before adapter update: " + pendingCampaignsList.size());
        campaignReviewAdapter.updateList(pendingCampaignsList); // This method should call notifyDataSetChanged()

        if (pendingCampaignsList.isEmpty()) {
            if (tvNoPendingCampaigns != null) tvNoPendingCampaigns.setVisibility(View.VISIBLE);
            if (recyclerViewCampaignReviews != null) recyclerViewCampaignReviews.setVisibility(View.GONE);
            Log.d(TAG, "loadPendingCampaignsFromDb: List is empty. Showing 'no pending campaigns' message.");
        } else {
            if (tvNoPendingCampaigns != null) tvNoPendingCampaigns.setVisibility(View.GONE);
            if (recyclerViewCampaignReviews != null) recyclerViewCampaignReviews.setVisibility(View.VISIBLE);
            Log.d(TAG, "loadPendingCampaignsFromDb: List is NOT empty. Showing RecyclerView.");
        }
        // This is the final log that was showing 0 previously
        Log.i(TAG, "loadPendingCampaignsFromDb: Finished. Loaded " + pendingCampaignsList.size() + " pending campaigns into the list for the adapter.");
    }

    @Override
    public void onApprove(CampaignReviewItem campaign, int position) {
        Log.d(TAG, "onApprove called for campaign: " + campaign.getTitle());
        boolean success = dbHelper.updateCampaignStatus(campaign.getId(), "approved");
        if (success) {
            Toast.makeText(this, "Campaign Approved: " + campaign.getTitle(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Campaign Approved in DB: " + campaign.getTitle() + " ID: " + campaign.getId());

            // It's generally better to reload the list from DB to ensure consistency,
            // or if removing, ensure the underlying pendingCampaignsList is also updated
            // if the adapter doesn't do it internally.
            // For now, let's assume adapter's removeItem handles its internal list.
            campaignReviewAdapter.removeItem(position);

            if (campaignReviewAdapter.getItemCount() == 0) {
                if (tvNoPendingCampaigns != null) tvNoPendingCampaigns.setVisibility(View.VISIBLE);
                if (recyclerViewCampaignReviews != null) recyclerViewCampaignReviews.setVisibility(View.GONE);
                Toast.makeText(this, "All pending submissions reviewed.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "All pending submissions reviewed. List is now empty.");
            }
        } else {
            Toast.makeText(this, "Failed to approve campaign: " + campaign.getTitle(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to approve campaign in DB: " + campaign.getTitle() + " ID: " + campaign.getId());
        }
    }

    @Override
    public void onReject(CampaignReviewItem campaign, int position) {
        Log.d(TAG, "onReject called for campaign: " + campaign.getTitle());
        boolean success = dbHelper.updateCampaignStatus(campaign.getId(), "rejected");
        if (success) {
            Toast.makeText(this, "Campaign Rejected: " + campaign.getTitle(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Campaign Rejected in DB: " + campaign.getTitle() + " ID: " + campaign.getId());

            campaignReviewAdapter.removeItem(position);

            if (campaignReviewAdapter.getItemCount() == 0) {
                if (tvNoPendingCampaigns != null) tvNoPendingCampaigns.setVisibility(View.VISIBLE);
                if (recyclerViewCampaignReviews != null) recyclerViewCampaignReviews.setVisibility(View.GONE);
                Toast.makeText(this, "All pending submissions reviewed.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "All pending submissions reviewed. List is now empty.");
            }
        } else {
            Toast.makeText(this, "Failed to reject campaign: " + campaign.getTitle(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to reject campaign in DB: " + campaign.getTitle() + " ID: " + campaign.getId());
        }
    }
}
