/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.actors;

import org.curious.felidae.Game;
import org.curious.felidae.media.Input;
import org.curious.felidae.media.Renderer;
import org.curious.felidae.state.Actor;
import org.curious.felidae.state.State;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.media.opengl.GL;

/**
 *
 * @author jarl
 */
public class Background implements Actor {
    public Color color;
    public boolean backgroundSet, quit;

    public Background(Map<String, String> data){
        backgroundSet = false;
        quit = false;
        String[] colorComponents = data.get("Color").split(",");
        color = new Color(Integer.parseInt(colorComponents[0]),
                          Integer.parseInt(colorComponents[1]),
                          Integer.parseInt(colorComponents[2]));
    }

    public String getName() {
        return "Background";
    }

    public void render(Renderer renderer) {
        if(!backgroundSet){
            renderer.setClearColor(color);
            backgroundSet = true;
        }
    }

    public boolean update(State state) {
        if(quit){
            state.game.stop();
        }
        return true;
    }

    public void processInput(Input input) {
        if(((KeyEvent)input.event).getKeyCode() == KeyEvent.VK_ESCAPE){
            quit = true;
        }
    }
}
