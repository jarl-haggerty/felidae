/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.actors;

import java.util.Map;
import org.curious.felidae.media.Input;
import org.curious.felidae.media.Renderer;
import org.curious.felidae.state.Actor;
import org.curious.felidae.state.State;

/**
 *
 * @author jarl
 */
public class Camera implements Actor {
    public double x, y, height;

    public Camera(Map<String, String> data){
        x = Double.parseDouble(data.get("X"));
        y = Double.parseDouble(data.get("Y"));
        height = Double.parseDouble(data.get("Height"));
    }

    public String getName() {
        return "Camera";
    }

    public void render(Renderer renderer) {
        renderer.setView(x, y, height*renderer.getViewRatio(), height);
    }

    public boolean update(State state) {
        return true;
    }

    public void processInput(Input input) {
    }

}
