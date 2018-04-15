import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.floorDiv;

public class Demo {

    private static int graphStuff(String location, String destination, Hashtable<String,Train> trains){
        Graph<Train, DefaultWeightedEdge> g = new DirectedWeightedMultigraph<Train, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        //This table is used to keep track of depart times for each edge
        //Example: Train A to B leaves at 19:00
        Hashtable<Integer, Integer> departTimeEdges = new Hashtable<>();
        //O(T) trains
        for(String train: trains.keySet()){
            g.addVertex(trains.get(train));
        }
        //O(V) vertexes
        for(Train train: g.vertexSet()){
            if(train.getDestinations().size() > 0) {
                //O(t) times
                for(int times = 0; times < train.getDestinations().size(); times++) {
                    if(isDepartTimeOk(train.getDepartTimes().get(times)) && isArrivalTimeOk(train.getDepartTimes().get(times), train.getTravelTimes().get(times))) {
                        //I decided to use the travel times as the weight for each edge
                        DefaultWeightedEdge edge = g.addEdge(train, trains.get(train.getDestinations().get(times)));
                        g.setEdgeWeight(edge, train.getTravelTimes().get(times));
                        departTimeEdges.put(edge.hashCode(), train.getDepartTimes().get(times));
                    }
                }
            }
        }

        //I used this function provided by jgraph to find all possible simple paths
        AllDirectedPaths<Train, DefaultWeightedEdge> allDirectedPaths = new AllDirectedPaths<>(g);
        List<GraphPath<Train, DefaultWeightedEdge>> possiblePaths = allDirectedPaths.getAllPaths(trains.get(location), trains.get(destination), true, 25);
        if(possiblePaths.size() > 0){
            //keep track of the index of the currentPath
            int pathIndex = 0;
            //keep track of the index of the shortestPath
            int shortestPathIndex = -1;
            int shortestTime = 100000000;
            //O(P) paths
            for(GraphPath path: possiblePaths){
                //The time the train stops at a location
                //example train A leaves at 2 and the travel time is 3 so stopTime is 5
                int stopTime = -1;
                int totalTimeTravel = 0;
                List<DefaultWeightedEdge> edges = path.getEdgeList();
                //O(E) edges
                for(DefaultWeightedEdge edge: edges){
                    if(stopTime != -1){
                        //Making sure I do not divide by zero
                        if(stopTime == 0){
                            stopTime = 24;
                        }
                        int newDepartTime = departTimeEdges.get(edge.hashCode());
                        //This is the section that took me the most time and ultimately making it five days late...
                        //O(1) for each statement
                        if((stopTime > newDepartTime || (stopTime + newDepartTime) /24 > 0) && newDepartTime != stopTime) {
                            totalTimeTravel+=12;
                            int twelveHoursAhead = (stopTime + 12) % 24;
                            int difference;
                            if(twelveHoursAhead > newDepartTime){// O(1)
                                difference = 12 - (stopTime - newDepartTime);
                            } else {//O(1)
                               difference = newDepartTime - twelveHoursAhead;
                            }
                            totalTimeTravel+=difference;
                        } else if(newDepartTime == stopTime){ //add nothing if the new depart time is equal to the start time
                            totalTimeTravel+=0;
                        }
                        else {//O(1)
                         totalTimeTravel+= (stopTime + newDepartTime) %24;
                        }
                        totalTimeTravel+= (int)g.getEdgeWeight(edge);
                        stopTime = ((int)g.getEdgeWeight(edge) + departTimeEdges.get(edge.hashCode())) %24;
                    } else{ // O(1)
                        //add the travel time to the total timeTravel and calculate a new stopTime
                        totalTimeTravel +=(int)g.getEdgeWeight(edge);
                        stopTime = (departTimeEdges.get(edge.hashCode()) + (int)g.getEdgeWeight(edge)) %24 ;
                    }
                }
                //O(1)
                if(totalTimeTravel < shortestTime){
                    shortestTime = totalTimeTravel;
                    shortestPathIndex = pathIndex;
                }
                totalTimeTravel = 0;
                stopTime = -1;
                pathIndex++;
            }
            System.out.println("shortest total travel: " + shortestTime);
            System.out.println("path: " + possiblePaths.get(shortestPathIndex));
            return floorDiv(shortestTime, 24);
        } else {
            return -1;
        }
    }
    //Make sure the train leaves between 18 and 6 O(1)
    private static boolean isDepartTimeOk(int departTime) {
        return departTime % 24 >= 18 || departTime % 24 < 6;
    }
    //Make sure the train arrives between 18 and 6 O(1)
    private static boolean isArrivalTimeOk(int departTime, int timeTravel) {
        return (departTime + timeTravel) % 24 >= 18 || (departTime + timeTravel) % 24 <= 6;
    }

    public static void main(String args []) throws FileNotFoundException {
        int totalTests;
        int routes;
        int testCount = 0;
        int routeCount = 0;
        Hashtable<String, Train> trains = new Hashtable<>();
        String inputFile = "input.txt";
        String outputFile = "output.txt";
        File file = new File(inputFile);
        File outFile = new File(outputFile);
        PrintStream out = new PrintStream(outFile);
        if(file.exists()){
            Scanner in = new Scanner(file);
            //O(l) lines
            while(in.hasNext()){
                totalTests = in.nextInt();
                System.out.println("test: " + totalTests);
                //O(T) tests
                while(testCount < totalTests){
                    routes = in.nextInt();
                    System.out.println("routes: " + routes);
                    //O(R) routes it's exponential which is meh
                    //This loops creates a train object, add destinations, depart times, and travel times for each train
                    //then the train is put into a hashtable by its name
                    while(routeCount < routes) {
                        Train train;
                        String trainLocation = in.next();
                        if(!(trains.get(trainLocation) == null)){
                            train = trains.get(trainLocation);
                        } else {
                            train = new Train(trainLocation);
                        }
                        int index = train.addDestination(in.next(), in.nextInt(), in.nextInt());
                        if(trains.get(train.getDestinations().get(index)) == null) {
                            String destinationTrainLoc = train.getDestinations().get(index);
                            Train destinationTrain = new Train(destinationTrainLoc);
                            trains.put(destinationTrain.getLocation(), destinationTrain);
                            trains.put(train.getLocation(), train);
                        }

                        routeCount++;
                    }
                    //graphStuff() is exponential as well. so the performance is exponential
                    int litersOfBlood = graphStuff(in.next(),  in.next(), trains);
                    //Writes the result to the output file
                    out.println("Test Case " + (testCount + 1));
                    if(litersOfBlood < 0){
                        out.println("There's no path");
                    } else {
                        out.println("Vladimir needs " + litersOfBlood + " litre(s) of blood.");
                    }
                    //Resets the route count, increments the test number and clears the hashtable
                    routeCount = 0;
                    testCount++;
                    trains.clear();
                }
            }
            in.close();
        }

    }
}
