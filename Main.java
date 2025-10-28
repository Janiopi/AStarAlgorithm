import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("A* Pathfinding - Minecraft Edition");
        
        DemoPanel dp = new DemoPanel();
        window.add(dp);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        System.out.println("=== A* Pathfinding - Minecraft Edition ===");
        System.out.println("Formula: f = g + 1.5*h (como en Minecraft)");
        System.out.println("Movimiento: 8 direcciones (diagonal = 1.414, horizontal = 1.0)");
        System.out.println();
        System.out.println("Controles:");
        System.out.println("  SPACE - Paso a paso del algoritmo");
        System.out.println("  ENTER - Búsqueda automática");
        System.out.println("  R - Reiniciar");
        System.out.println("  E - Abrir Editor de Escenarios");
        System.out.println("  C - Limpiar todo el terreno");
        System.out.println("  T - Generar mapa de ejemplo");
        System.out.println("  Y - Generar mapa aleatorio");
        System.out.println("  S - Generar laberinto simple");
        System.out.println("  G - Generar laberinto complejo");
        System.out.println();
        System.out.println("Bloques de Minecraft:");
        System.out.println("  Blanco - Normal (costo: 1)");
        System.out.println("  Naranja - Miel (costo: 8, no contagia)");
        System.out.println("  Azul - Agua (costo: 8, contagia 8 a vecinos)");
        System.out.println("  Azul claro - Afectado por agua (costo: 8)");
        System.out.println("  Verde - Cactus (NO CAMINABLE, contagia 8 a vecinos)");
        System.out.println("  Verde claro - Afectado por cactus (costo: 8)");
        System.out.println("  Rojo - Magma (costo: 16, SÍ CAMINABLE, contagia 8 a vecinos)");
        System.out.println("  Rojo oscuro - Afectado por magma (costo: 8)");
        System.out.println("  Gris oscuro - Muro (impassable)");
        System.out.println();
        System.out.println("En los nodos se muestra: F=costo total, G=costo acumulado");
        System.out.println();
        System.out.println("IMPORTANTE: Los bloques que 'contagian' afectan a sus 8 vecinos alrededor.");
        System.out.println("            No hay doble propagación - cada bloque solo se afecta una vez.");
        System.out.println("            El CACTUS NO es caminable pero contagia costo 8 a los vecinos.");
        System.out.println("            El MAGMA SÍ es caminable (costo 16) y contagia costo 8 a vecinos.");
        System.out.println("            En el editor, puedes mover el inicio y la meta haciendo clic.");
    }
}
