/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.state;

import clojure.lang.IPersistentSet;
import clojure.lang.ISeq;
import clojure.lang.MapEntry;
import felidae.Game;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author jarl
 */
public class StateListener implements ActionListener{
    public Game game;
    public StateListener(Game game) {
        this.game = game;
    }

    public void actionPerformed(ActionEvent e) {
        for(Object o : game.state.cast){
            ISeq seq = ((IPersistentSet)((MapEntry)o).val()).seq();
            while(seq != null){
                ((Actor)seq.first()).update(game);
                seq = seq.next();
            }
        }
    }

}
