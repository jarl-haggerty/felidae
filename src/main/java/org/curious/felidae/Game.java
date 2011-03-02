/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import java.awt.Color;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.Timer;
import org.python.core.PyDictionary;
import felidae.actors.ActorLoader;
import felidae.actors.Cast;
import felidae.audio.Audio;
import felidae.graphics.Graphics;
import felidae.input.Input;
import felidae.logging.Logger;
import felidae.scripting.Scripting;

/**
 *
 * @author Jarl
 */
public class Game {
    public static GameState currentState;
    private static boolean running;

    private static Timer timer;
    public static float delta, lastTime, speed;

    public static boolean debugging;
    public static String nextState;

    public static ActorLoader actorLoader;
    public static PyDictionary data;
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
        running &= Scripting.initialize();
        if(!running){
            return;
        }
        //running &= Input.initialize();
        if(!running){
            return;
        }
        debugging = false;
        data = new PyDictionary();
    }

    public static void run(String entryPoint){
        if(!running){
            shutdown();
            return;
        }

        timer = new Timer();
        lastTime = timer.getTime();
        delta = 0;

        nextState = null;
        currentState = new GameState(entryPoint, actorLoader);
        currentState.initialize();
        time = 0;
        FPS = 0;
        int framesThrown = 0, frames = 0;
        float d2 = 0;
        timer.reset();
        while(running){
            d2 = timer.getTime();
            delta = 1f/Display.getDisplayMode().getFrequency();
            time += d2;
            Timer.tick();
            if(d2 == 0){
                continue;
            }
            timer.reset();
            if(d2 > .1){
                framesThrown++;
                frames--;
                continue;
            }
            
            delta *= speed;
            
            Graphics.update();
	            

            if(Display.isActive()){
	            Graphics.clear();
	            
	            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
				if(controllers.length==0) {
					System.out.println("Found no controllers.");
					System.exit(0);
				}
				
				for(int i=0;i<controllers.length;i++) {
					controllers[i].poll();
					EventQueue queue = controllers[i].getEventQueue();
					Event event = new Event();
					while(queue.getNextEvent(event)) {
						StringBuffer buffer = new StringBuffer(controllers[i].getName());
						buffer.append(" at ");
						buffer.append(event.getNanos()).append(", ");
						Component comp = event.getComponent();
						buffer.append(comp.getName()).append(" changed to ");
						float value = event.getValue(); 
						if(comp.isAnalog()) {
							buffer.append(value);
						} else {
							if(value==1.0f) {
								buffer.append("On");
							} else {
								buffer.append("Off");
							}
						}
					}
				}
	            
	            currentState.update();
	            currentState.draw();
	
	            Scripting.update();
	
	            currentState.vocalize();
	            Display.sync(60);
            }

            if(Display.isCloseRequested()){
                running = false;
            }
            
            frames++;
            if(time >= 1){
            	FPS = frames/time;
            	time = 0;
            	frames = 0;
            }
            Graphics.setColor(Color.black);
            Graphics.drawStringOnView(String.valueOf(System.getProperty("java.version") + ", " + System.getProperty("java.home")), 0, 5);
            
            //GameGraphics.drawString(String.valueOf(time), 0, 100);
            //Graphics.drawString(String.valueOf(framesThrown), 0, 300);

            if(nextState != null){
                //break;
                Graphics.clearMemory();
                Audio.clearMemory();
                currentState = new GameState(nextState, actorLoader);
                currentState.initialize();
                timer.reset();
                time = 0;
                nextState = null;
            }
        }

        shutdown();
    }

    public static void shutdown(){
        Scripting.destroy();
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
