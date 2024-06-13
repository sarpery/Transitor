package com.example.Backend.Graph;

import java.util.HashMap;
import java.util.List;

public class BusGraph<T> implements Graph<T>{

     public HashMap<T, List<EdgeNode<T>>> verteces;
     
      public BusGraph() {
        verteces = new HashMap<T, List<EdgeNode<T>>>();

    }

     

}
