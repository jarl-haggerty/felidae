/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.state;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import org.curious.felidae.Game;
import org.curious.felidae.Utils;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import physics.WorldListener;

/**
 *
 * @author jarl
 */
public class State {
    public Game game;
    public World world;

    public String level;
    public double pixelsPerMeter;
    public WorldListener worldListener;
    public IPersistentMap cast;
    public Timer timer;
    public boolean reset;

    public State(Game game){
        this.game = game;
    }

    public Actor loadActor(Map<String, String> code) throws InvocationTargetException{
        try {
            Class<?> theClass = Class.forName(code.get("Role"));
            Class<?> []parameters = {Map.class};
            Constructor<?> constructor = theClass.getConstructor(parameters);
            Actor a = (Actor)constructor.newInstance(code);
            return a;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void load(String level) {
        reset = true;
        cast = PersistentHashMap.EMPTY;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(level));
            this.level = level;

            Map<String, String> code = Utils.parseCode(document.getElementsByTagName("Level").item(0));

            pixelsPerMeter = Double.parseDouble(code.get("PixelsPerMeter"));
            Vec2 dimensions = new Vec2(Float.parseFloat(code.get("Width")), Float.parseFloat(code.get("Height")));

            world = new World(new AABB(new Vec2(-10, -10), dimensions.add(dimensions)), new Vec2(0, 0), true);
            worldListener = new WorldListener();
            world.setContactListener(worldListener);

            IPersistentMap newCast = PersistentHashMap.EMPTY;
            Actor nextActor = null;
            NodeList elements = document.getElementsByTagName("Elements").item(0).getChildNodes();
            for(int a = 0;a < elements.getLength();a++){
                if(elements.item(a).getNodeType() == Node.ELEMENT_NODE){
                    code = Utils.parseCode(elements.item(a));
                    nextActor = loadActor(code);
                    if(newCast.containsKey(nextActor.getName())){
                        newCast = newCast.assoc(nextActor.getName(),
                                          ((IPersistentSet)cast.valAt(nextActor.getName())).cons(nextActor));
                    }else{
                        newCast = newCast.assoc(nextActor.getName(),
                                          PersistentHashSet.create(nextActor));
                    }
                }
            }
            cast = newCast;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start(){
        timer = new Timer(game.period, new StateListener(game));
        timer.start();
    }

    public void stop(){
        timer.stop();
    }

    public IPersistentSet getActor(String name) {
        return (IPersistentSet)cast.valAt(name);
    }

    public void addActor(Actor actor) {
        if(cast.containsKey(actor.getName())){
            cast = cast.assoc(actor.getName(),
                              ((IPersistentSet)cast.valAt(actor.getName())).cons(actor));
        }else{
            cast = cast.assoc(actor.getName(),
                              PersistentHashSet.create(actor));
        }
    }
}
