/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import java.awt.Color;

import felidae.actors.ActorLoader;
import felidae.actors.Cast;
import felidae.audio.Audio;
import felidae.graphics.Graphics;
import felidae.input.Input;
import felidae.logging.Logger;
import felidae.scripting.Scripting;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author Jarl
 */
public class Game {
    public static GameState currentState;
    private static boolean running;

    public static float delta, lastTime, speed;

    public static boolean debugging;
    public static String nextState;

    public static ActorLoader actorLoader;
    public static Cast actors;
    public static String title;
    public static float time, FPS;

    public static void initialize(ActorLoader actorLoader, String title){
        Game.title = title;
        Game.actorLoader = actorLoader;
        Game.speed = 1;
        running = true;
        
        running &= Logger.initialize();
        if(!running){
            return;
        }
        running &= Graphics.initialize();
        if(!running){
            return;
        }
        running &= Audio.initialize();
        if(!running){
            return;
        }
        debugging = false;
    }

    public static class GameUpdater implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            currentState = currentState.update();
        }

    }

    public static void run(String entryPoint){
        if(!running){
            shutdown();
            return;
        }
        currentState = new GameState(entryPoint, actorLoader);
        currentState.initialize();
        gameTimer = new Timer(16, new GameUpdater());
        graphicsTimer = new Timer(16, new GraphicsUpdater());
    }

    public static void shutdown(){
        Graphics.destroy();
        Audio.destroy();
        Logger.destroy();
    }

    public static GameState getCurrentState(){
        return currentState;
    }

    public static void setCurrentState(GameState state){
        currentState = state;
    }

    public static float getElapsedTime(){
        return delta;
    }

    /**
     * @return the debugging
     */
    public static boolean isDebugging() {
        return debugging;
    }

    /**
     * @param aDebugging the debugging to set
     */
    public static void setDebugging(boolean aDebugging) {
        debugging = aDebugging;
    }

    public static void quit(){
        running = false;
    }
}
