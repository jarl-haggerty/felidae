/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLException;
import org.curious.felidae.Game;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author jarl
 */
public class Renderer {
    public GL2 gl;
    public Game game;

    public Renderer(GL2 gl, Game game){
        this.gl = gl;
        this.game = game;
    }

    public void setClearColor(Color clearColor){
        gl.glClearColor(clearColor.getRed()/255f,
                        clearColor.getGreen()/255f,
                        clearColor.getBlue()/255f,
                        clearColor.getAlpha()/255f);
        game.graphics.clearColor = clearColor;
    }

    public void setView(double x, double y, double width, double height) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(x, x+width, y, y+height, -1, 1);
        game.graphics.view = new Rectangle2D.Double(x, y, width, height);
    }
    
    public double getViewRatio(){
        return 1.0*game.graphics.displayMode.getWidth()/game.graphics.displayMode.getHeight();
    }

    public void drawBody(Body body) {
        Vec2 temp;
        for(Shape s = body.getShapeList();s != null;s = s.getNext()){
            if(s instanceof PolygonShape){
                PolygonShape s2 = (PolygonShape)s;
                gl.glBegin(GL2.GL_POLYGON);
                    for(int a = 0;a < s2.getVertexCount();a++){
                        temp = XForm.mul(body.getXForm(), s2.m_vertices[a]);
                        gl.glVertex2f(temp.x, temp.y);
                    }
                gl.glEnd();
            }else if(s instanceof CircleShape){
                CircleShape s2 = (CircleShape)s;
                temp = XForm.mul(body.getXForm(), s2.m_localPosition);
                gl.glBegin(gl.GL_POLYGON);
                    for(float a = 0;a < 2*Math.PI;a += 2*Math.PI/100){
                        gl.glVertex2f(temp.x + (float)Math.cos(a)*s2.m_radius, temp.y + (float)Math.sin(a)*s2.m_radius);
                    }
                gl.glEnd();
            }
        }
    }

    public void renderVBO(VBO vbo){
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.vertices);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);    
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.texels);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);

        gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vbo.length);
    }

    public Texture loadTexture(String name){
        if(game.graphics.textures.containsKey(name)){
            return game.graphics.textures.get(name);
        }else{
            try {
                Texture texture = TextureIO.newTexture(new File(name), true);
                game.graphics.textures.put(name, texture);
                return texture;
            } catch (Exception ex) {
                System.out.println(ex);
                return null;
            }
        }
    }

    public void setTexture(Texture texture){
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTarget());
    }

    public VBO createVBO(List<Vec2> vertexList, List<Vec2> texelList){
        FloatBuffer vertexBuffer = FloatBuffer.allocate(vertexList.size()*3);
        FloatBuffer texelBuffer = FloatBuffer.allocate(texelList.size()*2);
        int index = 0;
        for(Vec2 point : vertexList){
            vertexBuffer.put(point.x);
            vertexBuffer.put(point.y);
            vertexBuffer.put(0);
            index += 3;
        }
        index = 0;
        for(Vec2 point : texelList){
            texelBuffer.put(point.x);
            texelBuffer.put(point.y);
            index += 2;
        }

        int[] vbo = new int[2];
        gl.glGenBuffers(1, vbo, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity()*4, vertexBuffer, GL2.GL_STATIC_DRAW);
        gl.glGenBuffers(1, vbo, 1);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[1]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texelBuffer.capacity()*4, texelBuffer, GL2.GL_STATIC_DRAW);

        return new VBO(vbo[0], vbo[1], vertexList.size());
    }

    public void setColor(Color color) {
        gl.glColor4f(color.getRed()/255f,
                     color.getGreen()/255f,
                     color.getBlue()/255f,
                     color.getAlpha()/255f);
        game.graphics.color = color;
    }
}
