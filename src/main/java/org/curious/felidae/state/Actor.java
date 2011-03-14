/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.state;

import org.curious.felidae.Game;
import org.curious.felidae.media.Input;
import org.curious.felidae.media.Renderer;
import javax.media.opengl.GL;

/**
 *
 * @author jarl
 */
public interface Actor {
    public String getName();
    public abstract void render(Renderer renderer);
    public boolean update(State state);
    public void processInput(Input input);
}
