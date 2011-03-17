/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.state;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.ISeq;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientMap;
import clojure.lang.MapEntry;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import org.curious.felidae.Game;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        ISeq seq = game.state.cast.seq();
        while(seq != null){
            MapEntry entry = (MapEntry)seq.first();
            ISeq actors = ((IPersistentSet)entry.val()).seq();
            ITransientCollection newSet = PersistentHashSet.EMPTY.asTransient();
            while(actors != null){
                Actor actor = (Actor)actors.first();
                if(actor.update(game.state)){
                    newSet.conj(actor);
                }
                actors = actors.next();
            }
            if(game.state.reset){
                game.state.reset = false;
                break;
            }
            IPersistentCollection resultSet = newSet.persistent();
            try {
                if(resultSet.count() == 0){
                    game.state.cast = game.state.cast.without(entry.key());
                }else{
                    game.state.cast = game.state.cast.assoc(entry.key(), resultSet);
                }
            } catch (Exception ex) {
                Logger.getLogger(StateListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            seq = seq.next();
        }
        game.state.world.step(game.period/1000f, 6);
    }

}
