/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae;

import org.curious.felidae.state.State;
import org.curious.felidae.media.Graphics;
import javax.media.opengl.GLProfile;

/**
 *
 * @author jarl
 */
public class Game {
    public String title;
    public State state;
    public Graphics graphics;
    public int period = 16;

    public Game(String title){
        this.title = title;
        this.state = new State(this);
        this.graphics = new Graphics(this);
    }

    public void start(String entryPoint){
        state.load(entryPoint);
        graphics.load("Settings.xml");
        state.start();
        graphics.start();
    }

    public void stop(){
        state.stop();
        graphics.stop();
    }
}
