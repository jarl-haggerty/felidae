/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.actors;

import java.util.Map;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.contacts.ContactPoint;

/**
 * Base class for all actors in a game
 *
 * @author Jarl Haggerty
 */
public abstract class Actor{

    public Body body;
    public BodyDef bodyDef;
    /**
     * Boolean to indicate that an actor is dead, when true the Actor will be removed from the game
     */
    public boolean dead, dying;
    /**
     * Boolean to indicate that an actor is accepting input
     */
    public boolean interactive;
    /**
     * Boolean to indicate that an actor is being use by another thread
     */
    public boolean inUse;
    /**
     * Name assigned to the actor
     */
    public String name;

    /**
     * @return the interactive
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * @param interactive the interactive to set
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public Actor(){
        bodyDef = new BodyDef();
        bodyDef.position = new Vec2();
        bodyDef.userData = this;

        name = "";

        dead = dying = false;
        inUse = false;
        interactive = true;
    }

    /**
     * Actor constructor, will construct the name, position, and center of this actor from the Map passed to it
     * 
     * @param code  An map containing the attributes of the actor
     */
    public Actor(Map<String, String> code){
        bodyDef = new BodyDef();
        bodyDef.position = new Vec2(Float.parseFloat(code.get("X")), Float.parseFloat(code.get("Y")));
        bodyDef.userData = this;

        name = code.get("Name");

        dead = dying = false;
        inUse = false;
        interactive = true;
    }

    /**
     * Initialization function to be called by the GameState after all actors are constructed
     */
    public abstract void initialize();

    /**
     * Update function to be called at each iteration of the game
     */
    public abstract boolean update(int phase);

    /**
     * Draw function to be called at each iteration of the game
     *
     * @param offset  Vector representing the upper left corner of the view of the game world
     */
    public abstract void render();

    /**
     * Function to be called at each iteration of the game to process user input
     */
    public abstract void processInput();

    public abstract void vocalize();

    public abstract void collision(ContactPoint point);

    public abstract void seperation(ContactPoint point);

    public abstract boolean filterCollision(Shape who);

    public abstract boolean filterRaycast(Actor who);
    
    @Override
    public boolean equals(Object other){
        return this == other;
    }

    public abstract void destroy();
}
