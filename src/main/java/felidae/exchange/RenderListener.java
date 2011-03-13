/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.exchange;

import clojure.lang.IPersistentSet;
import clojure.lang.ISeq;
import clojure.lang.MapEntry;
import felidae.state.Actor;
import felidae.Game;
import javax.media.opengl.GL;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dispose(GLAutoDrawable glad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void display(GLAutoDrawable glad) {
        GL gl = glad.getGL();
        if(game.graphics.changeClearColor){
            game.graphics.changeClearColor = false;
            gl.glClearColor(game.graphics.clearColor.getRed()/255f,
                            game.graphics.clearColor.getGreen()/255f,
                            game.graphics.clearColor.getBlue()/255f,
                            game.graphics.clearColor.getAlpha()/255f);
        }
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        for(Object o : game.state.cast){
            ISeq seq = ((IPersistentSet)((MapEntry)o).val()).seq();
            while(seq != null){
                ((Actor)seq.first()).render(gl);
                seq = seq.next();
            }
        }
    }

    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
