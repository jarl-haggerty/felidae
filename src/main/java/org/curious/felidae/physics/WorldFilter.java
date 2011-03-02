/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.physics;

import java.util.HashSet;
import java.util.Set;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.ContactFilter;
import org.jbox2d.dynamics.DefaultContactFilter;

import felidae.actors.Actor;

/**
 *
 * @author Jarl
 */
public class WorldFilter implements ContactFilter {
    private Set<Actor> filteringCollisions;
    private Set<Actor> filteringRaycasts;
    private DefaultContactFilter baseFilter;

    public WorldFilter(){
        filteringCollisions = new HashSet<Actor>();
        filteringRaycasts = new HashSet<Actor>();
        baseFilter = new DefaultContactFilter();
    }

    public void filterCollisions(Actor filterer){
        filteringCollisions.add(filterer);
    }

    public void unfilterCollisions(Actor filterer){
        filteringCollisions.remove(filterer);
    }

    public void filterRaycasts(Actor filterer){
        filteringRaycasts.add(filterer);
    }

    public void unfilterRaycasts(Actor filterer){
        filteringRaycasts.remove(filterer);
    }

    public boolean shouldCollide(Shape shape1, Shape shape2) {
        boolean result = true;
        if(filteringCollisions.contains((Actor)shape1.getBody().getUserData())){
            result &= ((Actor)shape1.getBody().getUserData()).filterCollision(shape2);
        }
//        if(filteringCollisions.contains((Actor)shape2.getBody().getUserData())){
//           result &= ((Actor) shape2.getBody().getUserData()).filterCollision(shape1);
//        }
        return result && baseFilter.shouldCollide(shape1, shape2);
    }

    public boolean rayCollide(Object userData, Shape shape) {
        boolean result = true;
        if(filteringRaycasts.contains((Actor)userData)){
            result &= ((Actor)userData).filterRaycast((Actor)shape.getBody().getUserData());
        }
//        if(filteringRaycasts.contains((Actor)shape.getBody().getUserData())){
//            result &= ((Actor)shape.getBody().getUserData()).filterRaycast((Actor)userData);
//        }
        return result && baseFilter.rayCollide(userData, shape);
    }
}
