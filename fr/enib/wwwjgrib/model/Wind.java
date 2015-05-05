/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.model;

/**
 * Represents a wind
 * @author s8lepage
 */
public class Wind {

    private double longitude;
    private double latitude;
    private double speed;
    private double direction;

    /**
     *
     * @param Longitude
     * @param Latitude
     * @param Speed
     * @param Direction
     */
    public Wind(double Longitude, double Latitude, double Speed, double Direction) {
        this.longitude = Longitude;
        this.latitude = Latitude;
        this.speed = Speed;
        this.direction = Direction;
    }

    /**
     * Creates a Wind with longitude 0, latitude 0, speed 0, and direction 0.
     */
    public Wind() {
        this(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        String buffer;
        buffer = "[Latitude=" + getLatitude() + " Longitude=" + getLongitude() + "]\n";
        buffer = buffer + "WindSpeed=" + getWindSpeed() + " Direction=" + getDirection() + "\n";
        return buffer;
    }

    /**
     *
     * @return the windSpeed
     */
    public double getWindSpeed() {
        return speed;
    }

    /**
     * @return the Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param Longitude the Longitude to set
     */
    public void setLongitude(double Longitude) {
        this.longitude = Longitude;
    }

    /**
     * @return the Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param Latitude the Latitude to set
     */
    public void setLatitude(double Latitude) {
        this.latitude = Latitude;
    }

    /**
     * @return the Speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * @param Speed the Speed to set
     */
    public void setSpeed(double Speed) {
        this.speed = Speed;
    }

    /**
     * @return the Direction
     */
    public double getDirection() {
        return direction;
    }

    /**
     * @param Direction the Direction to set
     */
    public void setDirection(double Direction) {
        this.direction = Direction;
    }
}
