import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
            dp.parallelAutoSearch();
            long end = System.nanoTime();
            double timeMs = (end - start) / 1_000_000.0;
            System.out.println("Parallel search took: " + String.format("%.2f", timeMs) + " ms");
        }
         if(code == KeyEvent.VK_SPACE){
            dp.parallelSearch();
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
