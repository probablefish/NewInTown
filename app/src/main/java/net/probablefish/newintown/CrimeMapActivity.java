package net.probablefish.newintown;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class CrimeMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    double locationLatitude;
    double locationLongitude;

    private String crimeDate;

    private Parcelable[] crimes;
    private HashMap<String, Integer> countsByCategory;
    private HashMap<String, ArrayList<Crime>> crimesByCategory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_map);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getExtras() != null) {
            locationLatitude = getIntent().getDoubleExtra("latitude", 0.0);
            locationLongitude = getIntent().getDoubleExtra("longitude", 0.0);
            crimeDate = getIntent().getStringExtra("crimeDate");
            crimes = getIntent().getExtras().getParcelableArray("crimes");
            // countsByCategory = (HashMap<String, Integer>) getIntent().getSerializableExtra("countsByCategory");
            // crimesByCategory =
               //  (HashMap<String, ArrayList<Crime>>) getIntent().getSerializableExtra("crimesByCategory");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.crimeMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latLng = new LatLng(locationLatitude, locationLongitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        populateMap();
    }


    private void populateMap() {
        for (Parcelable aCrime : crimes){
            Crime crime = (Crime) aCrime;
            LatLng crimeLatLng = new LatLng(crime.getLatitude(), crime.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(crimeLatLng)
                    .title(crime.getCategory());

            float markerColor = getMarkerColour(crime.getCategory());

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerColor));
            map.addMarker(markerOptions);
        }
    }

    private float getMarkerColour(String crimeCategory){
        float markerColor;
        switch (crimeCategory){
            case "Anti Social Behaviour" :
                markerColor = BitmapDescriptorFactory.HUE_BLUE;
                break;
            case "Public Order" :
                markerColor = BitmapDescriptorFactory.HUE_CYAN;
                break;
            case "Violent Crime" :
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
                break;
            case "Criminal Damage Arson" :
                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                break;
            case "Burglary" :
                markerColor = BitmapDescriptorFactory.HUE_VIOLET;
                break;
            case "Bicycle Theft" :
                markerColor = BitmapDescriptorFactory.HUE_YELLOW;
                break;
            default :
                markerColor = BitmapDescriptorFactory.HUE_RED;
        }
        return markerColor;
    }
}
