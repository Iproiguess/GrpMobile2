package com.example.grpmobile;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GrpMobile.db";
    private static final int DATABASE_VERSION = 2; // Incremented due to schema change (campaign ID fix)

    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id"; // Common convention for ID
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ROLE = "role"; // "User" or "Admin"

    // Campaign Table
    public static final String TABLE_CAMPAIGNS = "campaigns";
    public static final String COLUMN_CAMPAIGN_ID = "_id"; // Primary key for campaigns
    public static final String COLUMN_CAMPAIGN_TITLE = "title";
    public static final String COLUMN_CAMPAIGN_DESCRIPTION = "description";
    public static final String COLUMN_CAMPAIGN_LOCATION = "location";
    public static final String COLUMN_CAMPAIGN_DATE = "date";
    public static final String COLUMN_CAMPAIGN_IMAGE_URI = "image_uri";
    public static final String COLUMN_CAMPAIGN_SUBMITTER_USERNAME = "submitter_username"; // FK to users table username
    public static final String COLUMN_CAMPAIGN_CONTACT_EMAIL = "contact_email";
    public static final String COLUMN_CAMPAIGN_DONATION_GOAL = "donation_goal";
    public static final String COLUMN_CAMPAIGN_STATUS = "status"; // e.g., "pending", "approved", "rejected"
    public static final String COLUMN_CAMPAIGN_CURRENT_DONATION = "current_donation";


    // Create User Table SQL
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_ROLE + " TEXT NOT NULL"
            + ");";

    // Create Campaign Table SQL
    private static final String CREATE_TABLE_CAMPAIGNS = "CREATE TABLE " + TABLE_CAMPAIGNS + "("
            + COLUMN_CAMPAIGN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CAMPAIGN_TITLE + " TEXT NOT NULL,"
            + COLUMN_CAMPAIGN_DESCRIPTION + " TEXT,"
            + COLUMN_CAMPAIGN_LOCATION + " TEXT,"
            + COLUMN_CAMPAIGN_DATE + " TEXT,"
            + COLUMN_CAMPAIGN_IMAGE_URI + " TEXT,"
            + COLUMN_CAMPAIGN_SUBMITTER_USERNAME + " TEXT,"
            + COLUMN_CAMPAIGN_CONTACT_EMAIL + " TEXT,"
            + COLUMN_CAMPAIGN_DONATION_GOAL + " REAL DEFAULT 0,"
            + COLUMN_CAMPAIGN_STATUS + " TEXT NOT NULL DEFAULT 'pending',"  // Ensured NOT NULL and default
            + COLUMN_CAMPAIGN_CURRENT_DONATION + " REAL DEFAULT 0"
            + ");";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBHelper", "onCreate: Creating tables...");
        try {
            db.execSQL(CREATE_TABLE_USERS);
            Log.d("DBHelper", "onCreate: TABLE_USERS created.");
            db.execSQL(CREATE_TABLE_CAMPAIGNS);
            Log.d("DBHelper", "onCreate: TABLE_CAMPAIGNS created with status column: " + COLUMN_CAMPAIGN_STATUS);
        } catch (Exception e) {
            Log.e("DBHelper", "onCreate: Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAMPAIGNS);
        onCreate(db);
    }

    // User-related methods
    public boolean addUser(String username, String password, String email, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_ROLE, role);
        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
            Log.d("DBHelper", "addUser: User " + username + " insert result: " + result);
        } catch (Exception e) {
            Log.e("DBHelper", "Error adding user: " + username, e);
        } finally {
        }
        return result != -1;
    }

    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                    COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{username, password}, null, null, null);
            isValid = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e("DBHelper", "Error checking user credentials for " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isValid;
    }

    @SuppressLint("Range")
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String role = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_ROLE},
                    COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error getting user role for " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return role;
    }

    @SuppressLint("Range")
    public String getUserEmail(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String email = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL},
                    COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error getting user email for username: " + username, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return email;
    }

    // Campaign-related methods
    public boolean addCampaign(String title, String description, String location, String date,
                               String imageUri, String submitterUsername, String contactEmail,
                               double donationGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAMPAIGN_TITLE, title);
        values.put(COLUMN_CAMPAIGN_DESCRIPTION, description);
        values.put(COLUMN_CAMPAIGN_LOCATION, location);
        values.put(COLUMN_CAMPAIGN_DATE, date);
        values.put(COLUMN_CAMPAIGN_IMAGE_URI, imageUri);
        values.put(COLUMN_CAMPAIGN_SUBMITTER_USERNAME, submitterUsername);
        values.put(COLUMN_CAMPAIGN_CONTACT_EMAIL, contactEmail);
        values.put(COLUMN_CAMPAIGN_DONATION_GOAL, donationGoal);
        values.put(COLUMN_CAMPAIGN_STATUS, "pending"); // Explicitly setting status
        values.put(COLUMN_CAMPAIGN_CURRENT_DONATION, 0);

        long result = -1;
        try {
            // Using insertOrThrow to make errors more visible if schema is an issue.
            result = db.insertOrThrow(TABLE_CAMPAIGNS, null, values);
            Log.i("DBHelper", "addCampaign: '" + title + "', status explicitly set to 'pending'. Insert result: " + result);
        } catch (Exception e) {
            Log.e("DBHelper", "Error adding campaign in addCampaign: " + title, e);
            // result remains -1
        } finally {
            // db.close(); // Consider db lifecycle management
        }
        return result != -1;
    }

    public Cursor getPendingCampaigns() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CAMPAIGN_STATUS + " = ?";
        String[] selectionArgs = {"pending"}; // Querying for "pending"

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CAMPAIGNS, null, selection, selectionArgs, null, null, COLUMN_CAMPAIGN_ID + " DESC");
            if (cursor != null) {
                Log.d("DBHelper", "getPendingCampaigns: Query for status '" + selectionArgs[0] + "' found " + cursor.getCount() + " rows.");
            } else {
                Log.e("DBHelper", "getPendingCampaigns: Cursor is null after querying for status '" + selectionArgs[0] + "'.");
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error in getPendingCampaigns query", e);
            // Closing cursor here if an error occurs after it's been assigned but before return
            if (cursor != null) {
                cursor.close();
            }
            return null; // Or rethrow, or return an empty cursor from a MatrixCursor
        }
        // The calling method is responsible for closing the cursor if it's not null
        return cursor;
    }

    public Cursor getApprovedCampaigns() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CAMPAIGN_STATUS + " = ?";
        String[] selectionArgs = {"approved"};
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CAMPAIGNS, null, selection, selectionArgs, null, null, COLUMN_CAMPAIGN_ID + " DESC");
            if (cursor != null) {
                Log.d("DBHelper", "getApprovedCampaigns: Query for status '" + selectionArgs[0] + "' found " + cursor.getCount() + " rows.");
            } else {
                Log.e("DBHelper", "getApprovedCampaigns: Cursor is null after querying for status '" + selectionArgs[0] + "'.");
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error in getApprovedCampaigns query", e);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        return cursor;
    }

    public boolean updateCampaignStatus(int campaignId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAMPAIGN_STATUS, newStatus);
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_CAMPAIGNS, values, COLUMN_CAMPAIGN_ID + "=?",
                    new String[]{String.valueOf(campaignId)});
            Log.i("DBHelper", "updateCampaignStatus: ID " + campaignId + " to '" + newStatus + "'. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e("DBHelper", "Error updating campaign status for ID " + campaignId, e);
        } finally {
            // db.close();
        }
        return rowsAffected > 0;
    }

    @SuppressLint("Range")
    public CampaignReviewItem getCampaignById(int campaignId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        CampaignReviewItem campaign = null;
        try {
            cursor = db.query(TABLE_CAMPAIGNS, null, COLUMN_CAMPAIGN_ID + "=?",
                    new String[]{String.valueOf(campaignId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                campaign = new CampaignReviewItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_IMAGE_URI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_SUBMITTER_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_CONTACT_EMAIL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DONATION_GOAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_STATUS))
                );
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error getting campaign by ID: " + campaignId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return campaign;
    }

    // Method to dump all campaigns for debugging
    @SuppressLint("Range")
    public void logAllCampaigns() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Log.d("DBHelper_Dump", "--- Dumping ALL Campaigns ---");
        try {
            // Query all columns from the campaigns table
            cursor = db.query(TABLE_CAMPAIGNS, null, null, null, null, null, COLUMN_CAMPAIGN_ID + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Fetching all relevant campaign details
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DESCRIPTION));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_LOCATION));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DATE));
                    String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_IMAGE_URI));
                    String submitter = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_SUBMITTER_USERNAME));
                    String contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_CONTACT_EMAIL));
                    double donationGoal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DONATION_GOAL));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_STATUS));
                    double currentDonation = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_CURRENT_DONATION));

                    Log.d("DBHelper_Dump", "ID: " + id +
                            ", Title: '" + title + "'" +
                            // ", Desc: '" + description + "'" + // Can make log lengthy
                            // ", Loc: '" + location + "'" +
                            // ", Date: '" + date + "'" +
                            // ", ImgURI: '" + imageUri + "'" +
                            ", Submitter: '" + submitter + "'" +
                            ", Contact: '" + contactEmail + "'" +
                            ", Goal: " + donationGoal +
                            ", Status: '" + status + "'" + // Crucial for your issue
                            ", CurrentDon: " + currentDonation);
                } while (cursor.moveToNext());
                Log.d("DBHelper_Dump", "--- End of Campaign Dump (" + cursor.getCount() + " rows) ---");
            } else if (cursor != null) {
                Log.d("DBHelper_Dump", "No campaigns found in the table. Cursor count: 0");
            } else {
                Log.e("DBHelper_Dump", "Cursor is null when trying to dump all campaigns.");
            }
        } catch (Exception e) {
            Log.e("DBHelper_Dump", "Error dumping all campaigns", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close(); // Singleton pattern or careful management is better for db instances
        }
    }
}
