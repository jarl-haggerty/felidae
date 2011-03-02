/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.actors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jarl
 */
public class Cast implements Collection<Actor> {
    private List<Actor> actorList;
    private Map<String, Actor> actorMap;

    public Cast(){
        actorList = new LinkedList<Actor>();
        actorMap = new HashMap<String, Actor>();
    }

    public int size() {
        return actorList.size();
    }

    public boolean isEmpty() {
        return actorList.isEmpty();
    }

    public boolean contains(Object o) {
        if(o instanceof Actor){
            return actorList.contains((Actor)o);
        }else{
            return false;
        }
    }

    public Iterator<Actor> iterator() {
        return actorList.iterator();
    }

    public Object[] toArray() {
        return actorList.toArray();
    }

    @SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) {
        return actorList.toArray(a);
    }

    public boolean add(Actor o) {
        actorMap.put(o.name, o);
        return actorList.add(o);
    }

    public boolean remove(Object o) {
        if(o instanceof Actor){
            Actor a = (Actor)o;
            if(!a.name.isEmpty()){
                actorMap.remove(a.name);
            }
            return actorList.remove(a);
        }else{
            return false;
        }
    }

    public boolean containsAll(Collection<?> c) {
        return actorList.containsAll(c);
    }

    public boolean addAll(Collection<? extends Actor> c) {
        boolean result = true;
        for(Object o : c){
            if(o instanceof Actor){
                result = add((Actor)o) && result;
            }
        }
        return result;
    }

    public boolean removeAll(Collection<?> c) {
        boolean result = true;
        for(Object o : c){
            if(o instanceof Actor){
            	result = remove((Actor)o) && result;
            }
        }
        return result;
    }

    public boolean retainAll(Collection<?> c) {
        return actorList.retainAll(c);
    }

    public void clear() {
        actorList.clear();
    }

    public Actor get(String name){
        if(name != null){
            return actorMap.get(name);
        }else{
            return null;
        }
    }

    public Actor __getitem__(String name){
        return get(name);
    }
}
