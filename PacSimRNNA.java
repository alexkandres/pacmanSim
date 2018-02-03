import pacsim.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PacSimRNNA implements PacAction {

    private List<Point> path;
    private List<Point> allNodes;
    private int simTime;

    //constructor should be formatted this way
    public PacSimRNNA(String fname) {
        PacSim sim = new PacSim( fname );
        sim.init(this);
    }

    // instantiate class instants
    public static void main( String[] args ) {
        System.out.println("\nTSP using simple RNNA agent by Alex Andres:");
        System.out.println("\nMaze : " + args[ 0 ] + "\n" );
        new PacSimRNNA( args[ 0 ] );
    }

    //reset variables here
    //gets called at every new run(ie. when game restarts)
    @Override
    public void init() {
        simTime = 0;
        path = new ArrayList();
        allNodes = new ArrayList();
    }

    //called every time pacman moves (one pixel?)
    //TODO Implement RNNA here
    @Override
    public PacFace action(Object state) {

        //TODO compute RNNA path

        //2d array of grid
        //find pacman on the grid
        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman( grid );


        // make sure Pac-Man is in this game
        if( pc == null ) return null;

        // if current path completed (or just starting out),
        // select a the nearest food using the city-block
        // measure and generate a path to that target

        //TODO RNNA solution
        if( path.isEmpty() ) {
//            Point tgt = PacUtils.nearestFood( pc.getLoc(), grid);
//            //path = [(x1,y1),(x2,y2)]
//            path = BFSPath.getPath(grid, pc.getLoc(), tgt);
//
//            System.out.println("Pac-Man currently at: [ " + pc.getLoc().x + ", " + pc.getLoc().y + " ]");
//            System.out.println("Setting new target  : [ " + tgt.x + ", " + tgt.y + " ]");
            path = getRNNASolution(grid);
        }

        // take the next step on the current path
        //TODO determine next move

//        Point next = path.remove( 0 );
//        PacFace face = PacUtils.direction( pc.getLoc(), next );
//        System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", ++simTime, pc.getLoc().x, pc.getLoc().y, face );
//        return face;
        return null;
    }

    public List<Point> getRNNASolution(PacCell[][] grid) {
        //TODO put start at each of these nodes
        //List<Point> allnodes
        allNodes = PacUtils.findFood(grid);
        //int[] arr..
        List<Point>[] possibleSolution = new List[allNodes.size()];

        //each node get NNA path
        for(int i = 0; i < allNodes.size(); i++){
            //add each path in arraylist [[],[],..]
            //TODO may need to clone grid for each getPath
            PacCell[][] gridClone = grid.clone();
            possibleSolution[i] = getPath(allNodes.get(i),grid);
        }

        //get lowest path (ie. number of point length [(),()] = 2)
        return bestPath(possibleSolution);
    }

    //once per Node/point
    private List<Point> getPath(Point point, PacCell[][] grid) {
        List<Point> path = new ArrayList<>();
        List<Point> subPath = new ArrayList<>();

        //while no more food left
        while (PacUtils.foodRemains(grid)){

            //get nearest food and set path
            //add subPath to overall path
            if(subPath.isEmpty()){
                Point tgt = PacUtils.nearestFood(point, grid);
                subPath = BFSPath.getPath(grid, point, tgt);

                System.out.println("Currently at ["+point.x+", "+point.y+"]");
                System.out.println("Target at ["+tgt.x+", "+tgt.y+"]");

                path.addAll(subPath);
            }

            //remove each point from the path
            while (!subPath.isEmpty()){
                Point pcPoint =  new Point(point.x,point.y);
                point = subPath.remove(0);
                PacFace face = PacUtils.direction( pcPoint, point );

                System.out.println("  Removing ["+point.x+", "+ point.y+"]");
            }

        }

        return path;
    }

    private List<Point> bestPath(List<Point>[] possibleSolution) {
        return null;
    }
}
