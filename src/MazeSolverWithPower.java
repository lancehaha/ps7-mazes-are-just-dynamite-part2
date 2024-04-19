import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class MazeSolverWithPower implements IMazeSolverWithPower {
    private static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3;
    private static int[][] DELTAS = new int[][] {
            { -1, 0 }, // North
            { 1, 0 }, // South
            { 0, 1 }, // East
            { 0, -1 } // West
    };

    private Maze maze;
    private boolean[][] visited;
    private int[][] minSteps;
    public MazeSolverWithPower() {
        maze = null;
    }

    @Override
    public void initialize(Maze maze) {
        this.maze = maze;
        visited = new boolean[maze.getRows()][maze.getColumns()];
        minSteps = new int[maze.getRows()][maze.getColumns()];
    }

    private boolean canGo(int row, int col, int dir) {
        // not needed since our maze has a surrounding block of wall
        // but Joe the Average Coder is a defensive coder!
        if (row + DELTAS[dir][0] < 0 || row + DELTAS[dir][0] >= maze.getRows()) return false;
        if (col + DELTAS[dir][1] < 0 || col + DELTAS[dir][1] >= maze.getColumns()) return false;
        switch (dir) {
            case NORTH:
                return !maze.getRoom(row, col).hasNorthWall();
            case SOUTH:
                return !maze.getRoom(row, col).hasSouthWall();
            case EAST:
                return !maze.getRoom(row, col).hasEastWall();
            case WEST:
                return !maze.getRoom(row, col).hasWestWall();
        }
        return false;
    }

    private boolean inRange(int row, int col, int dir) {
        // not needed since our maze has a surrounding block of wall
        // but Joe the Average Coder is a defensive coder!
        if (row + DELTAS[dir][0] < 0 || row + DELTAS[dir][0] >= maze.getRows()) return false;
        if (col + DELTAS[dir][1] < 0 || col + DELTAS[dir][1] >= maze.getColumns()) return false;
        return true;
    }

    @Override
    public Integer pathSearch(int startRow, int startCol, int endRow, int endCol) throws Exception {
        if (maze == null) {
            throw new Exception("Oh no! You cannot call me without initializing the maze!");
        }
        if (startRow < 0 || startCol < 0 || startRow >= maze.getRows() || startCol >= maze.getColumns() ||
                endRow < 0 || endCol < 0 || endRow >= maze.getRows() || endCol >= maze.getColumns()) {
            throw new IllegalArgumentException("Invalid start/end coordinate");
        }
        // set all visited flag to false
        // before we begin our search
        for (int i = 0; i < maze.getRows(); ++i) {
            for (int j = 0; j < maze.getColumns(); ++j) {
                this.visited[i][j] = false;
                minSteps[i][j] = -1;
                maze.getRoom(i, j).onPath = false;
            }
        }
        return solve(startRow, startCol, endRow, endCol);
    }
    public void setOnPath(ArrayList<int[]> currentP) {
        for (int[] a: currentP) {
            maze.getRoom(a[0], a[1]).onPath = true;
        }
    }

    public Integer solve(int startRow, int startCol, int endRow, int endCol) {
        Queue<ArrayList<int[]>> queue = new LinkedList<>();
        ArrayList<int[]> initalPath = new ArrayList<>();
        initalPath.add(new int[]{startRow, startCol});
        queue.offer(initalPath);
        minSteps[startRow][startCol] = 0;
        Integer finalDistance = null;
        boolean firstTimeFlag = true;
        while (!queue.isEmpty()) {
            ArrayList<int[]> currentP = queue.poll();
            int s = currentP.size();
            int[] currentR = currentP.get(s - 1);
            int row = currentR[0];
            int col = currentR[1];
            visited[row][col] = true;
            if (row == endRow && col == endCol && firstTimeFlag) {
                firstTimeFlag = false;
                setOnPath(currentP);
                finalDistance = s-1;
            }
            for (int d = 0; d < 4; d++) {
                if (canGo(row, col, d)) {
                    int newRow = row + DELTAS[d][0];
                    int newCol = col + DELTAS[d][1];
                    if (!visited[newRow][newCol]) {
                        ArrayList<int[]> newP = (ArrayList<int[]>) currentP.clone();
                        newP.add(new int[]{newRow, newCol});
                        minSteps[newRow][newCol] = newP.size()-1;
                        queue.offer(newP);
                    }
                }
            }
        }
        return finalDistance;
    }
    @Override
    public Integer numReachable(int k) throws Exception {
        if (maze == null) {
            throw new Exception("illegal maze");
        }
        int rn = 0;
        for (int i = 0; i < minSteps.length; i++) {
            for (int j = 0; j < minSteps[i].length; j++) {
                if (minSteps[i][j] == k) {
                    rn += 1;
                }
            }
        }
        return rn;
    }

    private void reconstructPath(Map<String, String> prev, String startState, String endState) {
        String currentState = endState;
        while (currentState != null && !currentState.equals(startState)) {
            String[] parts = currentState.split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            maze.getRoom(row, col).onPath = true;
            currentState = prev.get(currentState);
        }
        // Also set start room on path
        String[] startParts = startState.split(",");
        maze.getRoom(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1])).onPath = true;
    }
    @Override
    public Integer pathSearch(int startRow, int startCol, int endRow,
                              int endCol, int superpowers) throws Exception {
        if (maze == null) {
            throw new Exception("Oh no! You cannot call me without initializing the maze!");
        }
        if (startRow < 0 || startCol < 0 || startRow >= maze.getRows() || startCol >= maze.getColumns() ||
                endRow < 0 || endCol < 0 || endRow >= maze.getRows() || endCol >= maze.getColumns()) {
            throw new IllegalArgumentException("Invalid start/end coordinate");
        }
        // set all visited flag to false
        // before we begin our search
        for (int i = 0; i < maze.getRows(); ++i) {
            for (int j = 0; j < maze.getColumns(); ++j) {
                this.visited[i][j] = false;
                minSteps[i][j] = -1;
                maze.getRoom(i, j).onPath = false;
            }
        }
        return supersolve(startRow, startCol, endRow, endCol, superpowers);
    }

    private Integer supersolve(int startRow, int startCol, int endRow, int endCol, int superpowers) {
        Queue<int[]> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        Map<String, String> prev = new HashMap<>();
        boolean firstTime = true;
        Integer finalDistance = null;
        minSteps[startRow][startCol] = 0;
        int steps = 0;

        String startState = startRow + "," + startCol + "," + superpowers;
        queue.offer(new int[]{startRow, startCol, superpowers, steps});
        visited.add(startState);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];
            int powers = current[2];
            int step = current[3];
            if (row == endRow && col == endCol && firstTime) {
                firstTime = false;
                reconstructPath(prev, startState, endRow + "," + endCol + "," + powers);
                finalDistance = minSteps[row][col];
            }

            for (int d = 0; d < 4; d++) {
                if (inRange(row, col, d)) {
                    int newRow = row + DELTAS[d][0];
                    int newCol = col + DELTAS[d][1];
                    int newPowers = canGo(row, col, d) ? powers : powers - 1;
                    int newStep = step + 1;
                    String newState = newRow + "," + newCol + "," + newPowers;

                    if (newPowers >= 0 && !visited.contains(newState)) {
                        prev.put(newState, row + "," + col + "," + powers);
                        queue.offer(new int[]{newRow, newCol, newPowers, newStep});
                        visited.add(newState);
                        if (minSteps[newRow][newCol] == -1 || minSteps[newRow][newCol] > newStep) {
                            minSteps[newRow][newCol] = newStep;
                        }
                    }
                }
            }
        }

        return finalDistance;
    }
    public static void main(String[] args) {
        try {
            Maze maze = Maze.readMaze("maze-empty.txt");
            IMazeSolverWithPower solver = new MazeSolverWithPower();
            solver.initialize(maze);

            System.out.println(solver.pathSearch(0, 0, 2, 3, 100));
            MazePrinter.printMaze(maze);

            for (int i = 0; i <= 9; ++i) {
                System.out.println("Steps " + i + " Rooms: " + solver.numReachable(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
