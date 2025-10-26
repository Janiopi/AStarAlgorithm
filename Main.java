import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("A* Pathfinding with Terrain Costs");
        
        DemoPanel dp = new DemoPanel();
        window.add(dp);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        System.out.println("=== A* Pathfinding with Terrain Costs ===");
        System.out.println("Controls:");
        System.out.println("  SPACE - Step through algorithm");
        System.out.println("  ENTER - Auto search");
        System.out.println("  R - Reset");
        System.out.println("  C - Clear all terrain");
        System.out.println("  T - Generate terrain map example");
        System.out.println("  Y - Generate random terrain map");
        System.out.println("  S - Generate simple random maze");
        System.out.println("  G - Generate maze");
        System.out.println();
        System.out.println("Terrain Types:");
        System.out.println("  White - Normal (cost: 1)");
        System.out.println("  Brown - Rough terrain (cost: 2)");
        System.out.println("  Dark Green - Swamp (cost: 3)");
        System.out.println("  Gray - Mountain (cost: 4)");
        System.out.println("  Cyan - Water (cost: 5)");
        System.out.println("  Dark Gray - Wall (impassable)");
        System.out.println();
        System.out.println("Legend on nodes: H=Heuristic, S=Step cost");
    }
}
