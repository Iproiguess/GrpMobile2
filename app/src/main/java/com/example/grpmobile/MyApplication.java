package com.example.grpmobile;

import android.app.Application;
import io.github.jan.supabase.SupabaseClient; // Still need this for the type

public class MyApplication extends Application {

    // You don't necessarily need to store the client here anymore
    // if SupabaseHelper.getClient() always returns the same instance.

    @Override
    public void onCreate() {
        super.onCreate();
        // You can ensure the client is initialized at startup if needed,
        // or just call SupabaseHelper.getClient() wherever you need it.
        SupabaseHelper.getClient(); // This will initialize it if not already done.
    }

    // You could provide this method, or components can call SupabaseHelper.getClient() directly
    public static SupabaseClient getSupabaseClient() {
        return SupabaseHelper.getClient();
    }
}