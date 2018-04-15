import java.io.Serializable;
import java.util.ArrayList;

public class Train {

    private String location;
    private ArrayList<String> destinations;
    private ArrayList<Integer> departTimes;

    public ArrayList<Integer> getTravelTimes() {
        return travelTimes;
    }

    public void setTravelTimes(ArrayList<Integer> travelTimes) {
        this.travelTimes = travelTimes;
    }

    private ArrayList<Integer> travelTimes;
    private int travelTime;


    public Train(String location) {
        this.location = location;
        //this.duplicates = 0;
        this.destinations = new ArrayList<>();
        this.departTimes = new ArrayList<>();
        this.travelTimes = new ArrayList<>();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    public ArrayList<String> getDestinations(){
        return destinations;
    }

    public ArrayList<Integer> getDepartTimes(){
        return departTimes;
    }

    public int addDestination(String destination, int departTime, int travelTime){
           this.destinations.add(destination);
           this.departTimes.add(departTime);
           this.travelTimes.add(travelTime);
           return this.destinations.lastIndexOf(destination);
    }

    public void alterDepartTime(String destination, int departTime){
        int index = destinations.indexOf(destination);
        departTimes.set(index, departTime);
    }

    @Override
    public String toString() {
        return location;
    }

}
