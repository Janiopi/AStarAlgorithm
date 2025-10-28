import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.SwingUtilities;

public class KeyHandler implements KeyListener {
    DemoPanel dp;

    public KeyHandler(DemoPanel dp){
        this.dp = dp;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_ENTER){
            dp.reset(); // Reset before search to ensure fair timing
            long start = System.nanoTime();
            dp.autoSearch();
            long end = System.nanoTime();
            double timeMs = (end - start) / 1_000_000.0;
            System.out.println("Search took: " + String.format("%.2f", timeMs) + " ms");
        }
         if(code == KeyEvent.VK_SPACE){
            dp.search();
        }
        if( code == KeyEvent.VK_R){
            dp.reset();
        }
        if( code == KeyEvent.VK_G){
            dp.generateMaze();
        }
        if ( code == KeyEvent.VK_C){
            dp.clearMaze();
        }
        if ( code == KeyEvent.VK_T){
            dp.generateTerrainMap();
        }
        if ( code == KeyEvent.VK_Y){
            dp.generateRandomTerrainMap();
        }
        if ( code == KeyEvent.VK_S){
            dp.generateSimpleRandomMaze();
        }
        if ( code == KeyEvent.VK_E){
            // Abrir editor de escenarios
            SwingUtilities.invokeLater(() -> {
                java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(dp);
                dp.openScenarioEditor(frame);
            });
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
    }
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }


    
}
