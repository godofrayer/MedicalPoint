package com.project.medicalpoint.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.medicalpoint.MainActivity;
import com.project.medicalpoint.R;
import com.project.medicalpoint.entity.MedicalPoint;
import com.project.medicalpoint.util.DirectionsJSONParser;
import com.project.medicalpoint.util.RecyclerViewEmptySupport;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FLocation extends Fragment implements OnMapReadyCallback, LocationListener {

    public static GoogleMap map;
    public static Polyline polyline;
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    CategoryPagerAdapter categoryPagerAdapter;
    @BindView(R.id.vp_category)
    ViewPager viewPager;

    public static LatLng currentLocation;

    public FLocation() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flocation, container, false);
        ButterKnife.bind(this, view);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .add(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        enableMyLocation();
    }

    public void refreshList() {
        categoryPagerAdapter = new CategoryPagerAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager());
        viewPager.setAdapter(categoryPagerAdapter);
    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> lastLocation = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity())).getLastLocation();
        lastLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.getResult() != null) {
                    currentLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));
                    refreshList();
                } else {
                    getCurrentLocation();
                }
            }
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public class CategoryPagerAdapter extends FragmentStatePagerAdapter {
        private CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new CategoryObjectFragment();
            Bundle args = new Bundle();
            args.putInt(CategoryObjectFragment.TAB_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SpannableStringBuilder sb;
            Drawable drawable;
            ImageSpan span;
            switch (position) {
                case 0:
                    sb = new SpannableStringBuilder("   Rumah Sakit");
                    drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.hospital);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return sb;
                case 1:
                    sb = new SpannableStringBuilder("   Klinik");
                    drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.clinic);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return sb;
                case 2:
                    sb = new SpannableStringBuilder("   Apotik");
                    drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.store);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return sb;
            }
            return "No Title";
        }
    }

    public static class CategoryObjectFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
        public static final String TAB_NUMBER = "tab_number";

        @BindView(R.id.rv_medical)
        RecyclerViewEmptySupport rvMedical;
        @BindView(R.id.srl_empty)
        SwipeRefreshLayout srlEmpty;
        @BindView(R.id.srl_full)
        SwipeRefreshLayout srlFull;

        int filterCategory;
        ArrayList<Marker> markers;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_category, container, false);
            ButterKnife.bind(this, view);
            markers = new ArrayList<>();
            Bundle args = getArguments();
            filterCategory = Objects.requireNonNull(args).getInt(TAB_NUMBER);
            MedicalPointAdapter medicalPointAdapter = new MedicalPointAdapter(new ArrayList<MedicalPoint>(), getActivity());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            rvMedical.setLayoutManager(layoutManager);
            rvMedical.setAdapter(medicalPointAdapter);
            rvMedical.setEmptyView(srlEmpty);
            srlEmpty.setOnRefreshListener(this);
            srlFull.setOnRefreshListener(this);
            srlEmpty.setColorSchemeResources(R.color.colorPrimary,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_orange_dark,
                    android.R.color.holo_blue_dark);
            srlFull.setColorSchemeResources(R.color.colorPrimary,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_orange_dark,
                    android.R.color.holo_blue_dark);
            srlFull.post(new Runnable() {
                @Override
                public void run() {
                    loadDataMedicalPoint(MainActivity.searchConditions == null ? "" : MainActivity.searchConditions);
                }
            });
            return view;
        }

        public void loadDataMedicalPoint(final String search) {
            srlFull.setRefreshing(true);
            srlEmpty.setRefreshing(true);
            FirebaseFirestore database = ((MainActivity) Objects.requireNonNull(getActivity())).getDatabase();
            database.collection("MedicalPoint")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<MedicalPoint> medicalPoints = new ArrayList<>();
                            if (!markers.isEmpty()) {
                                for (Marker marker : markers) {
                                    marker.remove();
                                }
                            }
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    MedicalPoint medicalPoint = documentSnapshot.toObject(MedicalPoint.class);
                                    if (medicalPoint.getName().toLowerCase().contains(search.toLowerCase()) ||
                                            medicalPoint.getDiseases().toLowerCase().contains(search.toLowerCase()) ||
                                            medicalPoint.getAddress().toLowerCase().contains(search.toLowerCase()) ||
                                            medicalPoint.getDoctors().toLowerCase().contains(search.toLowerCase())) {
                                        float[] result = new float[5];
                                        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                                                medicalPoint.getLatitude(), medicalPoint.getLongitude(), result);
                                        medicalPoint.setDistance(result[0]/1000);
                                        LatLng latLng = new LatLng(medicalPoint.getLatitude(), medicalPoint.getLongitude());
                                        switch (medicalPoint.getCategory()) {
                                            case "RS":
                                                if (filterCategory == 0) {
                                                    medicalPoints.add(medicalPoint);
                                                }
                                                markers.add(map.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(medicalPoint.getName())
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital))));
                                                break;
                                            case "Klinik":
                                                if (filterCategory == 1) {
                                                    medicalPoints.add(medicalPoint);
                                                }
                                                markers.add(map.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(medicalPoint.getName())
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.clinic))));
                                                break;
                                            case "Apotik":
                                                if (filterCategory == 2) {
                                                    medicalPoints.add(medicalPoint);
                                                }
                                                markers.add(map.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(medicalPoint.getName())
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.store))));
                                                break;
                                        }
                                    }
                                }
                                Collections.sort(medicalPoints);
                                MedicalPointAdapter medicalPointAdapter = new MedicalPointAdapter(medicalPoints, getActivity());
                                rvMedical.setAdapter(medicalPointAdapter);
                            } else {
                                Log.e(FLocation.class.getName(),
                                        Objects.requireNonNull(task.getException()).getLocalizedMessage(), task.getException());
                            }
                            srlEmpty.setRefreshing(false);
                            srlFull.setRefreshing(false);
                        }
                    });
        }

        @Override
        public void onRefresh() {
            loadDataMedicalPoint(MainActivity.searchConditions == null ? "" : MainActivity.searchConditions);
        }
    }

    public static class MedicalPointAdapter extends RecyclerView.Adapter<MedicalPointAdapter.MedicalPointViewHolder> {

        private ArrayList<MedicalPoint> medicalPoints;
        WeakReference<Context> contextWeakReference;

        private MedicalPointAdapter(ArrayList<MedicalPoint> medicalPoints, Context context) {
            this.medicalPoints = medicalPoints;
            this.contextWeakReference = new WeakReference<>(context);
        }

        @NonNull
        @Override
        public MedicalPointAdapter.MedicalPointViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View view = layoutInflater.inflate(R.layout.row_category, viewGroup, false);
            return new MedicalPointViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MedicalPointAdapter.MedicalPointViewHolder medicalPointViewHolder, int i) {
            final int position = medicalPointViewHolder.getAdapterPosition();
            medicalPointViewHolder.tvName.setText(medicalPoints.get(position).getName());
            medicalPointViewHolder.tvAddress.setText(medicalPoints.get(position).getAddress());
            medicalPointViewHolder.tvPhone.setText(medicalPoints.get(position).getPhone());
            medicalPointViewHolder.tvDistance.setText(String.format(Locale.US, "\u00B1%.2f KM", medicalPoints.get(position).getDistance()));
            medicalPointViewHolder.ibMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = getDirectionsUrl(currentLocation, new LatLng(medicalPoints.get(position).getLatitude()
                            , medicalPoints.get(position).getLongitude()));
                    new DownloadTask(contextWeakReference.get()).execute(url);
                }
            });
            medicalPointViewHolder.ibPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + medicalPoints.get(position).getPhone()));
                    contextWeakReference.get().startActivity(intent);
                }
            });
            medicalPointViewHolder.ibShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "http://maps.google.com/maps?f=d&daddr=" + medicalPoints.get(position).getLatitude() + "," +
                            medicalPoints.get(position).getLongitude() + "&dirflg=d&layer=t";
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, medicalPoints.get(position).getName() + "\n" + url);
                    contextWeakReference.get().startActivity(whatsappIntent);
                }
            });
        }

        private static class DownloadTask extends AsyncTask<String, Integer, String> {
            MaterialDialog materialDialog;
            WeakReference<Context> contextWeakReference;

            private DownloadTask(Context context) {
                this.contextWeakReference = new WeakReference<>(context);
            }

            @Override
            protected void onPreExecute() {
                materialDialog = new MaterialDialog.Builder(contextWeakReference.get())
                        .title("Mohon tunggu sebentar...")
                        .content("Sedang mengambil rute terdekat")
                        .progress(true, 0)
                        .autoDismiss(false)
                        .progressIndeterminateStyle(true)
                        .show();
            }

            @Override
            protected String doInBackground(String... url) {
                String data = "";
                try {
                    data = downloadUrl(url[0]);
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                materialDialog.dismiss();
                new ParserTask(contextWeakReference.get()).execute(result);
            }
        }

        private static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
            MaterialDialog materialDialog;
            WeakReference<Context> contextWeakReference;
            double destinationLatitude;
            double destinationLongitude;

            private ParserTask(Context context) {
                this.contextWeakReference = new WeakReference<>(context);
            }

            @Override
            protected void onPreExecute() {
                materialDialog = new MaterialDialog.Builder(contextWeakReference.get())
                        .title("Mohon tunggu sebentar...")
                        .content("Sedang menggambar rute pada map")
                        .progress(true, 0)
                        .autoDismiss(false)
                        .progressIndeterminateStyle(true)
                        .show();
            }

            // Parsing the data in non-ui thread
            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;
                try {
                    jObject = new JSONObject(jsonData[0]);
                    DirectionsJSONParser parser = new DirectionsJSONParser();
                    routes = parser.parse(jObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return routes;
            }

            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points;
                PolylineOptions lineOptions = null;
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = result.get(i);
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        destinationLatitude = lat;
                        destinationLongitude = lng;
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.BLUE);
                    lineOptions.geodesic(true);
                }
                // Drawing polyline in the Google Map for the i-th route
                if (polyline != null) {
                    polyline.remove();
                }
                materialDialog.dismiss();
                if (lineOptions == null) {
                    new MaterialDialog.Builder(contextWeakReference.get())
                            .title("Peringatan")
                            .icon(Objects.requireNonNull(ContextCompat.getDrawable(contextWeakReference.get(),
                                    R.drawable.warning)))
                            .content("Gagal mendapatkan rute terdekat. Silahkan coba kembali")
                            .positiveText("Tutup")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    polyline = map.addPolyline(lineOptions);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.latitude, currentLocation.longitude), 16.0f));
                    new MaterialDialog.Builder(contextWeakReference.get())
                            .title("Konfirmasi")
                            .icon(Objects.requireNonNull(ContextCompat.getDrawable(contextWeakReference.get(),
                                    R.drawable.confirm)))
                            .content("Apakah Anda ingin menggunakan aplikasi Google Navigator?")
                            .positiveText("Ya")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    String url = "http://maps.google.com/maps?f=d&daddr=" + destinationLatitude + "," + destinationLongitude + "&dirflg=d&layer=t";
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                    contextWeakReference.get().startActivity(intent);
                                }
                            })
                            .negativeText("Tidak")
                            .show();
                }
            }
        }

        private String getDirectionsUrl(LatLng origin, LatLng dest) {
            // Origin of route
            String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
            // Destination of route
            String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
            // Sensor enabled
            String sensor = "sensor=false";
            String mode = "mode=driving";
            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
            // Output format
            String output = "json";
            // Building the url to the web service
            return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        }

        private static String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                Objects.requireNonNull(iStream).close();
                urlConnection.disconnect();
            }
            return data;
        }

        @Override
        public int getItemCount() {
            return medicalPoints.size();
        }

        public class MedicalPointViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tv_name)
            TextView tvName;
            @BindView(R.id.tv_address)
            TextView tvAddress;
            @BindView(R.id.tv_distance)
            TextView tvDistance;
            @BindView(R.id.tv_phone)
            TextView tvPhone;
            @BindView(R.id.ib_map)
            ImageButton ibMap;
            @BindView(R.id.ib_phone)
            ImageButton ibPhone;
            @BindView(R.id.ib_share)
            ImageButton ibShare;

            private MedicalPointViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
