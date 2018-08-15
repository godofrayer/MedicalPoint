package com.project.medicalpoint;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.project.medicalpoint.fragment.FLocation;
import com.project.medicalpoint.fragment.FProfile;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MaterialSearchBar.OnSearchActionListener {

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    public static String searchConditions;
    private AppPreferences appPreferences;

    @BindView(R.id.searchBar)
    MaterialSearchBar searchBar;
    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    FLocation fLocation;
    FProfile fProfile;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(fProfile);
                    return true;
                case R.id.navigation_dashboard:
                    loadFragment(fLocation);
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        appPreferences = new AppPreferences(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fLocation = new FLocation();
        fProfile = new FProfile();

        searchBar.setOnSearchActionListener(this);
        List lastSearches = loadSearchSuggestionFromDisk();
        searchBar.setLastSuggestions(lastSearches);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private List loadSearchSuggestionFromDisk() {
        String lastSearch = appPreferences.getString("lastSearch", "");
        if (Objects.requireNonNull(lastSearch).isEmpty()) {
            return new ArrayList();
        }
        String[] lastSearchArr = lastSearch.split(";");
        return Arrays.asList(lastSearchArr);
    }

    private void saveSearchSuggestionToDisk() {
        List lastSearch = searchBar.getLastSuggestions();
        String lastSearchStr = TextUtils.join(";", lastSearch);
        appPreferences.put("lastSearch", lastSearchStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveSearchSuggestionToDisk();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FLocation.REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fLocation = new FLocation();
                    loadFragment(fLocation);
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> taskSetting = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        taskSetting.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    loadFragment(fProfile);
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                finish();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            finish();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                loadFragment(fLocation);
            } else {
                finish();
            }
        }
    }

    public FirebaseFirestore getDatabase() {
        return database;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        searchConditions = text.toString();
        loadFragment(fLocation);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }

    public FirebaseStorage getStorage() {
        return storage;
    }
}