import java.awt.Color;

public enum TerrainType {
    NORMAL(1, Color.WHITE, "Normal"),
    WATER(8, Color.CYAN, "Water"),                   // Honey, water, etc
    HOT(16, Color.RED, "Hot Terrain"),            // Blocks near lava, fire, etc
    SOLID(-1, Color.DARK_GRAY, "Wall");              // Wall

    private final int cost;
    private final Color color;
    private final String name;

    TerrainType(int cost, Color color, String name) {
        this.cost = cost;
        this.color = color;
        this.name = name;
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
        return cost > 0;
    }
}
