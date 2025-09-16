import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;



import javax.swing.JPanel;

public class DemoPanel extends JPanel{

    //Screen settings
    final int maxCol = 15;
    final int maxRow = 10;
    final int nodeSize = 70;
    final int screenWidth = nodeSize * maxCol;
    final int screenHeight = nodeSize * maxRow;

    //Node 
    Node[][] node = new Node[maxCol][maxRow];
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>();
    ArrayList<Node> checkedList = new ArrayList<>();

    boolean goalReached = false;
    int steps = 0;

    // For parallelization
    private final ForkJoinPool customThreadPool = new ForkJoinPool(16); // new ForkJoinPool(Runtime.getRuntime().availableProcessors())
    private final ReentrantLock searchLock = new ReentrantLock();

    // For random mazes
    private Random random = new Random();


    public DemoPanel() {
        this.setPreferredSize(new Dimension(screenWidth,screenHeight));
        this.setBackground(Color.black);
        this.setLayout(new GridLayout(maxRow,maxCol));
        this.addKeyListener(new KeyHandler(this));
        this.setFocusable(true);

        initializeNodes();

        //Set start and goal nodes
        setStartNode(5,2);
        setGoalNode(10,5);

        //Set solid nodes
        setSolidNode(7,1);
        setSolidNode(7,2);
        setSolidNode(7,3);
        setSolidNode(5,5);
        setSolidNode(6,5);
        setSolidNode(7,5);
        setSolidNode(8,5);
        setSolidNode(9,5);
        setSolidNode(9,4);
        setSolidNode(9,3);
        setSolidNode(2,7);

        //Set cost on nodes
        setCostOnNodes();
        
        // Add start node to open list
        openList.add(startNode);
    }

    private void initializeNodes(){
        //Place nodes
        int col = 0;
        int row = 0;

        while(col < maxCol && row < maxRow){
            node[col][row] = new Node(col,row);
            this.add(node[col][row]);
            col++;
            if(col == maxCol){
                col = 0;
                row++;
            }
        }


    }


    private void setStartNode(int col, int row){
        startNode = node[col][row];
        startNode.setAsStart();
        startNode.gCost = 0;
        currentNode = startNode;
    }

    private void setGoalNode(int col, int row){
        goalNode = node[col][row];
        goalNode.setAsGoal();
    }
    private void setSolidNode(int col, int row){
        node[col][row].setAsSolid();
    }
    private void setCostOnNodes(){
        int col = 0;
        int row = 0;
        while(col < maxCol && row < maxRow){
            getCost(node[col][row]);
            col++;
            if(col == maxCol){
                col = 0;
                row++;
            }
        }
    }
    private void getCost(Node node){
        //G cost
        // G cost will be calculated dynamically during the search

        //H cost
        int xDistance = Math.abs(node.col - goalNode.col);
        int yDistance = Math.abs(node.row - goalNode.row);
        node.hCost = xDistance + yDistance;

        //F cost
        node.fCost = node.gCost + node.hCost;

        // Display the cost on node
        if(node != startNode && node != goalNode ){
            node.setText("<html>H: " + node.hCost + "</html>");
        }
    }
    public void search() {
        if(goalReached == false && openList.size() > 0 && steps < maxCol * maxRow) {
            Node bestNode = findBestNode();
            currentNode = bestNode;

            if(currentNode == goalNode){
                System.out.println("Goal reached");
                goalReached = true;
                tracePath();
                return; // Stop immediately when goal is reached
            }
            processCurrentNode();
            steps++;
            System.out.println("Steps: " + steps);
        }
    }

    public void autoSearch() {
        while(goalReached == false && openList.size() > 0 && steps < maxCol * maxRow) {
            Node bestNode = findBestNode();
            currentNode = bestNode;

            if(currentNode == goalNode){
                System.out.println("Goal reached in autosearch");
                goalReached = true;
                tracePath();
                return; // Stop immediately when goal is reached
            }
            processCurrentNode();
            steps++;
            System.out.println("Steps: " + steps);
        }
    }

    public void parallelSearch() {

        if(goalReached == false && openList.size() > 0 && steps < maxCol * maxRow) {
            Node bestNode = findBestNode();
            currentNode = bestNode;

            if(currentNode == goalNode){
                System.out.println("Goal reached(parallel)");
                goalReached = true;
                tracePath();
                return; // Stop immediately when goal is reached
            }
            processCurrentNodeParallel();
        }
    }

    public void parallelAutoSearch() {
        System.out.println("Starting parallel auto search with " + customThreadPool.getParallelism() + " threads.");
        while(goalReached == false && openList.size() > 0 && steps < maxCol * maxRow) {
            Node bestNode = findBestNode();
            currentNode = bestNode;

            if(currentNode == goalNode){
                System.out.println("Goal reached(parallel)");
                goalReached = true;
                tracePath();
                return; // Stop immediately when goal is reached
            }
            processCurrentNodeParallel();
        }
        if(openList.size() == 0 && !goalReached){
            System.out.println("No path found to goal(parallel auto)");
        }
    }

    private void processCurrentNodeParallel() {
        currentNode.setAsChecked();
        checkedList.add(currentNode);
        openList.remove(currentNode); 

        //Get neighbors 
        List<Node> neighbors = getNeighbors(currentNode);
        
        // Process neighbors in parallel
        neighbors.parallelStream().forEach(neighbor -> {
            searchLock.lock();
            try {
                openNode(neighbor);
            } finally {
                searchLock.unlock();
            }
        });
    
    }
    
    private List<Node> getNeighbors(Node current){
        List<Node> neighbors = new ArrayList<>();
        int col = current.col;
        int row = current.row;
        
        if( row - 1 >= 0 ){
            neighbors.add(node[col][row - 1]); //Up
        }
        if( col + 1 < maxCol ){
            neighbors.add(node[col + 1][row]); //Right
        }
        if( row + 1 < maxRow ){
            neighbors.add(node[col][row + 1]); //Down
        }
        if( col - 1 >= 0 ){
            neighbors.add(node[col - 1][row]); //Left
        }
        return neighbors;
    }

    private Node findBestNode(){
        int bestNodeIndex = 0;
        int bestNodeFCost = 999;
        for(int i = 0; i < openList.size(); i++){
            //Check if this node's F cost is better
            if(openList.get(i).fCost < bestNodeFCost){
                bestNodeIndex = i;
                bestNodeFCost = openList.get(i).fCost;
                
            }
            //If F cost is the same, check G cost
            else if(openList.get(i).fCost == bestNodeFCost){
                if(openList.get(i).gCost < openList.get(bestNodeIndex).gCost){
                    bestNodeIndex = i;
                }
            }
        }
        return openList.get(bestNodeIndex);
    }
    
    public void processCurrentNode(){
        currentNode.setAsChecked();
        checkedList.add(currentNode);
        openList.remove(currentNode); 

        //Get neighbors
        List<Node> neighbors = getNeighbors(currentNode);
        // Process neighbors sequentially
        for(Node neighbor : neighbors) {
            openNode(neighbor);
        }
    }

    public void reset() {
        // Clear lists
        openList.clear();
        checkedList.clear();
        goalReached = false;
        steps = 0;

        // Reset nodes
        int col = 0;
        int row = 0;
        while(col < maxCol && row < maxRow){
            node[col][row].parent = null;
            node[col][row].open = false;
            node[col][row].checked = false;
            if(node[col][row] != startNode && node[col][row] != goalNode && node[col][row].solid == false){
                node[col][row].setBackground(Color.white);
                node[col][row].setForeground(Color.black);
                node[col][row].setText("<html>F: " + node[col][row].fCost + "<br>G: " + node[col][row].gCost + "<br>H: " + node[col][row].hCost + "</html>");
            }
            col++;
            if(col == maxCol){
                col = 0;
                row++;
            }
        }
        // Reset current node to start node
        currentNode = startNode;
        // Add start node to open list
        openList.add(startNode);
        System.out.println("Reset complete");
    }

  private void openNode(Node node){
    if(node.open == false && node.checked == false && node.solid == false){
        // Calcular nuevo G cost
        int newGCost = currentNode.gCost + 1; // +1 por cada paso
        
        if(!node.open || newGCost < node.gCost) {
            node.gCost = newGCost;
            node.fCost = node.gCost + node.hCost;
            node.parent = currentNode;
            
            if(!node.open) {
                node.setAsOpen();
                openList.add(node);
            }
            // Actualizar texto si no es start ni goal
            if(node != startNode && node != goalNode) {
                node.setText("<html>F: " + node.fCost + "<br>G: " + node.gCost + "<br>H: " + node.hCost + "</html>");
            }
        }
    }
}

    private void tracePath(){
        Node current = goalNode;
        while(current != startNode){
            current.setAsPath();
            current = current.parent;
        }
    }

    public void generateRandomMaze(){
        clearAllSolidNodes();
        generateMaze();
        setCostOnNodes();
        reset();
        System.out.println("Random maze generated");
    }

    public void generateSimpleRandomMaze(){
        clearAllSolidNodes();
        // Obstacles have a 30% chance of being created
        for(int col = 0; col < maxCol; col++){
            for(int row = 0; row < maxRow; row++){
                if(node[col][row] != startNode && node[col][row] != goalNode){
                    if(random.nextDouble() < 0.3){
                        setSolidNode(col, row);
                    }
                }
            }
        }
        setCostOnNodes();
        reset();
        System.out.println("Simple random maze generated");
    }

    public void generateMaze(){
        for(int col = 0; col < maxCol; col++){
            for(int row = 0; row < maxRow; row++){
                if(node[col][row] != startNode && node[col][row] != goalNode){
                    if(random.nextDouble() < 0.3){
                        setSolidNode(col, row);
                    }
                }
            }
        }
    }
    private void clearAllSolidNodes() {
        for(int col = 0; col < maxCol; col++) {
            for(int row = 0; row < maxRow; row++) {
                node[col][row].solid = false;
                if(node[col][row] != startNode && node[col][row] != goalNode) {
                    node[col][row].setBackground(Color.white);
                }
            }
        }
        
    }
    public void clearMaze() {
        clearAllSolidNodes();
        setCostOnNodes();
        reset();
        System.out.println("Maze cleared");
    }



}