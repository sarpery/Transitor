package com.example.Backend.Graph;

import java.util.List;

public interface Graph<T> {
 



    public List<EdgeNode<T>> neighbors(T x);

    /**
     * Adds the vertex x to the graph
     * 
     * @param x
     */
    public void addVertex(T x);

    /**
     * Removes the vertex x from the graph
     * 
     * @param x
     */
    public void removeVertex(T x);

    /**
     * Adds an edge from the vertices x to y
     * 
     * @param x
     * @param y
     */
    public void addEdge(T x, String y, Integer dis);

   
   
        

    public Integer getEdgeWeightBetweenVertices(T vertex1, T vertex2);
    

    public List<T> getVertecesList();
}
