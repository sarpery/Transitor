package com.example.Backend.Graph;

public class EdgeNode<T> {
    public String name;
    public Integer weight;
    public String departure_time;
    public String arrival_time;
    public T destination_point;
    public int distance;

    public EdgeNode(String element, Integer weight) {
        this.name = element;
        this.weight = weight;
    }

    public EdgeNode(T destination_point, String trip_id, String arr, String dep, int distance) {
        this.name = trip_id;
        this.destination_point = destination_point;
        this.arrival_time = arr;
        this.departure_time = dep;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String element) {
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
