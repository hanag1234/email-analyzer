//imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Set;
import java.util.Iterator;

public class A3 {
    //regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)((From|To|Cc|Bcc):\\s*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,}\\b|\\b[A-Za-z0-9._%+-]+@enron\\.com\\b)");

    //retrieves files and throws exception if I/O error occurs
    private static List < String > getFiles(String dataPath) throws IOException {
        try (Stream < Path > pathStream = Files.walk(Paths.get(dataPath))) {
            return pathStream
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());
        }
    }

    private static Graph makeGraph(List < String > data) throws IOException {
        //initialize graph
        Graph graph = new Graph();
        //initialize the unique senders and emails 
        Set < String > theUniqueSenders = new HashSet < > ();
        Set < String > theUniqueEmails = new HashSet < > ();
        //iterate over each file in the folder
        for (int i = 0; i < data.size(); i++) {
            String file = data.get(i);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                List < String > theReceivers = new ArrayList < > ();

                String cameFrom = null;
                String row;
                //read each row/line in the file
                for (row = reader.readLine(); row != null; row = reader.readLine()) {
                    if (row.startsWith("From: ")) {
                        //separate the email using regex
                        Matcher matcher = Pattern.compile("[a-zA-Z0-9._%+-]+@enron\\.com").matcher(row);
                        if (matcher.find()) {
                            cameFrom = matcher.group().trim();
                            if (!theUniqueSenders.contains(cameFrom)) {
                                theUniqueSenders.add(cameFrom);
                            }
                        } else {
                            cameFrom = null;
                        }
                    } else if (row.startsWith("To: ")) {
                        //separate the emails into a list 
                        theReceivers = Separate(row);
                        for (String receiver: theReceivers) {
                            if (cameFrom != null) {
                                //add an edge between the sender and receiver
                                graph.addEdge(cameFrom, receiver);
                            }
                            if (!theUniqueEmails.contains(receiver)) {
                                theUniqueEmails.add(receiver);
                            }
                        }
                    }
                }
            }
        }
        return graph;
    }


    private static void dfs(Graph graph, String vertex, String parent, Map < String, Integer > dfsNum, Map < String, Integer > prev, int assignedDFSNumbers, Set < String > connectors) {
        //return if the vertex has already been visited
        if (dfsNum.containsKey(vertex)) {
            return;
        }
        //mark the vertex visited 
        dfsNum.put(vertex, assignedDFSNumbers);
        prev.put(vertex, assignedDFSNumbers);
        int successor = 0; //count the successors of the vertex
        //get the neighbours of the vertex
        Set < String > neighbors = graph.getNeighbors(vertex);
        Iterator < String > iterator = neighbors.iterator();
        while (iterator.hasNext()) {
            String neighbor = iterator.next();

            if (!dfsNum.containsKey(neighbor)) {
                //if neighbour has not been visited, increase successor
                successor++;
                dfs(graph, neighbor, vertex, dfsNum, prev, assignedDFSNumbers + 1, connectors);
                //check for conditions to identify a connector
                if (dfsNum.get(vertex) <= prev.get(neighbor) && (parent != null || successor > 1)) {
                    connectors.add(vertex);
                }
                //Update the low-link value of the current vertex
                prev.put(vertex, Math.min(prev.get(vertex), prev.getOrDefault(neighbor, 0)));
            } else if (!neighbor.equals(parent)) {
                //Update the low-link value of the current vertex when encountering a back edge
                prev.put(vertex, Math.min(prev.get(vertex), dfsNum.get(neighbor)));
            }
        }

    }
    private static List < String > Separate(String row) {
        List < String > emails = new ArrayList < > ();
        //split the row/line into dif parts using space, commas, semicolons, separators
        String[] lineParts = row.split("[\\s,;]+");
        for (String part: lineParts) {
            Matcher matcher = EMAIL_PATTERN.matcher(part);
            if (matcher.matches()) {
                //if the part matches the email pattern, add it to the email list
                emails.add(part);
            }
        }
        return emails;
    }
    private static void printConnectors(Set < String > connectors, String outputPath) throws IOException {
        //change the group of connectors to a sorted list 
        List < String > sortedConnectors = new ArrayList < > (connectors);
        Collections.sort(sortedConnectors);
        //if output path is there, write the sorted connectors to a file
        if (outputPath != null)
            Files.write(Paths.get(outputPath), sortedConnectors, StandardCharsets.UTF_8);
    }
    private static Set < String > Connections(Graph graph) {
        //set to store connectors
        Set < String > connectors = new HashSet < > ();
        //hashmap to store dfs numbers for the vertexes
        Map < String, Integer > dfsNum = new HashMap < > ();
        //hashmap to store the low-link value for the vertexes
        Map < String, Integer > prev = new HashMap < > ();
        int assignedDFSNumbers = 1; //this counts the number of assigned DFS numbers
        //dfs for each vertex that hasn't been visited
        graph.getVertices().stream()
            //filter out the visited vertices
            .filter(vertex -> !dfsNum.containsKey(vertex)) 
            .forEach(vertex -> dfs(graph, vertex, null, dfsNum, prev, assignedDFSNumbers, connectors));

        return connectors;
    }
    public static void askForData(Graph graph) {
        Scanner sc = new Scanner(System.in);
        String userInput;
        //loop until type EXIT
        for (;;) {
            System.out.println("Email address of the individual (or EXIT to quit):");
            userInput = sc.nextLine().trim();

            if (userInput.equalsIgnoreCase("EXIT")) {
                break;
            }

            if (!graph.getVertices().contains(userInput)) {
                System.out.println("Email address (" + userInput + ") not found in the dataset.");
                continue;
            }
            //find the people who receieved emails from the sender
            Set < String > sentTo = graph.getNeighbors(userInput);
            //set to store the people who sent emails to the input email
            Set < String > where = new HashSet < > ();
            //iterate over all vertices 
            for (String vertex: graph.getVertices()) {
                if (graph.getNeighbors(vertex).contains(userInput)) {
                    where.add(vertex);
                }
            }

            Set < String > teamMembers = getTeamMembers(graph, userInput);
            //remove user from team members
            teamMembers.remove(userInput);
            //print
            System.out.println("* " + userInput + " has sent messages to " + sentTo.size() + " others");
            System.out.println("* " + userInput + " has received messages from " + where.size() + " others");
            System.out.println("* " + userInput + " is in a team with " + teamMembers.size() + " individuals");
        }
    }

    public static Set < String > getTeamMembers(Graph graph, String emailAddress) {
        //store the visited people
        Set < String > visited = new HashSet < > ();
        //stack for dfs traversal
        Stack < String > stack = new Stack < > ();
        //begin dfs from the provided email
        stack.add(emailAddress);

        do {
            //pop vertex from stack
            String current = stack.pop();
            //mark as visited
            visited.add(current);

            for (String neighbor: graph.getNeighbors(current)) {
                //iterate over neighbours
                if (!visited.contains(neighbor)) {
                    //push neighbours that weren't visited onto stack
                    stack.push(neighbor);
                }
            }
        //loop until stack is empty 
        } while (!stack.isEmpty());
        return visited;
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("java A3 <path to data set> <path to output file>");
            System.exit(1);
        }

        String dataPath = args[0];
        String outputPath = args[1];
        System.out.println("data is loading...");

        List < String > data = getFiles(dataPath);
        System.out.println("Total Number of Emails: " + data.size());

        Graph graph = makeGraph(data);
        Set < String > connectors = Connections(graph);
        printConnectors(connectors, outputPath);

        Scanner sc = new Scanner(System.in);
        String userInput;
        while (true) {
            System.out.println("Email address of the individual (or EXIT to quit):");
            userInput = sc.nextLine().trim();
            System.out.println("User input: " + userInput);

            if (userInput.equalsIgnoreCase("EXIT")) {
                break;
            }

            if (graph.getVertices().contains(userInput)) {
                System.out.println("Email was found in the dataset");
                Set < String > sentTo = new HashSet < > (graph.getNeighbors(userInput));
                Set < String > where = new HashSet < > ();

                for (String vertex: graph.getVertices()) {
                    if (graph.getNeighbors(vertex).contains(userInput)) {
                        where.add(vertex);
                    }
                }

                Set < String > teamMembers = getTeamMembers(graph, userInput);
                teamMembers.remove(userInput);
                System.out.println("* " + userInput + " has received messages from " + where.size() + " others");
                System.out.println("* " + userInput + " has sent messages to " + sentTo.size() + " others");
                System.out.println("* " + userInput + " is in a team with " + teamMembers.size() + " individuals");

                System.out.println("Connectors:");
                Set < String > teamConnectors = new HashSet < > (connectors);
                teamConnectors.retainAll(teamMembers);

                if (teamConnectors.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (String connector: teamConnectors) {
                        System.out.print(connector + " —— ");
                    }
                    System.out.println(userInput);
                }
            } else {
                System.out.println("Email address (" + userInput + ") not found in the dataset.");
            }
        }
    }
}