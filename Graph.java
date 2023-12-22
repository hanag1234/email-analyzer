
//import 
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

//graph class called from A3 
public class Graph 
{
    private final Map<String, Set<String>> adjacencyList;

//initialize the adjacency list 
    public Graph() 
    {
        adjacencyList = new HashMap<>();
    }

//adds neighbour to given vertex
    public void addNeighbor(String vertex, String neighbor)
     {
        //if there is no vertex then create new set
        adjacencyList.putIfAbsent(vertex, new HashSet<>());
        //add the neighbour to the set where the vertex is
        adjacencyList.get(vertex).add(neighbor);
    }
//adds an edge betweent the two vertices to make them neighbours
    public void addEdge(String vertex1, String vertex2) {
        //if the vertices are the same, skip adding the new edge
    if (vertex1.equals(vertex2)) {
        return; 
    }
    //make the vertexes neighbours 
    addNeighbor(vertex1, vertex2);
    addNeighbor(vertex2, vertex1);
    }
//retrieve all vertices 
    public Set<String> getVertices() 
    {
        return adjacencyList.keySet();
    }
//get the neighbours of the given vertex 
    public Set<String> getNeighbors(String vertex) 
    {
        //if the vertex doesn't exist, return an empty set
        return adjacencyList.getOrDefault(vertex, new HashSet<>());
    }
}

