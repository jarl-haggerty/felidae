/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.actors;

import clojure.lang.IPersistentCollection;
import clojure.lang.ISeq;
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
    public double height;
    public String tracking;
    public Box tracked = null;

    public Camera(Map<String, String> data){
        tracking = data.get("Tracking");
        height = Double.parseDouble(data.get("Height"));
    }

    public String getName() {
        return "Camera";
    }

    public void render(Renderer renderer) {
        if(tracked != null){
            double width = height*renderer.getViewRatio();
            renderer.setView(tracked.body.getWorldCenter().x - width/2,
                             tracked.body.getWorldCenter().y - height/2,
                             width,
                             height);
        }
    }

    public boolean update(State state) {
        if(tracked == null){
            tracked = (Box)((IPersistentCollection)state.cast.valAt(tracking)).seq().first();
        }
        return true;
    }

    public void processInput(Input input) {
    }

}
