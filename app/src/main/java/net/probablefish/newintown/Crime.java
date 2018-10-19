package net.probablefish.newintown;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.apache.commons.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

class Crime implements Comparable<Crime>, Parcelable{

    private String category;
    private double latitude;
    private double longitude;
    private String locationName;
    private String monthCommitted;


    Crime(JSONObject crimeJSON){
        try {
            category = crimeJSON.getString("category").replace('-',' ');
            category = WordUtils.capitalizeFully(category);
            latitude = Double.parseDouble(crimeJSON.getJSONObject("location")
                                .getString("latitude"));
            longitude = Double.parseDouble(crimeJSON.getJSONObject("location")
                                .getString("longitude"));
            locationName = crimeJSON.getJSONObject("location")
                                .getJSONObject("street")
                                .getString("name");
            monthCommitted = crimeJSON.getString("month");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getMonthCommitted() {
        return monthCommitted;
    }

    @Override
    public int compareTo(@NonNull Crime crime) {
        int result = category.compareTo(crime.category);

        if(result == 0){
            result = locationName.compareTo(crime.locationName);
        }
        return result;
    }

    public String toString(){
        return "\n\nCategory: " + getCategory()
                + "\nLat: " + getLatitude()
                + " Lon: " + getLongitude()
                + "\nLocation Name: " + getLocationName()
                + "\nMonth: " + getMonthCommitted() + "\n\n";
    }

    public Crime(Parcel parcel){
        category = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        locationName = parcel.readString();
        monthCommitted = parcel.readString();
    }

    Crime[] crimesArray;

    public static final Parcelable.Creator<Crime> CREATOR = new Parcelable.Creator<Crime>(){

        @Override
        public Crime createFromParcel(Parcel parcel) {
            return new Crime(parcel);
        }

        @Override
        public Crime[] newArray(int size) {
            return new Crime[size];
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
        parcel.writeString(locationName);
        parcel.writeString(monthCommitted);
    }
}
