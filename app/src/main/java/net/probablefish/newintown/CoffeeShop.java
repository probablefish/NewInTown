package net.probablefish.newintown;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

class CoffeeShop implements Comparable<CoffeeShop>, Parcelable {

    private String venueName;
    private String category;

    private String[] address;
    private double latitude;
    private double longitude;


    CoffeeShop(JSONObject aCoffeeObject){
        try{
            JSONObject venue = aCoffeeObject.getJSONObject("venue");
            venueName = venue.getString("name");

            JSONArray categories = venue.getJSONArray("categories");
            category = categories.getJSONObject(0).getString("name");

            JSONObject location = venue.getJSONObject("location");
            JSONArray addressArray = location.getJSONArray("formattedAddress");
            address = new String[addressArray.length()];

            for (int i = 0; i < addressArray.length(); i++){
                address[i] = addressArray.getString(i);
            }

            latitude = location.getDouble("lat");
            longitude = location.getDouble("lng");


        }
        catch (JSONException e){
            Log.d("FOURSQUARE JSON", "Error parsing json");
        }

    }

    String getVenueName(){
        return venueName;
    }

    String[] getAddress(){
        return address;
    }

    double getLatitude(){
        return latitude;
    }

    double getLongitude(){
        return longitude;
    }

    String getCategory(){
        return category;
    }

    @Override
    public int compareTo(@NonNull CoffeeShop coffeeShop) {
        return this.venueName.compareTo(coffeeShop.venueName);
    }

    public String toString(){
        return "\n\nVenue: " + getVenueName()
                + "\nLat: " + getLatitude()
                + " Lon: " + getLongitude()
                + "\nCategory: " + getCategory()
                + "\nAddress: " + Arrays.toString(getAddress()) + "\n\n";
    }

    public CoffeeShop(Parcel parcel){
        category = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        venueName = parcel.readString();
        address = parcel.createStringArray();
    }

    ArrayList<CoffeeShop> coffeeShopArray;

    public static final Parcelable.Creator<CoffeeShop> CREATOR = new Parcelable.Creator<CoffeeShop>(){

        @Override
        public CoffeeShop createFromParcel(Parcel parcel) {
            return new CoffeeShop(parcel);
        }

        @Override
        public CoffeeShop[] newArray(int size) {
            return new CoffeeShop[size];
        }
    };

    public int describeContents() {
        return hashCode();
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(category);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(venueName);
        parcel.writeStringArray(address);
    }
}
