    package net.probablefish.newintown;

    import android.Manifest;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.support.v4.app.ActivityCompat;
    import android.support.v4.content.ContextCompat;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.LinearLayout;
    import android.widget.TextView;

    import com.android.volley.Request;
    import com.android.volley.RequestQueue;
    import com.android.volley.Response;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.StringRequest;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.tasks.OnSuccessListener;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.Locale;


    public class MainActivity extends AppCompatActivity {

        private RequestQueue queue;

        private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
        private FusedLocationProviderClient mFusedLocationClient;

        private double currentLocationLatitude;
        private double currentLocationLongitude;
        private Location currentLocation;

        private Crime[] crimes;
        private HashMap<String, Integer> countsByCategory;
        private HashMap<String, ArrayList<Crime>> crimesByCategory;
        private Calendar crimeCalendar;
        private String crimeDate;

        ArrayList<CoffeeShop> coffeeShops;
        final static int CAFE_LIMIT = 6;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            queue = Queue.getInstance(this).getRequestQueue();

            setCrimeDate();

            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                }
            } else {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    currentLocation = location;
                                    getCurrentLocation();
                                    makeCrimeRequest();
                                    makeFoursquareCoffeeRequest();
                                }
                            }
                        });
            }
        }

        private void makeFoursquareCoffeeRequest(){
            String id = getString(R.string.FOURSQUARE_CLIENT_ID);
            String secret = getString(R.string.FOURSQUARE_CLIENT_SECRET);
            String url = "https://api.foursquare.com/v2/venues/explore"
                    + "?client_id=" + id
                    + "&client_secret=" + secret
                    + "&ll=" + currentLocationLatitude + "," + currentLocationLongitude
                    + "&section=coffee"
                    + "&sortByDistance=1"
                    + "&limit=" + CAFE_LIMIT
                    + "&radius=10000"
                    + "&v=20181009"; // version number of API that we are configured for (date)
            StringRequest request = buildFoursquareRequest(url);
            queue.add(request);
        }

        private void getCurrentLocation() {
            currentLocationLatitude = currentLocation.getLatitude();
            currentLocationLongitude = currentLocation.getLongitude();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                        if (ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED){
                            mFusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if (location != null) {
                                                currentLocation = location;
                                                getCurrentLocation();
                                                makeCrimeRequest();
                                                makeFoursquareCoffeeRequest();
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d("PERMISSIONS", "Denied. Need Access Coarse Location permission");
                    }
                }
            }
        }

        private void makeCrimeRequest(){
            String url = buildCrimeURL(currentLocationLatitude, currentLocationLongitude, crimeDate);
            StringRequest request = buildCrimeRequest(url);
            queue.add(request);
        }

        private String buildCrimeURL(double latitude, double longitude, String date){
            String[][] triangleCoords = Triangulation.triangulate(latitude, longitude);
            return "https://data.police.uk/api/crimes-street/all-crime?poly="
                    + triangleCoords[0][0] + ","
                    + triangleCoords[0][1] + ":"
                    + triangleCoords[1][0] + ","
                    + triangleCoords[1][1] + ":"
                    + triangleCoords[2][0] + ","
                    + triangleCoords[2][1]
                    + "&date=" + date;
        }

        private StringRequest buildFoursquareRequest(String url){
            return new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response){
                                coffeeShops = new ArrayList<>();
                                JSONArray items = getItemsFromResponse(response);

                                if (items != null){
                                    addItemsToCoffeeShopList(items);
                                }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("RESPONSE", error.getMessage());
                        }
                    });
        }

        private JSONArray getItemsFromResponse(String response){
            try {
                JSONObject toJSON = new JSONObject(response);
                JSONObject responseObject = toJSON.getJSONObject("response");
                JSONArray groups = responseObject.getJSONArray("groups");
                JSONObject groupObject = groups.getJSONObject(0);
                return groupObject.getJSONArray("items");
            } catch (JSONException e){
                Log.e("JSON", "JSON Exception: Error parsing response for coffee, returning null");
                return null;
            }
        }

        private void addItemsToCoffeeShopList(JSONArray items) {
            LinearLayout coffeeListLL = findViewById(R.id.coffee_list_LL);

            try {
                for (int i = 0; i < items.length(); i++) {
                    CoffeeShop coffeeShop = new CoffeeShop(items.getJSONObject(i));
                    coffeeShops.add(coffeeShop);

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = inflater.inflate(R.layout.cafe_details_layout, null);

                    TextView cafeNameTV = view.findViewById(R.id.cafe_name);
                    coffeeListLL.addView(view);
                    cafeNameTV.setText(coffeeShop.getVenueName());

                    TextView addressTV = view.findViewById(R.id.cafe_address);
                    String addressText = "";
                    for (int j = 0; j < coffeeShop.getAddress().length; j++) {
                        addressText += coffeeShop.getAddress()[j] + "\n";
                        addressTV.setText(addressText);
                    }
                }
            } catch (JSONException e){
                Log.e("JSON ERROR", "Error parsing JSONObject to CoffeeShop");
            }

        }


        private StringRequest buildCrimeRequest(String url){

            return new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jsonResponse = new JSONArray(response);

                                populateCrimesCountsAndCategories(jsonResponse);

                                String labelText = getString(R.string.crime_card_total_label) + " " +
                                        crimeCalendar.getDisplayName(
                                                Calendar.MONTH,
                                                Calendar.LONG,
                                                Locale.ENGLISH);
                                TextView totalCrimesTV = findViewById(R.id.crimeCardTotalCrimes);
                                TextView crimeCardTotalLabel = findViewById(R.id.crimeCardTotalLabel);
                                crimeCardTotalLabel.setText(labelText);
                                totalCrimesTV.setText(Integer.toString(numberOfCrimes()));

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("RESPONSE", "Not converted to JSONArray. JSONException");
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("RESPONSE", "error: bad response from server");
                        }
                    });

        }

        private void populateCrimesCountsAndCategories(JSONArray aJSONArray){
            crimes = new Crime[aJSONArray.length()];
            countsByCategory = new HashMap<>();
            crimesByCategory = new HashMap<>();
            for (int i = 0; i < aJSONArray.length(); i++){
                try {
                    JSONObject crimeJSON = aJSONArray.getJSONObject(i);
                    Crime crime = new Crime(crimeJSON);

                    if(!countsByCategory.containsKey(crime.getCategory())){
                        countsByCategory.put(crime.getCategory(), 1);

                        ArrayList<Crime> crimeArrayList = new ArrayList<>();
                        crimeArrayList.add(crime);

                        crimesByCategory.put(crime.getCategory(), crimeArrayList);
                    }
                    else{
                        countsByCategory.put(crime.getCategory(),
                                countsByCategory.get(crime.getCategory()).intValue() + 1);
                        crimesByCategory.get(crime.getCategory()).add(crime);
                    }

                    crimes[i] = crime;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Arrays.sort(this.crimes);
        }

        private int numberOfCrimes(){
            return crimes.length;
        }

        private void setCrimeDate(){
            int monthOffset = -2;
            crimeCalendar = Calendar.getInstance();
            crimeCalendar.add(Calendar.MONTH, monthOffset);
            crimeDate = "" + crimeCalendar.get(Calendar.YEAR) + "-" +
                    (crimeCalendar.get(Calendar.MONTH) + 1);
        }

        public void startCrimeMapActivity(View view){
            Intent mapIntent;
            mapIntent = new Intent(this, CrimeMapActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelableArray("crimes", crimes);
            extras.putDouble("latitude", currentLocationLatitude);
            extras.putDouble("longitude", currentLocationLongitude);
            extras.putString("crimeDate", crimeDate);
            mapIntent.putExtras(extras);
            startActivity(mapIntent);
        }

        public void startCoffeeMapActivity(View view) {
            Intent coffeeIntent;
            coffeeIntent = new Intent(this, CafeMapActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelableArrayList("coffeeShops", coffeeShops);
            extras.putDouble("latitude", currentLocationLatitude);
            extras.putDouble("longitude", currentLocationLongitude);
            coffeeIntent.putExtras(extras);
            startActivity(coffeeIntent);
        }


    }
