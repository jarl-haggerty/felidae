/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.exchange;

import felidae.Game;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * @author jarl
 */
public class DisplayListener implements WindowListener {
    public Game game;

    public DisplayListener(Game game){
        this.game = game;
    }

    public void windowOpened(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowClosing(WindowEvent e) {
        game.stop();
    }

    public void windowClosed(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowIconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowDeiconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowActivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void windowDeactivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
