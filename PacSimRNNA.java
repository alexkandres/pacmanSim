import pacsim.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PacSimRNNA implements PacAction {

    private List<Point> path;
    private List<Point> allNodes;
    private int simTime;
    private PacmanCell pc;
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
    @Override
    public PacFace action(Object state) {

        //2d array of grid
        //find pacman on the grid
        PacCell[][] grid = (PacCell[][]) state;
        pc = PacUtils.findPacman( grid );


        // make sure Pac-Man is in this game
        if( pc == null ) return null;

        // if current path completed (or just starting out),
        // select a the nearest food using the city-block
        // measure and generate a path to that target
        if( path.isEmpty() ) {
            path = getRNNASolutionBranch(grid);
        }

        // take the next step on the current path
        Point next = path.remove( 0 );
        PacFace face = PacUtils.direction( pc.getLoc(), next );
        System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", ++simTime, pc.getLoc().x, pc.getLoc().y, face );
        return face;
    }

    private List<Point> getRNNASolutionBranch(PacCell[][] grid) {


        allNodes = PacUtils.findFood(grid);

        PacCell[][] gridClone = PacUtils.cloneGrid(grid);
        List<PathGrid> pathGridList = getPossibleSolutions(gridClone);
        List<Point> bestPath = getBestPath(pathGridList);

        return bestPath;
    }

    private List<Point> getBestPath(List<PathGrid> pathGridList) {
        int shortest = 0;
        //find shortest int the list
        System.out.println("PATHSIZE: "+pathGridList.get(0).path.size());
        for(int i = 1; i < pathGridList.size(); i++){
            System.out.println("PATHSIZE: "+pathGridList.get(i).path.size());
            if(pathGridList.get(i).path.size() < pathGridList.get(shortest).path.size()){
                shortest = i;
                System.out.println("SHORTEST = " + "PATHSIZE " + pathGridList.get(i).path.size());
            }
        }

        //TODO may need to shift left
//        Point point = pathGridList.get(shortest).path.remove(0);
//        pathGridList.get(shortest).path.add(point);

        //printing shortest path
        for(int i = 0; i < pathGridList.get(shortest).path.size(); i++){
            System.out.println(i + "NODE " + pathGridList.get(shortest).path.get(i));
        }

        return pathGridList.get(shortest).path;
    }

    private List<PathGrid> getPossibleSolutions(PacCell[][] gridClone) {
        List<PathGrid> pathGridList = new ArrayList<>();
        List<Point> initPath = new ArrayList<>();
        //TODO change new Point() to be dynamic, add path to constructor

        PathGrid grid = new PathGrid(gridClone, new Point(pc.getX(),pc.getY()), initPath);
        List<Point> allFood = PacUtils.findFood(gridClone);
        for(int i = 0; i < allFood.size();i++){
            PathGrid pathGridInit = pathGridClone(grid);
            expand(pathGridInit, allFood.get(i));
            pathGridList.add(pathGridInit);
        }

        //update and expand possible solutions
        while (gridsHaveFood(pathGridList)){
            for(int i = 0; i < pathGridList.size(); i++){
                if(!PacUtils.foodRemains(pathGridList.get(i).grid)){
                    continue;
                }
                List<Point> allNearestFoods = getNearestFoods(pathGridList.get(i));
                PathGrid pathGridClone = pathGridClone(pathGridList.get(i));

                //expand first nearest food
                expand(pathGridList.get(i), allNearestFoods.remove(0));

                //create, expand and add to pathGridList
                for(int j = 0; j < allNearestFoods.size(); j++){
                    System.out.println( j+"Create new branch" + pathGridClone.currentPoint);
                    PathGrid newPathGrid = pathGridClone(pathGridClone);
                    expand(newPathGrid, allNearestFoods.get(j));
                    pathGridList.add(newPathGrid);
                }

            }
        }
        return pathGridList;
    }

    private void expand(PathGrid pathGrid, Point targetPoint) {
        List<Point> subPath;
        subPath = BFSPath.getPath(pathGrid.grid, pathGrid.currentPoint, targetPoint);
        System.out.println("BEFORE "+pathGrid.path);
        if(pathGrid == null)
            System.out.println("pathGrid == null");
        if(pathGrid.path == null)
            System.out.println("path == null");
        pathGrid.path.addAll(subPath);
        System.out.println("AFTER "+pathGrid.path);

        //mark food as eaten
        while (!subPath.isEmpty()){
            Point pcPoint = new Point(pathGrid.currentPoint.x, pathGrid.currentPoint.y);
            pathGrid.currentPoint = subPath.remove(0);
            PacFace face = PacUtils.direction(pcPoint, pathGrid.currentPoint);
            if(pathGrid.grid[pathGrid.currentPoint.x][pathGrid.currentPoint.y] instanceof FoodCell){
                pathGrid.grid[pathGrid.currentPoint.x][pathGrid.currentPoint.y] = new PacCell(pathGrid.currentPoint.x, pathGrid.currentPoint.y);
            }
        }
    }

    private PathGrid pathGridClone(PathGrid pathGrid) {
        PacCell[][] gridClone = PacUtils.cloneGrid(pathGrid.grid);
        List<Point> pathClone = pathClone(pathGrid.path);
        Point pointClone = new Point(pathGrid.currentPoint.x, pathGrid.currentPoint.y);

        //TODO pathGrid.path??? maybe a pointer
        PathGrid pathGridClone = new PathGrid(gridClone, pointClone, pathClone);
        return pathGridClone;
    }

    private List<Point> pathClone(List<Point> path) {
        List<Point> pathCloneList = new ArrayList<>();

        //for ea item place into new list
        for(int i = 0; i< path.size(); i++){
            int x = path.get(i).x;
            int y = path.get(i).y;

            pathCloneList.add(new Point(x,y));
        }
        return pathCloneList;
    }

    private List<Point> getNearestFoods(PathGrid pathGrid) {

        //find all foods
        List<Point> foodListPoint = PacUtils.findFood(pathGrid.grid);

        int shortestDistance = 0;
        //compare each food distance to shortest distance
        for(int i = 1; i < foodListPoint.size(); i++){
            List<Point> p1 = BFSPath.getPath(pathGrid.grid, pathGrid.currentPoint, foodListPoint.get(i));
            List<Point> p2 = BFSPath.getPath(pathGrid.grid, pathGrid.currentPoint, foodListPoint.get(shortestDistance));

            if (p1.size() < p2.size()){
                shortestDistance = i;
//                System.out.println("NearestFOODS" +foodListPoint.get(i));
            }
        }

        List<Point> shortestPoint = BFSPath.getPath(pathGrid.grid, pathGrid.currentPoint, foodListPoint.get(shortestDistance));

        //this list will be returned
        List<Point> allNearestFood = new ArrayList<>();
        for(int i = 0; i < foodListPoint.size(); i++){
            List<Point> p1 = BFSPath.getPath(pathGrid.grid, pathGrid.currentPoint, foodListPoint.get(i));
            if (p1.size() == shortestPoint.size()){
                allNearestFood.add(foodListPoint.get(i));
                System.out.println("NEAREST FOODS" +foodListPoint.get(i));
            }
        }
        return allNearestFood;
    }

    private boolean gridsHaveFood(List<PathGrid> pathGridList) {

        //for each pathgrid.grid check if
        for(int i = 0; i < pathGridList.size(); i++){
            if(PacUtils.foodRemains(pathGridList.get(i).grid)){
                return true;
            }
        }
        return false;
    }


}
