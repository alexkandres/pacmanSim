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
    @Override
    public PacFace action(Object state) {

        //2d array of grid
        //find pacman on the grid
        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman( grid );


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

        PacCell[][] gridClone = gridClone(grid);
        List<PathGrid> pathGridList = getPossibleSolutions(gridClone);
        List<Point> bestPath = getBestPath(pathGridList);

        return bestPath;
    }

    private List<Point> getBestPath(List<PathGrid> pathGridList) {
        int shortest = 0;
        //find shortest int the list
        for(int i = 1; i < pathGridList.size(); i++){
            if(pathGridList.get(i).path.size() < pathGridList.get(i-1).path.size()){
                shortest = i;
            }
        }

        //TODO may need to shift left
        Point point = pathGridList.get(shortest).path.remove(0);
        pathGridList.get(shortest).path.add(point);

        //printing shortest path
        for(int i = 0; i < pathGridList.get(shortest).path.size(); i++){
            System.out.println("NODE: " + pathGridList.get(shortest).path.get(i));
        }

        return pathGridList.get(shortest).path;
    }

    private List<PathGrid> getPossibleSolutions(PacCell[][] gridClone) {
        List<PathGrid> pathGridList = new ArrayList<>();
        //TODO change new Point() to be dynamic, add path to constructor
        PathGrid grid = new PathGrid(gridClone, new Point(3,4));
        pathGridList.add(grid);

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
        pathGrid.path.addAll(subPath);

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
        PacCell[][] gridClone = gridClone(pathGrid.grid);
        PathGrid pathGridClone = new PathGrid(gridClone, pathGrid.currentPoint, pathGrid.path);
        return pathGridClone;
    }

    private List<Point> getNearestFoods(PathGrid pathGrid) {
        //this list will be returned
        List<Point> allNearestFood = new ArrayList<>();

        //find closest food and distance
        Point foodPoint = PacUtils.nearestFood(pathGrid.currentPoint, pathGrid.grid);
        int shortestDistance = PacUtils.manhattanDistance(pathGrid.currentPoint, foodPoint);

        //find all foods
        List<Point> foodListPoint = PacUtils.findFood(pathGrid.grid);

        //compare each food distance to shortest distance
        for(int i = 0; i < foodListPoint.size(); i++){
            if (shortestDistance == PacUtils.manhattanDistance(pathGrid.currentPoint, foodListPoint.get(i))){
                allNearestFood.add(foodListPoint.get(i));
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
            System.out.println("Starting node " + i);
            PacCell[][] gridClone = gridClone(grid);
            possibleSolution[i] = getPath(allNodes.get(i),gridClone);
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
                //TODO maybe more than one closest food
                //get list of closest foods
                //for each, try a path on them
                Point tgt = PacUtils.nearestFood(point, grid);
                subPath = BFSPath.getPath(grid, point, tgt);
                //grid[tgt.x][tgt.y] = not food cell
                if(grid[tgt.x][tgt.y] instanceof FoodCell) {
                    grid[tgt.x][tgt.y] = new PacCell(tgt.x,tgt.y);
                }
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
                if(grid[point.x][point.y] instanceof FoodCell) {
                    grid[point.x][point.y] = new PacCell(point.x,point.y);
                }
            }

        }

        return path;
    }

    private List<Point> bestPath(List<Point>[] possibleSolution) {

        //= {
        //      [(x1y1),..]
        //      [(x1y2),..] }
        int shortest = 0;
        for(int i = 1; i < possibleSolution.length; i++){
            if(possibleSolution[i].size() < possibleSolution[i-1].size()){
                shortest = i;
            }
        }

        for(int i = 0; i < possibleSolution[shortest].size(); i++){
            System.out.println("NODE "+ i + "," + possibleSolution[shortest].get(i) + "x,y = "+ possibleSolution[shortest].get(i).x +","+possibleSolution[shortest].get(i).y);
        }
        System.out.println("SHORTEST Found = " + possibleSolution.toString());

        // switch from starting node(ie. [4,3])
        //if first node is not starting node (ie. 4,3) then shift to the left
            //remove 1 node then place at the end
        while(possibleSolution[shortest].get(0).x != 4 || possibleSolution[shortest].get(0).y != 3){

            //shift
            System.out.println("Shift " + possibleSolution[shortest].get(0));
            Point point = possibleSolution[shortest].remove(0);
            possibleSolution[shortest].add(point);
        }

        Point point = possibleSolution[shortest].remove(0);
        possibleSolution[shortest].add(point);
        for(int i = 0; i < possibleSolution[shortest].size(); i++){
            System.out.println("NODE "+ i + "," + possibleSolution[shortest].get(i)+ "x,y = "+ possibleSolution[shortest].get(i).x +","+possibleSolution[shortest].get(i).y);
        }
        return possibleSolution[shortest];
    }

    private PacCell[][] gridClone(PacCell[][] grid) {
        System.out.println("Rowwww "+ grid.length+", collll"+grid[0].length);
        PacCell[][] gridClone = new PacCell[grid.length][grid[0].length];

        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid[0].length; j++){
                gridClone[i][j] = grid[i][j].clone();
            }
        }
        return gridClone;
    }
}
