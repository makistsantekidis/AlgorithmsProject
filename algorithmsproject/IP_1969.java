package algorithmsproject;

import integrationproject.algorithms.Algorithms;
import integrationproject.model.Ant;
import integrationproject.model.BlackAnt;
import integrationproject.model.RedAnt;
import integrationproject.utils.InputHandler;
import integrationproject.utils.Visualize;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.vecmath.Tuple2d;

/**
 *
 * @author Avraam Tsantekidis
 * @aem 1969
 * @email avraamt@csd.auth.gr
 */
public class IP_1969 extends Algorithms {

    public static void main(String[] args) {
        checkParameters(args);

        //create Lists of Red and Black Ants
        int flag = Integer.parseInt(args[1]);
        ArrayList<RedAnt> redAnts = new ArrayList<>();
        ArrayList<BlackAnt> blackAnts = new ArrayList<>();
        if (flag == 0) {
            InputHandler.createRandomInput(args[0], Integer.parseInt(args[2]));
        }
        InputHandler.readInput(args[0], redAnts, blackAnts);

        IP_1969 algs = new IP_1969();

        //debugging options
        boolean visualizeMST = true;
        boolean visualizeSM = true;
        boolean printCC = true;
        boolean evaluateResults = true;
        if (visualizeMST) {
            int[][] mst = algs.findMST(redAnts, blackAnts);
            if (mst != null) {
                Visualize sd = new Visualize(redAnts, blackAnts, mst, null, "Minimum Spanning Tree");
                sd.drawInitialPoints();
            }
        }

        if (visualizeSM) {
            int[][] matchings = algs.findStableMarriage(redAnts, blackAnts);
            if (matchings != null) {
                Visualize sd = new Visualize(redAnts, blackAnts, null, matchings, "Stable Marriage");
                sd.drawInitialPoints();
            }
        }

        if (printCC) {
            int[] coinChange = algs.coinChange(redAnts.get(0), blackAnts.get(0));
            System.out.println("Capacity: " + redAnts.get(0).getCapacity());
            for (int i = 0; i < blackAnts.get(0).getObjects().length; i++) {
                System.out.println(blackAnts.get(0).getObjects()[i] + ": " + coinChange[i]);
            }
        }

        if (evaluateResults) {
            System.out.println("\nEvaluation Results");
            algs.evaluateAll(redAnts, blackAnts);
        }
    }

    @Override
    public int[][] findMST(ArrayList<RedAnt> redAnts, ArrayList<BlackAnt> blackAnts) {

        ArrayList<Ant> ants = new ArrayList<>();
        ArrayList<Vertex> vertices = new ArrayList<>();
        ants.addAll(redAnts);
        ants.addAll(blackAnts);

        /*
         create all existing vertices between eges(ants). It is possible to 
         use Delaunay triangulation to find only the vertices that will be 
         useful, but it is out of the scope of this project.
         */
        for (int i = 0; i < ants.size(); i++) {
            for (int j = 0; j < i; j++) { // avoid counting vertices two times
                vertices.add(new Vertex(i, j, ants.get(i).getDistanceFrom(ants.get(j))));
            }
        }

        /*
         Sort the vertices in ascending order based on their length
         */
        Collections.sort(vertices);

        UnionFind a = new UnionFind(vertices.size());
        int redsize = redAnts.size();
        int start, end, start_belongs, end_belongs;
        int i = 0;
        int j = 0;
        ArrayList<int[]> connected_vertices = new ArrayList<>();
        while (a.getN() > 1 && i < vertices.size()) {
            start = vertices.get(i).getStart();
            end = vertices.get(i).getEnd();
            start_belongs = 0;
            end_belongs = 0;

            if (a.find(start) != a.find(end)) {
                a.union(start, end);
                if (start >= redsize) {
                    start -= redsize;
                    start_belongs = 1;
                }
                if (end >= redsize) {
                    end -= redsize;
                    end_belongs = 1;
                }
                connected_vertices.add(new int[]{start, start_belongs, end, end_belongs});
            }
            i++;
        }

        int[][] result = new int[connected_vertices.size()][4];
        for (int q = 0; q < connected_vertices.size(); q++) {
            result[q] = connected_vertices.get(q);
        }

        //You should implement this method.
        return result;
    }

    @Override
    public int[][] findStableMarriage(ArrayList<RedAnt> redAnts, ArrayList<BlackAnt> blackAnts) {
        //You should implement this method.
        ArrayList<ArrayList<Vertex>> redOptions = new ArrayList<>();

        /* 
         We don't have to compute the inverse preferences for the black ants 
         because their preference is exactly the same as the red ants, as it
         only deprents on the distance between them which is equal either way.
         */
        for (int i = 0; i < redAnts.size(); i++) {
            ArrayList<Vertex> option = new ArrayList<>();
            for (int j = 0; j < blackAnts.size(); j++) {
                option.add(new Vertex(i, j, redAnts.get(i).getDistanceFrom(blackAnts.get(j))));
            }
            Collections.sort(option);
            redOptions.add(option);
        }
//        ArrayList<ArrayList<Vertex>> blackOptions = (ArrayList<ArrayList<Vertex>>) redOptions.clone();        
        /*
         Create a comparator to sort an arraylist of arraylists of vertices
         to make sure that first couple of the first arraylist is always 
         going to be a certain couple and no better matches will exist.
         */
        Comparator comp = new Comparator<ArrayList<Vertex>>() {
            public int compareTo(ArrayList<Vertex> a, ArrayList<Vertex> b) {
                return compare(a, b);
            }

            @Override
            public int compare(ArrayList<Vertex> a, ArrayList<Vertex> b) {
                return a.get(0).compareTo(b.get(0));
            }
        };

        ArrayList<Integer> red_married = new ArrayList<>();
        ArrayList<Integer> black_married = new ArrayList<>();
        int[][] result = new int[redAnts.size()][2];
        int numMarriages = 0;
        // Dont stop until we have as many 
        while (numMarriages < blackAnts.size()) {
            // Add the first couple of the first arraylist of vertices as married
            Collections.sort(redOptions, comp);
            Vertex couple = redOptions.get(0).get(0);
            result[numMarriages] = new int[]{couple.getStart(), couple.getEnd()};
            red_married.add(couple.getStart());
            black_married.add(couple.getEnd());
            numMarriages++;

            // remove the red ant from the proposers
            redOptions.remove(0);

            // remove any combinations that contained the black ant that was 
            // just married
            for (int i = 0; i < redOptions.size(); i++) {
                ArrayList<Vertex> rest = redOptions.get(i);
                for (int j = 0; j < rest.size(); j++) {
                    Vertex rest_vertices = rest.get(j);
                    if (couple.getEnd() == rest_vertices.getEnd()) {
                        redOptions.get(i).set(j, null);
                    }
                }
                redOptions.get(i).removeAll(Collections.singleton(null));
            }
        }
        return result;
    }

    @Override
    public int[] coinChange(RedAnt redAnt, BlackAnt blackAnt) {
        /*  
         Methodology found at 
         https://www.topcoder.com/community/data-science/data-science-tutorials/dynamic-programming-from-novice-to-advanced/
         array min keeps the least amount of seeds required to sum up to 
         the weight represented by each index of min 
         (e.g. to get to weight 5 you need min[5] seeds)
         combinations keeps each seed required to sum up to the weight
         represented by each index (the same exact thing as above)
         */

        ArrayList<ArrayList<Integer>> combinations = new ArrayList<>();
        int[] min = new int[redAnt.getCapacity() + 1];
        int[] seeds = blackAnt.getObjects();
        Arrays.sort(seeds);

        combinations.add(new ArrayList<Integer>());
        for (int i = 1; i < redAnt.getCapacity() + 1; i++) {
            min[i] = Integer.MAX_VALUE;
            combinations.add(new ArrayList<Integer>());
        }
        min[0] = 0;

        for (int i = 1; i < redAnt.getCapacity() + 1; i++) {
            for (int j = 0; j < seeds.length; j++) {
                if (seeds[j] <= i && min[i - seeds[j]] < min[i]) {
                    min[i] = min[i - seeds[j]] + 1;
                    combinations.set(i, (ArrayList<Integer>) combinations.get(i - seeds[j]).clone());
                    combinations.get(i).add(seeds[j]);
                }
            }
        }

        seeds = blackAnt.getObjects();

        int[] result = new int[seeds.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Collections.frequency(combinations.get(redAnt.getCapacity()), seeds[i]);
        }
        //You should implement this method.
        return result;
    }

    private static void checkParameters(String[] args) {
        if (args.length == 0 || args.length < 2 || (args[1].equals("0") && args.length < 3)) {
            if (args.length > 0 && args[1].equals("0") && args.length < 3) {
                System.out.println("3rd argument is mandatory. Represents the population of the Ants");
            }
            System.out.println("Usage:");
            System.out.println("1st argument: name of filename");
            System.out.println("2nd argument: 0 create random file, 1 input file is given as input");
            System.out.println("3rd argument: number of ants to create (optional if 1 is given in the 2nd argument)");
            System.exit(-1);
        }
    }

}

/* 
 Code for this class comes from http://algs4.cs.princeton.edu/15uf/UF.java.html
 The main mechanism of this class is keeping a list of connected 
 edges in a graph, having in essence the different unconnected parts of the 
 graph as different ids. Using the Union function it connects different edges
 in the graph and if the edges where connected to groups of unnconnected "subgraphs"
 it connects them as well under a common id (although it does that lazily by 
 updating the id array every time you call the find function).
 */
class UnionFind {

    private int N;      // number of components
    private int[] id;     // id[i] = parent of i
    private byte[] rank;  // rank[i] = rank of subtree rooted at i (cannot be more than 31)

    /**
     * Initializes an empty union-find data structure with <tt>N</tt>
     * isolated components <tt>0</tt> through <tt>N-1</tt>
     *
     * @throws java.lang.IllegalArgumentException if <tt>N &lt; 0</tt>
     * @param N the number of sites
     */
    public UnionFind(int N) {
        if (N < 0) {
            throw new IllegalArgumentException();
        }
        this.N = N;
        id = new int[N];
        rank = new byte[N];
        for (int i = 0; i < N; i++) {
            id[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Returns the component identifier for the component containing site
     * <tt>p</tt>.
     *
     * @param p the integer representing one object
     * @return the component identifier for the component containing site
     * <tt>p</tt>
     * @throws java.lang.IndexOutOfBoundsException unless <tt>0 &le; p &lt;
     * N</tt>
     */
    public int find(int p) {
        if (p < 0 || p >= getId().length) {
            throw new IndexOutOfBoundsException();
        }
        while (p != getId()[p]) {
            getId()[p] = getId()[getId()[p]];    // path compression by halving                             
            p = getId()[p];
        }
        return p;
    }

    /**
     * Merges the component containing site <tt>p</tt> with the the component
     * containing site <tt>q</tt>.
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @throws java.lang.IndexOutOfBoundsException unless both <tt>0 &le; p &lt;
     * N</tt> and <tt>0 &le; q &lt; N</tt>
     */
    public void union(int p, int q) {
        int i = find(p);
        int j = find(q);
        if (i == j) {
            return;
        }
        // make root of smaller rank point to root of larger rank
        if (getRank()[i] < getRank()[j]) {
            getId()[i] = j;
        } else if (getRank()[i] > getRank()[j]) {
            getId()[j] = i;
        } else {
            getId()[j] = i;
            getRank()[i]++;
        }
        this.setN(this.getN() - 1);
    }

    /**
     * @return the N
     */
    public int getN() {
        return N;
    }

    /**
     * @param N the N to set
     */
    public void setN(int N) {
        this.N = N;
    }

    /**
     * @return the id
     */
    public int[] getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int[] id) {
        this.id = id;
    }

    /**
     * @return the rank
     */
    public byte[] getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(byte[] rank) {
        this.rank = rank;
    }
}

/* Implements a vertex with comparample interface to sort an arraylist of these
 objects based on their length.
 */
class Vertex implements Comparable<Vertex> {

    private double length;
    private int firstMember;
    private int secondMember;

    public Vertex(int start, int end, double length) {
        this.length = length;
        this.firstMember = start;
        this.secondMember = end;
    }

    @Override
    public int compareTo(Vertex other) {
        return Double.compare(this.getLength(), other.getLength());
    }

    /**
     * @return the length
     */
    public double getLength() {
        return this.length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return firstMember;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.firstMember = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return secondMember;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.secondMember = end;
    }
}
