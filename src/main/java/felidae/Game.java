/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import felidae.state.State;
import felidae.exchange.Graphics;

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
        graphics.load("settings.xml");

        state.start();
        graphics.start();
    }

    public void stop(){
        state.stop();
        graphics.stop();
    }
}
