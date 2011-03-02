/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import felidae.actors.Actor;
import felidae.actors.ActorLoader;
import felidae.actors.Cast;
import felidae.graphics.Graphics;
import felidae.physics.WorldFilter;
import felidae.physics.WorldListener;
import felidae.scripting.Scripting;

/**
 *
 * @author Jarl
 */
public class GameState {
    public List<Actor> toAdd;
    public Set<Actor> updateHogs, updateHogsToAdd, updateHogsToRemove, inputHogs, inputHogsToAdd, inputHogsToRemove;
    public Cast actors;
    public World simulation;
    public WorldListener simulationListener;
    public WorldFilter simulationFilter;
    public Vec2 position, dimensions;
    public static int MaxUpdates = 10;
    public String name;
    private float pixelsPerMeter;
    private Color clearColor;
    public boolean created;
    
    public GameState(String fileName, ActorLoader loader){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            Document document = db.parse(new File("Levels" + File.separator + fileName));
            name = fileName;

            Map<String, String> code;

            code = Utils.parseCode(document.getElementsByTagName("Level").item(0));

            pixelsPerMeter = Float.parseFloat(code.get("PixelsPerMeter"));
            position = new Vec2(0, 0);
            dimensions = new Vec2(Float.parseFloat(code.get("Width")), Float.parseFloat(code.get("Height")));
            String []colorComponents = code.get("BackgroundColor").split(",");
            this.clearColor = new Color(Integer.parseInt(colorComponents[0]),
                                        Integer.parseInt(colorComponents[1]),
                                        Integer.parseInt(colorComponents[2]));
            Graphics.setClearColor(clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue());

            simulation = new World(new AABB(new Vec2(-1000, -1000), dimensions.add(new Vec2(1000, 1000))), new Vec2(), true);
            simulationListener = new WorldListener();
            simulation.setContactListener(simulationListener);
            simulationFilter = new WorldFilter();
            simulation.setContactFilter(simulationFilter);
            
            updateHogs = new HashSet<Actor>();
            inputHogs = new HashSet<Actor>();
            updateHogsToAdd = new HashSet<Actor>();
            inputHogsToAdd = new HashSet<Actor>();
            updateHogsToRemove = new HashSet<Actor>();
            inputHogsToRemove = new HashSet<Actor>();
            actors = new Cast();
            Actor nextActor = null;
            NodeList nodes = document.getElementsByTagName("Element");
            for(int a = 0;a < nodes.getLength();a++){
                if(nodes.item(a).getNodeType() == Node.ELEMENT_NODE){
                    code = Utils.parseCode(nodes.item(a));
                    nextActor = loader.load(code);
                    if(nextActor != null){
                        actors.add(nextActor);
                    }
                }
            }
            Game.actors = actors;
        } catch (IOException ex) {
            System.err.println(ex);
            created = false;
        } catch (SAXException ex) {
            System.err.println(ex);
            created = false;
        } catch (ParserConfigurationException ex) {
            System.err.println(ex);
            created = false;
        }

        actors.addAll(loader.base(this));
        for(Actor actor : actors){
            actor.body = simulation.createBody(actor.bodyDef);
        }

        toAdd = new ArrayList<Actor>();
    }

    public void addActor(Actor actor){
        toAdd.add(actor);
    }
    
    public void addInputHog(Actor actor){
    	inputHogsToAdd.add(actor);
    }
    
    public void addUpdateHog(Actor actor){
    	updateHogsToAdd.add(actor);
    }
    
    public void removeInputHog(Actor actor){
    	inputHogsToRemove.add(actor);
    }
    
    public void removeUpdateHog(Actor actor){
    	updateHogsToRemove.add(actor);
    }

    public void initialize(){
        for(Actor actor : actors){
            actor.initialize();
        }
        File starter = new File("Levels" + File.separator + name.substring(0, name.indexOf(".")) + ".py");
        if(starter.isFile()){
            Scripting.exec(starter);
        }
    }

    public void update(){
        for(Actor actor : toAdd){
            actor.body = simulation.createBody(actor.bodyDef);
            actor.initialize();
        }
        actors.addAll(toAdd);
        updateHogs.addAll(updateHogsToAdd);
        inputHogs.addAll(inputHogsToAdd);
        updateHogs.removeAll(updateHogsToRemove);
        inputHogs.removeAll(inputHogsToRemove);
        toAdd.clear();
        updateHogsToAdd.clear();
        inputHogsToAdd.clear();
        updateHogsToRemove.clear();
        inputHogsToRemove.clear();

        boolean complete, actorComplete;
        List<Actor> todo = new ArrayList<Actor>(actors), next = new ArrayList<Actor>();
        int a = 0;

        for(Actor actor : actors){
            if(!Scripting.getConsole().isVisible()){
            	if(inputHogs.isEmpty() || inputHogs.contains(actor)){
            		actor.processInput();
            	}
            }
        }

        while(true){
            complete = true;
            for(Actor actor : todo){
            	if(updateHogs.isEmpty() || updateHogs.contains(actor)){
            		actorComplete = actor.update(a);
            	}else{
            		actorComplete = false;
            	}
                if(!actorComplete){
                    next.add(actor);
                }
                complete &= actorComplete;
            }
            if(complete){
                break;
            }
            todo = new ArrayList<Actor>(next);
            next.clear();
            a++;
        }

        List<Actor> toRemove = new ArrayList<Actor>();
        for(Actor actor : actors){
            if(actor.dead){
                actor.destroy();
                toRemove.add(actor);
            }
        }
        for(Actor actor : toRemove){
            actors.remove(actor);
        }

        simulation.step(Game.delta, 6);
    }

    public void draw(){
        for(Actor actor : actors){
            actor.render();
        }
    }

    public void vocalize(){
        for(Actor actor : actors){
            actor.vocalize();
        }
    }

    public Actor getActor(String who) {
        return actors.get(who);
    }

    public void setOffset(Vec2 offset) {
        this.position = new Vec2(offset);
    }

    /**
     * @return the pixelsPerMeter
     */
    public float getPixelsPerMeter() {
        return pixelsPerMeter;
    }

    public void reset(){
        Game.nextState = name;
    }
}
