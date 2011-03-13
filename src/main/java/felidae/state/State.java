/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.state;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import felidae.Game;
import felidae.Utils;
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

    public State(Game game){
        this.game = game;
    }

    public Actor loadActor(Map<String, String> code) throws InvocationTargetException{
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String mainClass = stackTrace[stackTrace.length-1].getClassName();
            Class<?> theClass = Class.forName(mainClass.substring(0, mainClass.indexOf(".")) + ".actors." + code.get("Role"));
            Class<?> []parameters = {Map.class};
            Constructor<?> constructor = theClass.getConstructor(parameters);
            Actor a = (Actor)constructor.newInstance(code);
            return a;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return null;
    }

    public void load(String level) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(level));
            this.level = level;

            Map<String, String> code = Utils.parseCode(document.getElementsByTagName("Level").item(0));

            pixelsPerMeter = Double.parseDouble(code.get("PixelsPerMeter"));
            Vec2 dimensions = new Vec2(Float.parseFloat(code.get("Width")), Float.parseFloat(code.get("Height")));
            String []colorComponents = code.get("BackgroundColor").split(",");
            game.graphics.setClearColor(new Color(Integer.parseInt(colorComponents[0]),
                                                  Integer.parseInt(colorComponents[1]),
                                                  Integer.parseInt(colorComponents[2])));

            world = new World(new AABB(new Vec2(0, 0), dimensions.add(dimensions)), new Vec2(), true);
            worldListener = new WorldListener();
            world.setContactListener(worldListener);

            cast = PersistentHashMap.EMPTY;
            Actor nextActor = null;
            NodeList elements = document.getElementsByTagName("Elements").item(0).getChildNodes();
            for(int a = 0;a < elements.getLength();a++){
                if(elements.item(a).getNodeType() == Node.ELEMENT_NODE){
                    code = Utils.parseCode(elements.item(a));
                    nextActor = loadActor(code);
                    if(cast.containsKey(nextActor.getName())){
                        cast = cast.assoc(nextActor.getName(),
                                          ((IPersistentSet)cast.valAt(nextActor.getName())).cons(nextActor));
                    }else{
                        cast = cast.assoc(nextActor.getName(),
                                          PersistentHashSet.create(nextActor));
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void start(){
        timer = new Timer(game.period, new StateListener(game));
        timer.start();
    }

    public void stop(){
        timer.stop();
    }
}
