import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class Node extends JButton implements ActionListener{

    Node parent;
    int col;
    int row;
    float gCost;
    float hCost;
    float fCost;
    float stepCost; // Depending on terrain type
    TerrainType terrainType; 
    boolean start;
    boolean goal;
    boolean solid;
    boolean open;
    boolean checked;

    public Node(int col, int row){
        this.col = col;
        this.row = row;
        this.terrainType = TerrainType.NORMAL; // Default terrain
        this.stepCost = terrainType.getCost(); // Get cost from terrain type
        setBackground(terrainType.getColor());
        setForeground(Color.black);
        setFocusable(false); // Prevent nodes from stealing focus
        addActionListener(this);
    }

    // Overloaded constructor to set custom step cost
    public Node(int col, int row, int stepCost){
        this.col = col;
        this.row = row;
        this.stepCost = stepCost;
        this.terrainType = TerrainType.NORMAL; // Default, will be overridden if needed
        setBackground(Color.white);
        setForeground(Color.black);
        setFocusable(false); // Prevent nodes from stealing focus
        addActionListener(this);
    }

    public void setAsStart(){
        setBackground(Color.blue);
        setForeground(Color.white);
        setText("Start");
        start = true;
    }
    public void setAsGoal(){
        setBackground(Color.yellow);
        setForeground(Color.black);
        setText("Goal");
        goal = true;
    }
    public void setAsSolid(){
        setBackground(Color.gray);
        solid = true;
        terrainType = TerrainType.SOLID;
        stepCost = terrainType.getCost();
    }
    public void setAsOpen(){
        setBackground(Color.orange);
        open = true;
    }
    public void setAsChecked(){
        if(start == false && goal == false){
            setBackground(Color.red);
            setForeground(Color.black);
        }
        checked = true;
    }
    public void setAsPath(){
        setBackground(Color.green);
        setForeground(Color.black);
    }
    public void setStepCost(int cost){
        this.stepCost = cost;
    }
    public float getStepCost(){
        return this.stepCost;
    }
    
    public void setTerrainType(TerrainType type) {
        this.terrainType = type;
        this.stepCost = type.getCost();
        if (!start && !goal) {
            setBackground(type.getColor());
        }
        // Update solid status based on terrain
        this.solid = !type.isPassable();
    }
    
    public TerrainType getTerrainType() {
        return this.terrainType;
    }
    
    public void resetToNormal() {
        if (!start && !goal) {
            setTerrainType(TerrainType.NORMAL);
            open = false;
            checked = false;
        }
    }   


    @Override
    public void actionPerformed(ActionEvent e) {
        // setBackground(Color.orange);
    }
}