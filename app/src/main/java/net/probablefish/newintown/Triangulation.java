package net.probablefish.newintown;


class Triangulation {

    private final static double latOffset = 0.01;
    private final static double lonOffset = 0.015;

    static String[][] triangulate(double latitude, double longitude){
        return new String[][] {
                getPoint1(latitude, longitude),
                getPoint2(latitude, longitude),
                getPoint3(latitude, longitude)
                };
    }

    private static String[] getPoint1(double latitude, double longitude){
        double point1Lat = latitude + latOffset;
        double point1Lon = longitude + lonOffset;
        return new String[]{String.format("%.6f",point1Lat), String.format("%.6f", point1Lon)};
    }

    private static String[] getPoint2(double latitude, double longitude){
        double point2Lat = latitude + latOffset;
        double point2Lon = longitude - lonOffset;
        return new String[] {String.format("%.6f", point2Lat), String.format("%.6f", point2Lon)};
    }

    private static String[] getPoint3(double latitude, double longitude){
        double point3Lat = latitude - latOffset;
        return new String[] {String.format("%.6f", point3Lat), String.format("%.6f", longitude)};
    }
}
