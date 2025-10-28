import java.awt.Color;

public enum TerrainType {
    NORMAL(1, Color.WHITE, "Normal", false, 0),
    WATER(8, new Color(0, 150, 255), "Water", true, 8),           // Agua - contagia 8
    CACTUS(8, new Color(34, 139, 34), "Cactus", true, 8),        // Cactus - contagia 8, NO caminable
    HONEY(8, new Color(255, 165, 0), "Honey", false, 0),         // Miel - no contagia
    MAGMA(16, new Color(255, 69, 0), "Magma", true, 8),          // Magma - caminable costo 16, contagia 8
    MAGMA_AFFECTED(8, new Color(200, 50, 0), "Magma Affected", false, 0), // Bloques afectados por magma
    WATER_AFFECTED(8, new Color(100, 180, 255), "Water Affected", false, 0), // Bloques afectados por agua
    CACTUS_AFFECTED(8, new Color(100, 180, 100), "Cactus Affected", false, 0), // Bloques afectados por cactus
    SOLID(-1, Color.DARK_GRAY, "Wall", false, 0);                // Muro

    private final int cost;
    private final Color color;
    private final String name;
    private final boolean spreads;           // Si contagia a vecinos
    private final int spreadCost;            // El costo que aplica a vecinos

    TerrainType(int cost, Color color, String name, boolean spreads, int spreadCost) {
        this.cost = cost;
        this.color = color;
        this.name = name;
        this.spreads = spreads;
        this.spreadCost = spreadCost;
    }

    public int getCost() {
        return cost;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public boolean isPassable() {
        // Cactus NO es caminable, Magma SÍ es caminable
        return cost > 0 && this != CACTUS;
    }
    
    public boolean spreads() {
        return spreads;
    }
    
    public int getSpreadCost() {
        return spreadCost;
    }
    
    // Obtiene el tipo de terreno afectado correspondiente
    public TerrainType getAffectedType() {
        switch(this) {
            case WATER:
                return WATER_AFFECTED;
            case CACTUS:
                return CACTUS_AFFECTED;
            case MAGMA:
                return MAGMA_AFFECTED;
            default:
                return NORMAL;
        }
    }
}
