package net.probablefish.newintown;

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

public class CafeMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    double locationLatitude;
    double locationLongitude;

    private ArrayList<CoffeeShop> coffeeShops;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_map);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getExtras() != null) {
            locationLatitude = getIntent().getDoubleExtra("latitude", 0.0);
            locationLongitude = getIntent().getDoubleExtra("longitude", 0.0);
            coffeeShops = getIntent().getExtras().getParcelableArrayList("coffeeShops");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.cafeMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latLng = new LatLng(locationLatitude, locationLongitude);
        centerCameraOnLocation(latLng);
        populateMap();
    }

    private void centerCameraOnLocation(LatLng latLng) {
        int zoomLevel = 14;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }


    private void populateMap() {
        for (CoffeeShop aCoffeeShop : coffeeShops){
            LatLng coffeeLatLng = new LatLng(aCoffeeShop.getLatitude(), aCoffeeShop.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions().position(coffeeLatLng)
                    .title(aCoffeeShop.getVenueName());

            float markerColor = BitmapDescriptorFactory.HUE_CYAN;
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerColor));

            map.addMarker(markerOptions);
        }
    }
}
