/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

import org.curious.felidae.Game;
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
    }

    public void windowClosing(WindowEvent e) {
        game.stop();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

}
