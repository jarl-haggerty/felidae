/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package physics;

import org.curious.felidae.state.Actor;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

/**
 *
 * @author jarl
 */
public class WorldListener implements ContactListener {

    public void add(ContactPoint point) {
        ((Actor)point.shape1.getBody().getUserData()).processContact(point);
        ((Actor)point.shape2.getBody().getUserData()).processContact(point);
    }

    public void persist(ContactPoint point) {
    }

    public void remove(ContactPoint point) {
    }

    public void result(ContactResult point) {
    }

}
