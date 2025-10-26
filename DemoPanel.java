import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        //Set solid nodes and different terrain types
        setSolidNode(7,1);
        setSolidNode(7,2);
        setSolidNode(7,3);
        
        // Add different terrain types as examples
        setWaterTerrain(3,1);
        setWaterTerrain(4,1);
        setWaterTerrain(11,2);
        setHotTerrain(12,2);
        setHotTerrain(1,6);
        setHotTerrain(2,6);
        setWaterTerrain(13,7);
        setWaterTerrain(14,7);
        
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

    private void setHotTerrain(int col, int row) {
        node[col][row].setTerrainType(TerrainType.HOT);
    }

    private void setWaterTerrain(int col, int row) {
        node[col][row].setTerrainType(TerrainType.WATER);
    }
    
    // Métodos compatibles para los terrenos faltantes
    private void setRoughTerrain(int col, int row) {
        setWaterTerrain(col, row); // Usar WATER como rough terrain
    }
    
    private void setSwampTerrain(int col, int row) {
        setWaterTerrain(col, row); // Usar WATER como swamp
    }
    
    private void setMountainTerrain(int col, int row) {
        setHotTerrain(col, row); // Usar HOT como mountain
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

        //H cost - Now with terrain consideration
        node.hCost = calculateHeuristic(node);

        //F cost ( using weighted A* like in Minecraft)
        node.fCost = node.gCost + 1.5f * node.hCost;

        // Display the cost on node
        if(node != startNode && node != goalNode && !node.solid){
            node.setText("<html>H:" + String.format("%.1f", node.hCost) + "<br>S:" + (int)node.stepCost + "</html>");
        }
    }
    
    private float calculateHeuristic(Node node) {
        // Basic Manhattan distance
        int xDistance = Math.abs(node.col - goalNode.col);
        int yDistance = Math.abs(node.row - goalNode.row);
        float basicDistance = xDistance + yDistance;
        
        // Option 1: Simple heuristic (current approach)
        // return basicDistance;
        
        // Option 2: Terrain-aware heuristic
        return calculateTerrainAwareHeuristic(node, basicDistance);
    }
    
    private float calculateTerrainAwareHeuristic(Node node, float basicDistance) {
        // Average terrain cost estimation
        float averageTerrainCost = estimateAverageTerrainCost(node);
        
        // Multiply basic distance by estimated average terrain cost
        // But keep it admissible by using minimum possible cost (1.0)
        float terrainAwareH = basicDistance * Math.max(1.0f, averageTerrainCost * 0.8f);
        
        return terrainAwareH;
    }
    
    private float estimateAverageTerrainCost(Node startNode) {
        // Sample some nodes between current node and goal to estimate terrain
        float totalCost = 0;
        int samples = 0;
        
        int deltaCol = goalNode.col - startNode.col;
        int deltaRow = goalNode.row - startNode.row;
        int steps = Math.max(Math.abs(deltaCol), Math.abs(deltaRow));
        
        if (steps == 0) return 1.0f;
        
        // Sample a few points along the direct path
        for (int i = 1; i <= Math.min(steps, 5); i++) {
            int sampleCol = startNode.col + (deltaCol * i) / steps;
            int sampleRow = startNode.row + (deltaRow * i) / steps;
            
            // Ensure within bounds
            if (sampleCol >= 0 && sampleCol < maxCol && sampleRow >= 0 && sampleRow < maxRow) {
                totalCost += node[sampleCol][sampleRow].getStepCost();
                samples++;
            }
        }
        
        return samples > 0 ? totalCost / samples : 1.0f;
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
        float bestNodeFCost = 999;
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
                // Restore terrain color
                node[col][row].setBackground(node[col][row].getTerrainType().getColor());
                node[col][row].setForeground(Color.black);
                node[col][row].setText("<html>H:" + node[col][row].hCost + "<br>S:" + (int)node[col][row].stepCost + "</html>");
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
        // Calcular nuevo G cost usando el costo del nodo destino
        float newGCost = currentNode.gCost + node.getStepCost();

        if(!node.open || newGCost < node.gCost) {
            node.gCost = newGCost;
            node.fCost = node.gCost + 1.5f * node.hCost; // Maintain weighted A*
            node.parent = currentNode;
            
            if(!node.open) {
                node.setAsOpen();
                openList.add(node);
            }
            // Actualizar texto si no es start ni goal
            if(node != startNode && node != goalNode) {
                node.setText("<html>F:" + String.format("%.1f", node.fCost) + "<br>G:" + String.format("%.1f", node.gCost) + "<br>H:" + node.hCost + "</html>");
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
                if(node[col][row] != startNode && node[col][row] != goalNode) {
                    node[col][row].setTerrainType(TerrainType.NORMAL);
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

    public void generateTerrainMap() {
        clearAllSolidNodes();
        
        // Create some example terrain patterns
        // Add some rough terrain
        setRoughTerrain(3, 3);
        setRoughTerrain(4, 3);
        setRoughTerrain(5, 3);
        
        // Add swamp areas
        setSwampTerrain(8, 2);
        setSwampTerrain(9, 2);
        setSwampTerrain(8, 3);
        setSwampTerrain(9, 3);
        
        // Add mountain terrain
        setMountainTerrain(12, 1);
        setMountainTerrain(12, 2);
        setMountainTerrain(13, 1);
        
        // Add water (very expensive)
        setWaterTerrain(6, 6);
        setWaterTerrain(7, 6);
        setWaterTerrain(8, 6);
        setWaterTerrain(6, 7);
        setWaterTerrain(7, 7);
        setWaterTerrain(8, 7);
        
        // Add some walls
        setSolidNode(10, 0);
        setSolidNode(10, 1);
        setSolidNode(10, 2);
        setSolidNode(10, 3);
        
        setCostOnNodes();
        reset();
        System.out.println("Terrain map generated with different step costs");
    }

    public void generateRandomTerrainMap() {
        clearAllSolidNodes();
        
        for(int col = 0; col < maxCol; col++) {
            for(int row = 0; row < maxRow; row++) {
                if(node[col][row] != startNode && node[col][row] != goalNode) {
                    double rand = random.nextDouble();
                    if(rand < 0.1) {
                        setSolidNode(col, row); // 10% walls
                    } else if(rand < 0.25) {
                        setRoughTerrain(col, row); // 15% rough terrain
                    } else if(rand < 0.35) {
                        setSwampTerrain(col, row); // 10% swamp
                    } else if(rand < 0.42) {
                        setMountainTerrain(col, row); // 7% mountain
                    } else if(rand < 0.47) {
                        setWaterTerrain(col, row); // 5% water
                    }
                    // Rest remains normal terrain (53%)
                }
            }
        }
        
        setCostOnNodes();
        reset();
        System.out.println("Random terrain map generated");
    }



}