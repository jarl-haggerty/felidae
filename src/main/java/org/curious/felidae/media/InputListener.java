/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

import clojure.lang.IPersistentSet;
import clojure.lang.ISeq;
import clojure.lang.MapEntry;
import org.curious.felidae.state.Actor;
import org.curious.felidae.Game;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author jarl
 */
public class InputListener implements KeyListener {
    public Game game;

    public InputListener(Game game) {
        this.game = game;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        for(Object o : game.state.cast){
            ISeq seq = ((IPersistentSet)((MapEntry)o).val()).seq();
            while(seq != null){
                ((Actor)seq.first()).processInput(new Input(e, 1));
                seq = seq.next();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        for(Object o : game.state.cast){
            ISeq seq = ((IPersistentSet)((MapEntry)o).val()).seq();
            while(seq != null){
                ((Actor)seq.first()).processInput(new Input(e, 0));
                seq = seq.next();
            }
        }
    }

}
