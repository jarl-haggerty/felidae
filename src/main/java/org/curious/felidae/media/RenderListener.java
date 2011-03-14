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
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

/**
 *
 * @author jarl
 */
public class RenderListener implements GLEventListener {
    public Game game;

    public RenderListener(Game game){
        this.game = game;
    }

    public void init(GLAutoDrawable glad) {
    }

    public void dispose(GLAutoDrawable glad) {
    }

    public void display(GLAutoDrawable glad) {
        GL2 gl = glad.getGL().getGL2();
        Renderer renderer = new Renderer(gl, game);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        for(Object o : game.state.cast){
            ISeq seq = ((IPersistentSet)((MapEntry)o).val()).seq();
            while(seq != null){
                ((Actor)seq.first()).render(renderer);
                seq = seq.next();
            }
        }
    }

    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
    }

}
