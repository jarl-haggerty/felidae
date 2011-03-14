/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.actors;

import com.jogamp.opengl.util.texture.Texture;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.curious.felidae.media.Input;
import org.curious.felidae.media.Renderer;
import org.curious.felidae.media.VBO;
import org.curious.felidae.state.Actor;
import org.curious.felidae.state.State;
import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

/**
 *
 * @author jarl
 */
public class Box implements Actor {
    public Body body = null;
    public float x, y, width, height;
    public String name;
    public VBO vbo = null;
    public Texture texture;

    public Box(Map<String, String> data){
        name = data.get("Name");
        x = Float.parseFloat(data.get("X"));
        y = Float.parseFloat(data.get("Y"));
        width = Float.parseFloat(data.get("Width"));
        height = Float.parseFloat(data.get("Height"));
    }

    public String getName() {
        return name;
    }

    public void render(Renderer renderer) {
        if(vbo == null){
            List<Vec2> vertices = new LinkedList<Vec2>();
            vertices.add(new Vec2(0, 0));
            vertices.add(new Vec2(width, 0));
            vertices.add(new Vec2(width, height));
            vertices.add(new Vec2(0, height));
            List<Vec2> texels = new LinkedList<Vec2>();
            texels.add(new Vec2(0, 0));
            texels.add(new Vec2(1, 0));
            texels.add(new Vec2(1, 1));
            texels.add(new Vec2(0, 1));
            vbo = renderer.createVBO(vertices, texels);
            texture = renderer.loadTexture("Test.png");
        }
        renderer.setColor(Color.white);
        renderer.drawBody(body);
    }

    public boolean update(State state) {
        if(body == null){
            BodyDef bodyDef = new BodyDef();
            bodyDef.position = new Vec2(x, y);
            body = state.world.createBody(bodyDef);
            PolygonDef polygonDef = new PolygonDef();
            polygonDef.density = 1/width/height;
            polygonDef.addVertex(new Vec2(0, 0));
            polygonDef.addVertex(new Vec2(width, 0));
            polygonDef.addVertex(new Vec2(width, height));
            polygonDef.addVertex(new Vec2(0, height));
            body.createShape(polygonDef);
            body.setMassFromShapes();
        }
        if(name.equals("One")){
            body.applyForce(new Vec2(0, -10), body.getWorldCenter());
        }else{
            body.applyImpulse(body.getLinearVelocity().mul(-body.getMass()), body.getWorldCenter());
            body.applyImpulse(new Vec2(0, -10), body.getWorldCenter());
        }
        System.out.println(body.getLinearVelocity());
        return true;
    }

    public void processInput(Input input) {
    }
}
