/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.curious.felidae.Game;
import org.curious.felidae.Utils;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author jarl
 */
public class Graphics {
    public Game game;
    public Color clearColor;
    public GraphicsEnvironment graphicsEnvironment;
    public GraphicsDevice graphicsDevice;
    public DisplayMode displayMode;
    public boolean fullScreen;
    public String settingsFile;
    public GLProfile glProfile;
    public GLCapabilities glCapabilities;
    public GLCanvas glCanvas;
    public FPSAnimator animator;
    public JFrame frame;
    public Map<String, TextRenderer> fonts;
    public Map<String, Texture> textures;
    public Rectangle2D.Double view;
    public Color color;

    static{
        GLProfile.initSingleton(true);
    }
    public Graphics(Game game){
        this.game = game;
    }

    public void start(){
        textures = new HashMap<String, Texture>();

        glProfile = GLProfile.getDefault();
        glCapabilities = new GLCapabilities(glProfile);
        glCanvas = new GLCanvas(glCapabilities);
        glCanvas.addGLEventListener(new RenderListener(game));
        glCanvas.addKeyListener(new InputListener(game));
        glCanvas.setFocusable(true);
        animator = new FPSAnimator(glCanvas, 1000/game.period);
        animator.add(glCanvas);
        frame = new JFrame(game.title);
        frame.addWindowListener(new DisplayListener(game));
        frame.add(glCanvas);
        display();
    }

    public void stop(){
        animator.stop();
        frame.dispose();
    }

    public void load(String fileName){
        this.settingsFile = fileName;
        try {
            graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = db.parse(new File(settingsFile));

            Map<String, String> code = Utils.parseCode(document.getElementsByTagName("Graphics").item(0));
            setGraphics(Integer.parseInt(code.get("ScreenWidth")),
                        Integer.parseInt(code.get("ScreenHeight")),
                        Boolean.parseBoolean(code.get("FullScreen")));
        } catch (SAXException ex) {
            setGraphics(-1, -1, false);
        } catch (IOException ex) {
            setGraphics(-1, -1, false);
        } catch (ParserConfigurationException ex) {
            setGraphics(-1, -1, false);
        }
    }

    public void display(){
        animator.start();
        if(fullScreen){
            frame.setResizable(true);
            frame.setUndecorated(true);
            frame.setIgnoreRepaint(true);
            frame.setVisible(true);
            graphicsDevice.setFullScreenWindow(frame);
            graphicsDevice.setDisplayMode(displayMode);
        }else{
            frame.setResizable(false);
            frame.setUndecorated(false);
            frame.setIgnoreRepaint(false);
            Insets insets = frame.getInsets();
            frame.setSize(displayMode.getWidth() + insets.left + insets.right,
                          displayMode.getHeight() + insets.top + insets.bottom);
            frame.setVisible(true);
        }
        
    }

    public void setGraphics(int width, int height, boolean fullScreen){
        displayMode = null;
        DisplayMode[] displayModes = graphicsDevice.getDisplayModes();
        for(int a = 0;a < displayModes.length;a++){
            if(displayModes[a].getWidth() == width &&
               displayModes[a].getHeight() == height &&
               (displayMode == null || displayModes[a].getBitDepth() > displayMode.getBitDepth())){
                displayMode = displayModes[a];
            }
        }
        if(displayMode == null){
            int minDistance = Integer.MAX_VALUE;
            for(DisplayMode mode : displayModes){
                if(minDistance == Integer.MAX_VALUE && (displayMode == null || mode.getWidth() < displayMode.getWidth() &&
                                                                               mode.getHeight() < displayMode.getHeight())){
                    displayMode = mode;
                }else{
                    if(mode.getWidth() <= width && mode.getHeight() <= height){
                        if(width-mode.getWidth() + height-mode.getHeight() < minDistance){
                            minDistance = width-mode.getWidth() + height-mode.getHeight();
                            displayMode = mode;
                        }
                    }
                }
            }
        }
        this.fullScreen = fullScreen && graphicsDevice.isFullScreenSupported();
        {
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element root = document.createElement("Graphics");
                document.appendChild(root);

                Element property = document.createElement("ScreenWidth");
                property.appendChild(document.createTextNode(String.valueOf(displayMode.getWidth())));
                root.appendChild(property);

                property = document.createElement("ScreenHeight");
                property.appendChild(document.createTextNode(String.valueOf(displayMode.getHeight())));
                root.appendChild(property);

                property = document.createElement("FullScreen");
                property.appendChild(document.createTextNode(String.valueOf(this.fullScreen)));
                root.appendChild(property);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(document);
                transformer.transform(source, new StreamResult(new File(settingsFile)));
            } catch (TransformerException ex) {
                Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
