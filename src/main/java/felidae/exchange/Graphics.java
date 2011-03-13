/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.exchange;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import felidae.Game;
import felidae.Utils;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jarl
 */
public class Graphics {
    public Game game;
    public Color clearColor;
    public boolean changeClearColor;
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

    public Graphics(Game game){
        this.game = game;
    }

    public void start(){
        GLProfile.initSingleton(true);
        glProfile = GLProfile.getDefault();
        glCapabilities = new GLCapabilities(glProfile);
        glCanvas = new GLCanvas((GLCapabilitiesImmutable)glCapabilities);
        glCanvas.addGLEventListener(new RenderListener(game));
        glCanvas.addKeyListener(new InputListener(game));
        glCanvas.setFocusable(true);

        animator = new FPSAnimator(glCanvas, 1000/16);
        animator.add(glCanvas);

        frame = new JFrame(game.title);
        frame.addWindowListener(new DisplayListener(game));
        frame.setResizable(false);
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
            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void display(){
        if(fullScreen){
            frame.setUndecorated(true);
            frame.setIgnoreRepaint(true);
            frame.setVisible(true);
            graphicsDevice.setFullScreenWindow(frame);
            graphicsDevice.setDisplayMode(displayMode);
        }else{
            frame.setUndecorated(false);
            frame.setIgnoreRepaint(false);
            Insets insets = frame.getInsets();
            frame.setSize(displayMode.getWidth() + insets.left + insets.right,
                          displayMode.getHeight() + insets.top + insets.bottom);
            frame.setVisible(true);
        }
        animator.start();
    }

    public void setGraphics(int width, int height, boolean fullScreen){
        DisplayMode[] displayModes = graphicsDevice.getDisplayModes();
        for(int a = 0;a < displayModes.length;a++){
            if(displayModes[a].getWidth() == width &&
               displayModes[a].getHeight() == height){
                displayMode = displayModes[a];
            }
        }
        this.fullScreen = fullScreen && graphicsDevice.isFullScreenSupported();
    }

    public void setClearColor(Color clearColor){
        this.clearColor = clearColor;
        changeClearColor = true;
    }
}
