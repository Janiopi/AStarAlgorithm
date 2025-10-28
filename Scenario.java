import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para guardar y cargar escenarios de pathfinding
 */
public class Scenario {
    public String name;
    public int maxCol;
    public int maxRow;
    public Position startNode;
    public Position goalNode;
    public List<TerrainData> terrains;
    
    public Scenario() {
        terrains = new ArrayList<>();
    }
    
    public Scenario(String name, int maxCol, int maxRow) {
        this.name = name;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        this.terrains = new ArrayList<>();
    }
    
    public static class Position {
        public int col;
        public int row;
        
        public Position() {}
        
        public Position(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
    
    public static class TerrainData {
        public int col;
        public int row;
        public String terrainType; // Nombre del enum
        
        public TerrainData() {}
        
        public TerrainData(int col, int row, String terrainType) {
            this.col = col;
            this.row = row;
            this.terrainType = terrainType;
        }
    }
    
    public void addTerrain(int col, int row, TerrainType type) {
        terrains.add(new TerrainData(col, row, type.name()));
    }
    
    /**
     * Guarda el escenario en un archivo
     */
    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("SCENARIO:" + name + "\n");
            writer.write("DIMENSIONS:" + maxCol + "," + maxRow + "\n");
            writer.write("START:" + startNode.col + "," + startNode.row + "\n");
            writer.write("GOAL:" + goalNode.col + "," + goalNode.row + "\n");
            writer.write("TERRAINS\n");
            for (TerrainData terrain : terrains) {
                writer.write(terrain.col + "," + terrain.row + "," + terrain.terrainType + "\n");
            }
            writer.write("END\n");
        }
    }
    
    /**
     * Carga un escenario desde un archivo
     */
    public static Scenario loadFromFile(String filename) throws IOException {
        Scenario scenario = new Scenario();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingTerrains = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SCENARIO:")) {
                    scenario.name = line.substring(9);
                } else if (line.startsWith("DIMENSIONS:")) {
                    String[] dims = line.substring(11).split(",");
                    scenario.maxCol = Integer.parseInt(dims[0]);
                    scenario.maxRow = Integer.parseInt(dims[1]);
                } else if (line.startsWith("START:")) {
                    String[] pos = line.substring(6).split(",");
                    scenario.startNode = new Position(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                } else if (line.startsWith("GOAL:")) {
                    String[] pos = line.substring(5).split(",");
                    scenario.goalNode = new Position(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                } else if (line.equals("TERRAINS")) {
                    readingTerrains = true;
                } else if (line.equals("END")) {
                    break;
                } else if (readingTerrains && !line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        scenario.terrains.add(new TerrainData(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            parts[2]
                        ));
                    }
                }
            }
        }
        
        return scenario;
    }
    
    /**
     * Lista todos los escenarios disponibles en un directorio
     */
    public static List<String> listScenarios(String directory) {
        List<String> scenarios = new ArrayList<>();
        File dir = new File(directory);
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".scenario"));
        if (files != null) {
            for (File file : files) {
                scenarios.add(file.getName().replace(".scenario", ""));
            }
        }
        
        return scenarios;
    }
}
