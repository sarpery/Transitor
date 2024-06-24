package com.example.Backend.Algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.Backend.Graph.AdjacencyListGraph;
import com.example.Backend.Graph.EdgeNode;
public class BusDijkstra<T> {
    private AdjacencyListGraph<T> graph;
    private Map<T, Long> resultTimes = new HashMap<>();
    private Algorithms<String> alg = new Algorithms<>();
    private String time;
    private Long timeCost;
    public BusDijkstra(AdjacencyListGraph<T> graph1){

        this.graph = graph1;
    }

    public List<String> dijkstra(T start, T target, String time) {
        this.time = time;
        Map<T, Integer> distance = new HashMap<>();
        Map<T, String> previousTrips = new HashMap<>();
        Map<T, T> previousStops = new HashMap<>();
        PriorityQueue<T> priorityQueue = new PriorityQueue<>((u, v) -> Math.toIntExact(resultTimes.get(u) - resultTimes.get(v)));
        Set<T> visited = new HashSet<>();
        

        // Initialize distances
        for (T vertex : graph.verteces.keySet()) {
            resultTimes.put(vertex, (long)Integer.MAX_VALUE);
            previousTrips.put(vertex, null);
            previousStops.put(vertex, null);
        }
        resultTimes.put(start, (long)0);
        distance.put(start, 0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            T current = priorityQueue.poll();
            if (current.equals(target)) {
                System.out.println("!!!");
                break; // Reached the target
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            for (EdgeNode<T> neighbor : graph.neighbors(current)) {
                T next = neighbor.destination_point;
                Long newTime;
                if(neighbor.getName().startsWith("null"))
                    newTime = calcTimeWalking(current, neighbor);
                else
                    newTime = calcTimeBus(current, neighbor);
                if (newTime < resultTimes.get(next)) {
                    resultTimes.put(next, newTime);
                    previousTrips.put(next, neighbor.getName());
                    previousStops.put(next, current);
                    priorityQueue.add(next);
                }
            }
        }

        // Reconstruct the path from start to target
        LinkedList<T> path = new LinkedList<>();
        LinkedList<String> trips = new LinkedList<>();
        T current = target;
        trips.add(previousTrips.get(target));
        path.add(target);
        while (current != null) {
            if(trips.getFirst().equals(previousTrips.get(current)) && !previousTrips.get(current).startsWith("null"))
                current = previousStops.get(current);
            else
            {
                trips.add(0, previousTrips.get(current));
                path.add(0, current);
                current = previousStops.get(current);
            }
        }

        LinkedList<String> result = new LinkedList<>();

        Iterator it1 = path.iterator();
        Iterator it2 = trips.iterator();
        

        while(it1.hasNext())
        {
            result.add(it1.next() + "-" + it2.next());
        }

        timeCost = resultTimes.get(target);
        return result;
    }

    public Long getTime(){
        return timeCost;
    }

    private Long calcTimeBus(T startPoint, EdgeNode<T> edge){
        
        Long currentTime = resultTimes.get(startPoint);
        Long reachTime = alg.parseDuration(edge.departure_time).minus(alg.parseDuration(time)).toMinutes();

        if(reachTime - currentTime < 0) return (long)Integer.MAX_VALUE;

        return alg.parseDuration(edge.arrival_time).minus(alg.parseDuration(time)).toMinutes();
    }

    private Long calcTimeWalking(T startPoint, EdgeNode<T> edge){
        
        String[] pt1String = edge.arrival_time.split(":");
        double[] pt1 = {Double.parseDouble(pt1String[0]), Double.parseDouble(pt1String[1])};
        String[] pt2String = edge.departure_time.split(":");
        double[] pt2 = {Double.parseDouble(pt2String[0]), Double.parseDouble(pt2String[1])};

        return (long) (alg.getWalkingTime(pt1, pt2) + resultTimes.get(startPoint));
    }
}
