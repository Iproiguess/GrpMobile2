package com.example.grpmobile;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.SupabaseClientBuilder;
import io.github.jan.supabase.auth.Auth;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.realtime.Realtime;
import io.github.jan.supabase.storage.Storage;

public class SupabaseHelper {

    private static SupabaseClient client = null;
    private static final Object LOCK = new Object(); // For thread safety during initialization

    // Private constructor to prevent direct instantiation
    private SupabaseHelper() {
    }

    public static SupabaseClient getClient() {
        // Double-checked locking for thread-safe singleton initialization
        if (client == null) {
            synchronized (LOCK) {
                if (client == null) {
                    String supabaseUrl = "https://gtxzgctkknsozyxkjwmf.supabase.co";
                    String supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0eHpnY3Rra25zb3p5eGtqd21mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc5MTYwNTcsImV4cCI6MjA3MzQ5MjA1N30.9IH81I7tE7WiJsy7APcFscZpkszsr-pUgeflunSANOw"; // Your Anon Key

                    client = new SupabaseClientBuilder(supabaseUrl, supabaseAnonKey)
                            .addPlugin(Auth.INSTANCE)
                            .addPlugin(Postgrest.INSTANCE)
                            .addPlugin(Realtime.INSTANCE)
                            .addPlugin(Storage.INSTANCE)
                            .build();
                }
            }
        }
        return client;
    }
}
