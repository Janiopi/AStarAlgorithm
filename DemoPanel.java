import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.util.Enumeration;

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
    
    // Edit mode
    private ButtonGroup editToolGroup = null;


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

        // Cargar escenario de ejemplo de Minecraft
        loadMinecraftExampleScenario();

        //Set cost on nodes
        setCostOnNodes();
        
        // Add start node to open list
        openList.add(startNode);
    }
    
    private void loadMinecraftExampleScenario() {
        // Ejemplo con bloques de Minecraft
        setTerrainWithSpread(3, 1, TerrainType.WATER);
        setTerrainWithSpread(11, 2, TerrainType.CACTUS);
        setTerrainWithSpread(7, 7, TerrainType.MAGMA);
        
        node[2][6].setTerrainType(TerrainType.HONEY);
        node[12][2].setTerrainType(TerrainType.HONEY);
        
        // Algunos muros
        setSolidNode(7,1);
        setSolidNode(7,2);
        setSolidNode(7,3);
        setSolidNode(5,5);
        setSolidNode(6,5);
        setSolidNode(9,5);
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

    private void setTerrainWithSpread(int col, int row, TerrainType type) {
        if (col < 0 || col >= maxCol || row < 0 || row >= maxRow) return;
        
        // Permitir cambiar terreno incluso en start/goal en modo edición
        // pero preservar la etiqueta de start/goal
        boolean wasStart = (node[col][row] == startNode);
        boolean wasGoal = (node[col][row] == goalNode);
        
        node[col][row].setTerrainType(type);
        
        // Restaurar etiquetas si era start o goal
        if (wasStart) {
            node[col][row].setAsStart();
        }
        if (wasGoal) {
            node[col][row].setAsGoal();
        }
        
        // Si el terreno se propaga, aplicar efecto a vecinos
        if (type.spreads()) {
            applySpreadEffect(col, row, type);
        }
    }
    
    private void applySpreadEffect(int col, int row, TerrainType sourceType) {
        // Aplicar efecto a los 8 vecinos alrededor (incluye diagonales)
        int[][] neighbors = {
            {-1, -1}, {0, -1}, {1, -1},  // Arriba izq, arriba, arriba der
            {-1,  0},          {1,  0},  // Izquierda, derecha
            {-1,  1}, {0,  1}, {1,  1}   // Abajo izq, abajo, abajo der
        };
        
        for (int[] neighbor : neighbors) {
            int newCol = col + neighbor[0];
            int newRow = row + neighbor[1];
            
            if (newCol >= 0 && newCol < maxCol && newRow >= 0 && newRow < maxRow) {
                Node neighborNode = node[newCol][newRow];
                
                // No afectar a start, goal, ni a otros bloques especiales de tipo fuente
                if (neighborNode != startNode && neighborNode != goalNode) {
                    TerrainType currentType = neighborNode.getTerrainType();
                    
                    // Solo afectar a bloques normales (no sobrescribir otros efectos)
                    // Esto evita la doble propagación
                    if (currentType == TerrainType.NORMAL) {
                        neighborNode.setTerrainType(sourceType.getAffectedType());
                    }
                }
            }
        }
    }
    
    // Recalcular todos los efectos de propagación
    private void recalculateSpreadEffects() {
        // Primero, limpiar todos los efectos
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                TerrainType type = node[col][row].getTerrainType();
                if (type == TerrainType.WATER_AFFECTED || 
                    type == TerrainType.CACTUS_AFFECTED || 
                    type == TerrainType.MAGMA_AFFECTED) {
                    if (node[col][row] != startNode && node[col][row] != goalNode) {
                        node[col][row].setTerrainType(TerrainType.NORMAL);
                    }
                }
            }
        }
        
        // Luego, aplicar todos los efectos de nuevo
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                TerrainType type = node[col][row].getTerrainType();
                if (type.spreads()) {
                    applySpreadEffect(col, row, type);
                }
            }
        }
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

        //H cost - Distancia Manhattan simple
        node.hCost = calculateHeuristic(node);

        //F cost - Minecraft usa f = g + 1.5*h
        node.fCost = node.gCost + 1.5f * node.hCost;

        // Display the cost on node
        if(node != startNode && node != goalNode && !node.solid){
            node.setText("<html>H:" + String.format("%.1f", node.hCost) + "<br>S:" + (int)node.stepCost + "</html>");
        }
    }
    
    private float calculateHeuristic(Node node) {
        // Distancia Euclidiana (línea recta desde el nodo hasta la meta)
        // Incluye movimiento diagonal de forma natural
        int dx = Math.abs(node.col - goalNode.col);
        int dy = Math.abs(node.row - goalNode.row);
        
        // h = √(dx² + dy²)
        return (float) Math.sqrt(dx * dx + dy * dy);
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
        
        // Movimiento en 8 direcciones (incluye diagonales, como en Minecraft)
        // Arriba
        if( row - 1 >= 0 ){
            neighbors.add(node[col][row - 1]);
        }
        // Arriba-derecha (diagonal)
        if( row - 1 >= 0 && col + 1 < maxCol ){
            neighbors.add(node[col + 1][row - 1]);
        }
        // Derecha
        if( col + 1 < maxCol ){
            neighbors.add(node[col + 1][row]);
        }
        // Abajo-derecha (diagonal)
        if( row + 1 < maxRow && col + 1 < maxCol ){
            neighbors.add(node[col + 1][row + 1]);
        }
        // Abajo
        if( row + 1 < maxRow ){
            neighbors.add(node[col][row + 1]);
        }
        // Abajo-izquierda (diagonal)
        if( row + 1 < maxRow && col - 1 >= 0 ){
            neighbors.add(node[col - 1][row + 1]);
        }
        // Izquierda
        if( col - 1 >= 0 ){
            neighbors.add(node[col - 1][row]);
        }
        // Arriba-izquierda (diagonal)
        if( row - 1 >= 0 && col - 1 >= 0 ){
            neighbors.add(node[col - 1][row - 1]);
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
        // Calcular si el movimiento es diagonal
        boolean isDiagonal = (currentNode.col != node.col) && (currentNode.row != node.row);
        
        // Costo de movimiento: 1.414 para diagonal, 1.0 para horizontal/vertical
        float movementCost = isDiagonal ? 1.414f : 1.0f;
        
        // Calcular nuevo G cost: costo de movimiento × costo del terreno del nodo destino
        float newGCost = currentNode.gCost + (movementCost * node.getStepCost());

        if(!node.open || newGCost < node.gCost) {
            node.gCost = newGCost;
            node.fCost = node.gCost + 1.5f * node.hCost; // Minecraft: f = g + 1.5*h
            node.parent = currentNode;
            
            if(!node.open) {
                node.setAsOpen();
                openList.add(node);
            }
            // Actualizar texto si no es start ni goal
            if(node != startNode && node != goalNode) {
                node.setText("<html>F:" + String.format("%.1f", node.fCost) + "<br>G:" + String.format("%.1f", node.gCost) + "</html>");
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
        
        // Ejemplo de terrenos de Minecraft
        setTerrainWithSpread(3, 3, TerrainType.WATER);
        setTerrainWithSpread(8, 2, TerrainType.CACTUS);
        setTerrainWithSpread(12, 1, TerrainType.MAGMA);
        
        node[6][6].setTerrainType(TerrainType.HONEY);
        node[7][6].setTerrainType(TerrainType.HONEY);
        
        // Add some walls
        setSolidNode(10, 0);
        setSolidNode(10, 1);
        setSolidNode(10, 2);
        setSolidNode(10, 3);
        
        setCostOnNodes();
        reset();
        System.out.println("Terrain map generated with Minecraft blocks");
    }

    public void generateRandomTerrainMap() {
        clearAllSolidNodes();
        
        for(int col = 0; col < maxCol; col++) {
            for(int row = 0; row < maxRow; row++) {
                if(node[col][row] != startNode && node[col][row] != goalNode) {
                    double rand = random.nextDouble();
                    if(rand < 0.1) {
                        setSolidNode(col, row); // 10% walls
                    } else if(rand < 0.15) {
                        setTerrainWithSpread(col, row, TerrainType.WATER); // 5% water
                    } else if(rand < 0.20) {
                        setTerrainWithSpread(col, row, TerrainType.CACTUS); // 5% cactus
                    } else if(rand < 0.25) {
                        node[col][row].setTerrainType(TerrainType.HONEY); // 5% honey
                    } else if(rand < 0.28) {
                        setTerrainWithSpread(col, row, TerrainType.MAGMA); // 3% magma
                    }
                    // Rest remains normal terrain (72%)
                }
            }
        }
        
        setCostOnNodes();
        reset();
        System.out.println("Random Minecraft terrain map generated");
    }
    
    // ========== FUNCIONES PARA EL EDITOR DE ESCENARIOS ==========
    
    public void setEditMode(boolean enabled, ButtonGroup toolGroup) {
        this.editToolGroup = toolGroup;
        
        if (enabled) {
            // Habilitar modo edición en los nodos
            for (int col = 0; col < maxCol; col++) {
                for (int row = 0; row < maxRow; row++) {
                    final int c = col;
                    final int r = row;
                    node[col][row].setEditMode(true, e -> handleNodeClick(c, r));
                }
            }
        } else {
            // Deshabilitar modo edición
            for (int col = 0; col < maxCol; col++) {
                for (int row = 0; row < maxRow; row++) {
                    node[col][row].setEditMode(false, null);
                }
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void handleNodeClick(int col, int row) {
        if (editToolGroup == null) return;
        
        Enumeration<javax.swing.AbstractButton> buttons = editToolGroup.getElements();
        while (buttons.hasMoreElements()) {
            JRadioButton button = (JRadioButton) buttons.nextElement();
            if (button.isSelected()) {
                Object terrainObj = button.getClientProperty("terrainType");
                
                if (terrainObj instanceof TerrainType) {
                    TerrainType type = (TerrainType) terrainObj;
                    setTerrainWithSpread(col, row, type);
                    recalculateSpreadEffects();
                    setCostOnNodes();
                } else if (button.getText().equals("Inicio")) {
                    // Limpiar el nodo anterior de inicio
                    if (startNode != null) {
                        startNode.start = false;
                        startNode.setTerrainType(TerrainType.NORMAL);
                    }
                    setStartNode(col, row);
                    setCostOnNodes();
                    reset();
                } else if (button.getText().equals("Meta")) {
                    // Limpiar el nodo anterior de meta
                    if (goalNode != null) {
                        goalNode.goal = false;
                        goalNode.setTerrainType(TerrainType.NORMAL);
                    }
                    setGoalNode(col, row);
                    setCostOnNodes();
                    reset();
                }
                break;
            }
        }
    }
    
    public Scenario exportScenario(String name) {
        Scenario scenario = new Scenario(name, maxCol, maxRow);
        scenario.startNode = new Scenario.Position(startNode.col, startNode.row);
        scenario.goalNode = new Scenario.Position(goalNode.col, goalNode.row);
        
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                TerrainType type = node[col][row].getTerrainType();
                if (type != TerrainType.NORMAL && 
                    node[col][row] != startNode && 
                    node[col][row] != goalNode) {
                    scenario.addTerrain(col, row, type);
                }
            }
        }
        
        return scenario;
    }
    
    public void loadScenario(Scenario scenario) {
        // Limpiar todo primero
        clearAllSolidNodes();
        
        // Establecer start y goal
        setStartNode(scenario.startNode.col, scenario.startNode.row);
        setGoalNode(scenario.goalNode.col, scenario.goalNode.row);
        
        // Cargar todos los terrenos
        for (Scenario.TerrainData terrain : scenario.terrains) {
            try {
                TerrainType type = TerrainType.valueOf(terrain.terrainType);
                
                // No usar setTerrainWithSpread para terrenos afectados
                if (type == TerrainType.WATER_AFFECTED || 
                    type == TerrainType.CACTUS_AFFECTED || 
                    type == TerrainType.MAGMA_AFFECTED) {
                    // Estos se regenerarán automáticamente
                    continue;
                } else if (type == TerrainType.SOLID) {
                    setSolidNode(terrain.col, terrain.row);
                } else if (type.spreads()) {
                    setTerrainWithSpread(terrain.col, terrain.row, type);
                } else {
                    node[terrain.col][terrain.row].setTerrainType(type);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown terrain type: " + terrain.terrainType);
            }
        }
        
        // Recalcular efectos de propagación
        recalculateSpreadEffects();
        setCostOnNodes();
        reset();
        
        System.out.println("Scenario loaded: " + scenario.name);
    }
    
    public void openScenarioEditor(java.awt.Frame parent) {
        ScenarioEditorDialog editor = new ScenarioEditorDialog(parent, this);
        editor.setVisible(true);
    }



}