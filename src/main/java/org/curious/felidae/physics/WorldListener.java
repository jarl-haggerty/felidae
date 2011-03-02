/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.physics;

import felidae.actors.Actor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.ContactPoint;

/**
 *
 * @author Jarl
 */
public class WorldListener implements ContactListener{
    public Map<Actor, Set<Actor>> addMap;
    public Set<Actor> adders;
    public Map<Actor, Set<Actor>> persistMap;
    public Set<Actor> persisters;

    public WorldListener(){
        addMap = new HashMap<Actor, Set<Actor>>();
        adders = new HashSet<Actor>();
        persistMap = new HashMap<Actor, Set<Actor>>();
        persisters = new HashSet<Actor>();
    }
    
    public void subscribeToActor(Actor subscriber, Actor publisher){
    	subscribeToActor(subscriber, publisher, false);
    }

    public void subscribeToActor(Actor subscriber, Actor publisher, boolean constant){
        if(constant){
            if(!persistMap.containsKey(subscriber)){
                persistMap.put(subscriber, new HashSet<Actor>());
            }
            persistMap.get(subscriber).add(publisher);
        }else{
            if(!addMap.containsKey(subscriber)){
                addMap.put(subscriber, new HashSet<Actor>());
            }
            addMap.get(subscriber).add(publisher);
        }
    }
    
    public void subscribe(Actor subscriber){
    	subscribe(subscriber, false);
    }

    public void subscribe(Actor subscriber, boolean constant){
        if(constant){
            persisters.add(subscriber);
        }
        adders.add(subscriber);
    }

    public void unsubscribeToActor(Actor subscriber, Actor publisher){
        addMap.get(subscriber).remove(publisher);
        if(addMap.get(subscriber).isEmpty()){
            addMap.remove(subscriber);
        }
    }

    public void add(ContactPoint point) {
        org.jbox2d.dynamics.contacts.ContactPoint reversePoint = new org.jbox2d.dynamics.contacts.ContactPoint();
        reversePoint.friction = point.friction;
        reversePoint.normal.set(point.normal.mul(-1));
        reversePoint.position.set(point.position);
        reversePoint.restitution = point.restitution;
        reversePoint.separation = point.separation;
        reversePoint.shape1 = point.shape2;
        reversePoint.shape2 = point.shape1;
        reversePoint.velocity.set(point.velocity.mul(-1));

        Set<Actor> temp = addMap.get((Actor)point.shape1.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape2.m_body.m_userData) ||
           adders.contains((Actor)point.shape1.m_body.m_userData)){
            ((Actor)point.shape1.m_body.m_userData).collision(point);
        }

        temp = addMap.get((Actor)point.shape2.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape1.m_body.m_userData) ||
           adders.contains((Actor)point.shape2.m_body.m_userData)){
            ((Actor)point.shape2.m_body.m_userData).collision(reversePoint);
        }
    }

    public void persist(org.jbox2d.dynamics.contacts.ContactPoint point) {
        org.jbox2d.dynamics.contacts.ContactPoint reversePoint = new org.jbox2d.dynamics.contacts.ContactPoint();
        reversePoint.friction = point.friction;
        reversePoint.normal.set(point.normal.mul(-1));
        reversePoint.position.set(point.position);
        reversePoint.restitution = point.restitution;
        reversePoint.separation = point.separation;
        reversePoint.shape1 = point.shape2;
        reversePoint.shape2 = point.shape1;
        reversePoint.velocity.set(point.velocity.mul(-1));


        Set<Actor> temp = persistMap.get((Actor)point.shape1.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape2.m_body.m_userData) ||
           persisters.contains((Actor)point.shape1.m_body.m_userData)){
            ((Actor)point.shape1.m_body.m_userData).collision(point);
        }

        temp = persistMap.get((Actor)point.shape2.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape1.m_body.m_userData) ||
           persisters.contains((Actor)point.shape2.m_body.m_userData)){
            ((Actor)point.shape2.m_body.m_userData).collision(reversePoint);
        }
    }

    public void remove(org.jbox2d.dynamics.contacts.ContactPoint point) {
        org.jbox2d.dynamics.contacts.ContactPoint reversePoint = new org.jbox2d.dynamics.contacts.ContactPoint();
        reversePoint.friction = point.friction;
        reversePoint.normal.set(point.normal.mul(-1));
        reversePoint.position.set(point.position);
        reversePoint.restitution = point.restitution;
        reversePoint.separation = point.separation;
        reversePoint.shape1 = point.shape2;
        reversePoint.shape2 = point.shape1;
        reversePoint.velocity.set(point.velocity.mul(-1));


        Set<Actor> temp = addMap.get((Actor)point.shape1.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape2.m_body.m_userData) ||
           adders.contains((Actor)point.shape1.m_body.m_userData)){
            ((Actor)point.shape1.m_body.m_userData).seperation(point);
        }

        temp = addMap.get((Actor)point.shape2.m_body.m_userData);
        if(temp != null && temp.contains((Actor)point.shape1.m_body.m_userData) ||
           adders.contains((Actor)point.shape2.m_body.m_userData)){
            ((Actor)point.shape2.m_body.m_userData).seperation(reversePoint);
        }
    }

    public void result(org.jbox2d.dynamics.contacts.ContactResult point) {
    }

}
