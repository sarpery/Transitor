package com.example.Backend.Graph;

import com.example.Backend.Algorithms.Algorithms;

public class EdgeNode<T> {
    public T name;
    public Integer weight;
    public String departure_time;
    public String arrival_time;
    public T destination_point;
    public int distance;

    public EdgeNode(T element, Integer weight) {
        this.name = element;
        this.weight = weight;
    }

    public EdgeNode(T destination_point, T trip_id, String arr, String dep, int distance) {
        this.name = trip_id;
        this.destination_point = destination_point;
        this.arrival_time = arr;
        this.departure_time = dep;
        this.distance = distance;
        this.weight = calc_weight();
    }

    private int calc_weight(){
        Algorithms<String> alg = new Algorithms<>();
        return Math.toIntExact(alg.parseDuration(arrival_time).minus(alg.parseDuration(departure_time)).toMinutes());
    }

    public T getName() {
        return name;
    }

    public void setName(T element) {
        this.name = element;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        return name.equals(((EdgeNode<T>) o).getName());
    }
}
