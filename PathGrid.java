import pacsim.PacCell;

import java.awt.*;
import java.util.List;

public class PathGrid {
    public PacCell[][] grid;
    public List<Point> path;
    public Point currentPoint;

    public PathGrid(PacCell[][] grid, Point currentPoint) {
        this.grid = grid;
        this.path = path;
        this.currentPoint = currentPoint;
    }

    public PathGrid(PacCell[][] grid, List<Point> path, Point currentPoint) {
        this.grid = grid;
        this.path = path;
        this.currentPoint = currentPoint;
    }
}
