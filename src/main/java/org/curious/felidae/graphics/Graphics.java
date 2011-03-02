/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import felidae.Game;
import felidae.Utils;
import java.lang.reflect.Field;

/**
 *
 * @author Jarl
 */
public class Graphics {
    private static ConcurrentMap<String, Texture> loadedTextures;
    private static TextureLoader textureLoader;
    private static DisplayMode displayMode;
    private static boolean fullScreen;

    public static Vec2 dimensions;
    public static Rectangle.Float viewPort;
    public static int currentProgram;
    public static LWJGLRenderer guiRenderer;
    public static DesktopArea desktop;
    public static GUI gui;
    public static ThemeManager theme;
    public static Map<String, TrueTypeFont> fonts;
    private static TrueTypeFont currentFont;
    public static boolean initialized;
    static{
        initialized = false;
    }

    public static boolean initialize(){
//    	try {
//            Class clazz = ClassLoader.class;
//            Field field = clazz.getDeclaredField("sys_paths");
//            boolean accessible = field.isAccessible();
//            if (!accessible){
//                field.setAccessible(true);
//            }
//            Object original = field.get(clazz);
//            // Reset it to null so that whenever "System.loadLibrary" is called, it will be reconstructed with the changed value.
//            field.set(clazz, null);
//            System.setProperty("java.library.path", System.getProperty("user.dir") + File.separator + "NativeCode");
//
//            if(System.getProperty("os.name").toLowerCase().contains("windows")){
//                if(System.getProperty("os.arch").contains("32")){
//                    System.loadLibrary("lwjgl");
//                    System.loadLibrary("jinput-raw");
//                    System.loadLibrary("jinput-dx8");
//                }else if(System.getProperty("os.arch").contains("64")){
//                    System.loadLibrary("lwjgl64");
//                    System.loadLibrary("jinput-raw_64");
//                    System.loadLibrary("jinput-dx8_64");
//                }
//            }else if(System.getProperty("os.name").toLowerCase().contains("mac")){
//                System.loadLibrary("lwjgl");
//                System.loadLibrary("jinput-osx");
//            }else if(System.getProperty("os.name").toLowerCase().contains("nix")){
//                if(System.getProperty("os.arch").contains("32")){
//                    System.loadLibrary("lwjgl");
//                    System.loadLibrary("jinput-linux");
//                }else if(System.getProperty("os.arch").contains("64")){
//                    System.loadLibrary("lwjgl64");
//                    System.loadLibrary("jinput-linux64");
//                }
//            }
//
//            //Revert back the changes.
//            field.set(clazz, original);
//            field.setAccessible(accessible);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SecurityException ex) {
//            Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
//        }
    	
        loadedTextures = new ConcurrentHashMap<String, Texture>();
        textureLoader = new TextureLoader();
        //new Thread(textureLoader).start();
        fullScreen = true;

        BufferedReader reader;
        File settings = new File("Settings" + File.separator + "Graphics.set");
        try {
            reader = new BufferedReader(new FileReader(settings));

            String line, rawCode = "";
            while((line = reader.readLine()) != null){
                rawCode += line + "\n";
            }

            Map<String, String> code = Utils.parseCode(rawCode);
            int targetWidth = Integer.parseInt(code.get("ScreenWidth"));
            int targetHeight = Integer.parseInt(code.get("ScreenHeight"));
            int targetBPP = Integer.parseInt(code.get("BPP"));
            fullScreen = Boolean.parseBoolean(code.get("FullScreen"));

            displayMode = findDisplayMode(targetWidth, targetHeight, targetBPP, fullScreen);
            if(displayMode == null){
                displayMode = findLowestDisplayMode();
                if(displayMode == null){
                    return false;
                }
            }
            setDisplayMode(displayMode);

        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            displayMode = findLowestDisplayMode();
            if(displayMode == null){
                return false;
            }
            setDisplayMode(displayMode);
        } catch(IOException ex){
            System.err.println(ex);
            displayMode = findLowestDisplayMode();
            if(displayMode == null){
                return false;
            }
            setDisplayMode(displayMode);
        }
        try {
            Display.create(new PixelFormat(0, 8, 1));
            setViewPort(new Rectangle.Float(0, 0, 1, 1));
            GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
        } catch (LWJGLException ex) {
            System.err.println(ex);
            return false;
        }

        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		GL11.glClearStencil(1);
        
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);

        GL11.glLineWidth(1);
        
        fonts = new HashMap<String, TrueTypeFont>();
        currentFont = new TrueTypeFont(new Font(Font.SERIF, Font.PLAIN, 20), true);
        fonts.put("Default", currentFont);
        try {
            guiRenderer = new LWJGLRenderer();
        } catch (LWJGLException ex) {
            System.err.println(ex);
            return false;
        }
        desktop = new DesktopArea();
        gui = new GUI(desktop, guiRenderer);
        try {
            theme = ThemeManager.createThemeManager(new File("GUI" + File.separator + "chat.xml").toURI().toURL(), guiRenderer);
        } catch (MalformedURLException ex) {
            System.err.println(ex);
            return false;
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
        
        gui.applyTheme(theme);

        currentProgram = 0;

        initialized = true;
        return true;
    }
    
    public static boolean loadFont(String fileName){
        return Graphics.loadFont(new File(fileName));
    }
    
    public static boolean loadFont(File file){
        try {
            FileInputStream is = new FileInputStream(file);
            fonts.put(file.getName().substring(0, file.getName().indexOf(".ttf")), new TrueTypeFont(Font.createFont(Font.TRUETYPE_FONT, is), true));
        } catch (FontFormatException ex) {
            System.err.println(ex);
            return false;
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            return false;
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    public static void setCurrentFont(String font){
        currentFont = fonts.get(font);
    }

    public static void drawString(Object text, float x, float y){
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        currentFont.drawString(x, y, text == null ? "null" : text.toString(), viewPort.width/dimensions.x, viewPort.height/dimensions.y);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static void drawStringOnView(Object text, float x, float y){
        drawString(text, x + viewPort.x, y + viewPort.y);
    }
    
    public static Rectangle.Float getStringVolume(Object text){
        return currentFont.getStringVolume(text == null ? "null" : text.toString(), viewPort.width/dimensions.x, viewPort.height/dimensions.y);
    }

    public static void drawStringInCenter(Object text){
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        currentFont.drawString(viewPort.x + viewPort.width/2, viewPort.y + viewPort.height/2, text == null ? "null" : text.toString(), viewPort.width/dimensions.x, viewPort.height/dimensions.y);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static DisplayMode findLowestDisplayMode(){
        try{
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            DisplayMode output = modes[0];
            int minWidth = modes[0].getWidth();
            int minHeight = modes[0].getHeight();
            for (int i = 1; i < modes.length; i++) {
                if (modes[i].getWidth() <= minWidth && modes[i].getHeight() <= minHeight && modes[i].isFullscreenCapable()) {
                    output = modes[i];
                    minWidth = modes[i].getWidth();
                    minHeight = modes[i].getHeight();
                }
            }

            return output;
        } catch (LWJGLException ex){
            System.err.println(ex);
            return null;
        }
    }

    public static DisplayMode findDisplayMode(int width, int height, int bpp, boolean fullScreen){
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            DisplayMode output = null;
            for (int i = 1; i < modes.length; i++) {
                if (modes[i].getWidth() == width && modes[i].getHeight() == height && modes[i].getBitsPerPixel() == bpp && Utils.implies(fullScreen, modes[i].isFullscreenCapable())) {
                    output = modes[i];
                }
            }
            return output;
        } catch (LWJGLException ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static void clear(){
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
    }

    public static void clearMemory(){
        IntBuffer temp = BufferUtils.createIntBuffer(loadedTextures.size());
        for(Texture t : loadedTextures.values()){
            temp.put(t.id);
        }
        GL11.glDeleteTextures(temp);
        loadedTextures.clear();

        //ARBShaderObjects.glDeleteObjectARB(currentProgram);
    }

    private static class TextureLoader implements Runnable {
        public BlockingQueue<Texture> loadingTextures;
        public BlockingQueue<Texture> readyTextures;

        public void run() {
            while(!loadingTextures.isEmpty()){
                try {
                    Texture texture = loadingTextures.take();
                    BufferedImage image = ImageIO.read(new File(texture.fileName));
                    texture.width = image.getWidth();
                    texture.height = image.getHeight();
                    byte[] rawData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                    texture.data = BufferUtils.createByteBuffer(4 * texture.width * texture.height);
                    int offset = 0;
                    switch (image.getType()) {
                        case BufferedImage.TYPE_4BYTE_ABGR:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset + 3, 1);
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset, 1);
                                offset += 4;
                            }
                            break;
                        case BufferedImage.TYPE_INT_ARGB:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 3, 1);
                                texture.data.put(rawData, offset, 1);
                                offset += 4;
                            }
                            break;
                        case BufferedImage.TYPE_3BYTE_BGR:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset, 1);
                                texture.data.put((byte) 255);
                                offset += 3;
                            }
                            break;
                        case BufferedImage.TYPE_INT_BGR:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset, 1);
                                texture.data.put((byte) 255);
                                offset += 4;
                            }
                            break;
                        case BufferedImage.TYPE_INT_RGB:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset, 1);
                                texture.data.put((byte) 255);
                                offset += 4;
                            }
                            break;
                        case BufferedImage.TYPE_CUSTOM:
                            while (texture.data.hasRemaining()) {
                                texture.data.put(rawData, offset, 1);
                                texture.data.put(rawData, offset + 1, 1);
                                texture.data.put(rawData, offset + 2, 1);
                                texture.data.put(rawData, offset + 3, 1);
                                offset += 4;
                            }
                            break;
                    }
                    texture.data.rewind();
                    readyTextures.add(texture);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

//    public static synchronized Texture loadTexture(String textureName) throws IOException{
//        if (loadedTextures.containsKey(textureName)) {
//            return loadedTextures.get(textureName);
//        }
//        IntBuffer scratch = BufferUtils.createIntBuffer(1);
//        GL11.glGenTextures(scratch);
//        Texture texture = new Texture(scratch.get(0), textureName);
//        textureLoader.loadingTextures.add(texture);
//
//        return texture;
//    }

    public static Texture loadTexture(String textureName){
    	return loadTexture(textureName, new Effects());
    }
    
    public static Texture loadTexture(String textureName, Effects effects){
        if (loadedTextures.containsKey(textureName)) {
            return loadedTextures.get(textureName);
        }

        Texture newTexture;
        try {
            newTexture = loadTexture(ImageIO.read(new File(textureName)), effects);
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }

        loadedTextures.put(textureName, newTexture);

        return newTexture;
    }

    public static Texture loadTexture(BufferedImage image, Effects effects){
        int width = image.getWidth();
        int height = image.getHeight();
        byte []rawData =  ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        ByteBuffer data = BufferUtils.createByteBuffer(4*width*height);
        int offset = 0;
        switch(image.getType()){
            case BufferedImage.TYPE_4BYTE_ABGR:
                while(data.hasRemaining()){
                    data.put(rawData, offset+3, 1);
                    data.put(rawData, offset+2, 1);
                    data.put(rawData, offset+1, 1);
                    data.put(rawData, offset, 1);
                    offset+=4;
                }
                break;
            case BufferedImage.TYPE_INT_ARGB:
                while(data.hasRemaining()){
                    data.put(rawData, offset+1, 1);
                    data.put(rawData, offset+2, 1);
                    data.put(rawData, offset+3, 1);
                    data.put(rawData, offset, 1);
                    offset+=4;
                }
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                while(data.hasRemaining()){
                    data.put(rawData, offset+2, 1);
                    data.put(rawData, offset+1, 1);
                    data.put(rawData, offset, 1);
                    data.put((byte)255);
                    offset+=3;
                }
                break;
            case BufferedImage.TYPE_INT_BGR:
                while(data.hasRemaining()){
                    data.put(rawData, offset+2, 1);
                    data.put(rawData, offset+1, 1);
                    data.put(rawData, offset, 1);
                    data.put((byte)255);
                    offset+=4;
                }
                break;
            case BufferedImage.TYPE_INT_RGB:
                while(data.hasRemaining()){
                    data.put(rawData, offset+2, 1);
                    data.put(rawData, offset+1, 1);
                    data.put(rawData, offset, 1);
                    data.put((byte)255);
                    offset+=4;
                }
                break;
            case BufferedImage.TYPE_CUSTOM:
                if(rawData.length == 4*width*height){
                    while(data.hasRemaining()){
                        data.put(rawData, offset, 1);
                        data.put(rawData, offset+1, 1);
                        data.put(rawData, offset+2, 1);
                        data.put(rawData, offset+3, 1);
                        offset+=4;
                    }
                }else if(rawData.length == 3*width*height){
                    while(data.hasRemaining()){
                        data.put(rawData, offset, 1);
                        data.put(rawData, offset+1, 1);
                        data.put(rawData, offset+2, 1);
                        data.put((byte)255);
                        offset+=3;
                    }
                }else{
                    System.err.println("Failed to load image");
                }
                break;
        }

        data.rewind();
        IntBuffer scratch = BufferUtils.createIntBuffer(1);

        GL11.glGenTextures(scratch);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, scratch.get(0));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        switch(effects.wrapping){
        case Clamp:
	        GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP );
	        GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP );
	        break;
        case Repeat:
        	GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT );
	        GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT );
	        break;
        }
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);

        return new Texture(scratch.get(0), width, height);
    }

    public static void setClearColor(Vec3 color) {
        GL11.glClearColor(color.x, color.y, color.z, 1.0f);
    }

    public static void setClearColor(float red, float green, float blue) {
        setClearColor(red, green, blue, 1.0f);
    }

    public static void setClearColor(float red, float green, float blue, float alpha) {
        GL11.glClearColor(red, green, blue, alpha);
    }

    public static void setClearColor(Color color) {
        GL11.glClearColor(color.getAlpha()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
    }

    public static void update(){
//        while(!textureLoader.readyTextures.isEmpty()){
//            try {
//                Texture texture = textureLoader.readyTextures.take();
//                texture.data.rewind();
//                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
//                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, texture.width, texture.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.data);
//                texture.loaded = true;
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        gui.update();
        Display.update();
    }

    public static boolean texturesLoaded(){
        return textureLoader.loadingTextures.isEmpty() && textureLoader.readyTextures.isEmpty();
    }

    public static void destroy() {
        clearMemory();
        if(gui != null){
            gui.destroy();
        }
        if(theme != null){
            theme.destroy();
        }
        if(Display.isCreated()){
            Display.destroy();
        }
    }

    public static boolean setDisplayMode(int width, int height, int bpp, boolean fullScreen){
        Graphics.fullScreen = fullScreen;
        DisplayMode mode = findDisplayMode(width, height, bpp, fullScreen);
        if(mode == null){
            System.err.println("Could not find find LWJGL display matching " + width + "x" + height + "bpp: " + bpp + "fullscreen: " + fullScreen);
            return false;
        }else{
            return setDisplayMode(mode);
        }
    }

    public static boolean setDisplayMode(DisplayMode displayMode){
        try {
            Graphics.displayMode = displayMode;
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Settings" + File.separator + "Graphics.set")));
            Map<String, String> toFile = new TreeMap<String, String>();
            toFile.put("ScreenWidth", String.valueOf(displayMode.getWidth()));
            toFile.put("ScreenHeight", String.valueOf(displayMode.getHeight()));
            toFile.put("BPP", String.valueOf(displayMode.getBitsPerPixel()));
            toFile.put("FullScreen", String.valueOf(fullScreen));
            writer.write(Utils.packageCode(toFile));
            writer.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
        try {
            Display.setDisplayMode(displayMode);
            Display.setTitle(Game.title);
            Display.setFullscreen(fullScreen);
            if (Display.isCreated()) {
                setViewPort(new Rectangle.Float(0, 0, 1, 1));
                GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
            }
            dimensions = new Vec2(displayMode.getWidth(), displayMode.getHeight());
        } catch (LWJGLException ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    public static void setViewPort(Rectangle.Float viewPort){
        Graphics.viewPort = viewPort;
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(viewPort.x, viewPort.x + viewPort.width, viewPort.y, viewPort.y + viewPort.height, -1.0, 1.0);        
    }

    public static Rectangle.Float getViewPort(){
        return viewPort;
    }

    public static float getAspectRatio(){
        return (float)dimensions.x/dimensions.y;
    }

    public static int getWidth(){
        return displayMode.getWidth();
    }

    public static int getHeight(){
        return displayMode.getHeight();
    }

    public static Vec2 getDims(){
        return new Vec2(displayMode.getWidth(), displayMode.getHeight());
    }
    
    public static void drawTexture(Texture texture, Rectangle.Float destination){
    	drawTexture(texture, destination, new Rectangle.Float(0, 0, 1, 1), new Effects());
    }

    public static void drawTexture(Texture texture, Rectangle.Float destination, Effects effects){
    	drawTexture(texture, destination, new Rectangle.Float(0, 0, 1, 1), effects);
    }
    
    public static void drawTexture(Texture texture, Rectangle.Float destination, Rectangle.Float source){
    	drawTexture(texture, destination, source, new Effects());
    }

    public static void drawTexture(Texture texture, Rectangle.Float destination, Rectangle.Float source, Effects effects) {
        float left = source.x;
        float right = source.x + source.width;
        float top = source.y + source.height;
        float bottom = source.y;
        //bottom += top*.05;

        if(effects.doTile){
            right = source.x + source.width*destination.width/effects.tile.x;
            top =  source.y + source.height*destination.height/effects.tile.y;
        }
        if(effects.isFlipHorizontal()){
            float temp = left;
            left = right;
            right = temp;
        }
        

        GL11.glColor4f(effects.color.getRed(), effects.color.getGreen(), effects.color.getBlue(), effects.color.getAlpha());
        //GL11.glColor3f(1, 1, 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id);

        Vec2 temp;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(left, top);
            temp = XForm.mul(effects.xform, new Vec2(destination.x, destination.y));
            GL11.glVertex2f(temp.x, temp.y);
            GL11.glTexCoord2f(right, top);
            temp = XForm.mul(effects.xform, new Vec2(destination.x + destination.width, destination.y));
            GL11.glVertex2f(temp.x, temp.y);
            GL11.glTexCoord2f(right, bottom);
            temp = XForm.mul(effects.xform, new Vec2(destination.x + destination.width, destination.y + destination.height));
            GL11.glVertex2f(temp.x, temp.y);
            GL11.glTexCoord2f(left, bottom);
            temp = XForm.mul(effects.xform, new Vec2(destination.x, destination.y + destination.height));
            GL11.glVertex2f(temp.x, temp.y);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static float getDisplayPeriod(){
        return 1f/Display.getDisplayMode().getFrequency();
    }

    public static Shader loadProgram(String name){
        return loadProgram(name + ".vert", name + ".frag");
    }

    public static Shader loadProgram(String vertexName, String fragmentName){
        ByteBuffer fragmentCode, vertexCode;

        try
        {
            DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(vertexName))));
            vertexCode = ByteBuffer.allocate(input.available());
            input.readFully(vertexCode.array());
            input.close();

            input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fragmentName))));
            fragmentCode = ByteBuffer.allocate(input.available());
            input.readFully(fragmentCode.array());
            input.close();

            int vertexShader = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
            int fragmentShader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

            ARBShaderObjects.glShaderSourceARB(vertexShader, vertexCode);
            ARBShaderObjects.glCompileShaderARB(vertexShader);
            ARBShaderObjects.glShaderSourceARB(fragmentShader, fragmentCode);
            ARBShaderObjects.glCompileShaderARB(fragmentShader);

            int programObject  = ARBShaderObjects.glCreateProgramObjectARB();
            ARBShaderObjects.glAttachObjectARB(programObject, vertexShader);
            ARBShaderObjects.glAttachObjectARB(programObject, fragmentShader);
            ARBShaderObjects.glLinkProgramARB(programObject);
            ARBShaderObjects.glValidateProgramARB(programObject);

            return new Shader(programObject);
        }
        catch (Exception e)
        {
                System.out.println(e.getMessage());
        }

        return null;
    }

    public static void useShader(Shader shader){
        ARBShaderObjects.glUseProgramObjectARB(shader.program);
        currentProgram = shader.program;
    }

    public static void drawBody(Body body) {
        Vec2 temp;
        for(Shape s = body.getShapeList();s != null;s = s.getNext()){
            if(s instanceof PolygonShape){
                PolygonShape s2 = (PolygonShape)s;
                GL11.glBegin(GL11.GL_LINE_STRIP);
                    for(int a = 0;a < s2.getVertexCount();a++){
                        temp = XForm.mul(body.getXForm(), s2.m_vertices[a]);
                        GL11.glVertex2f(temp.x, temp.y);
                    }
                    temp = XForm.mul(body.getXForm(), s2.m_vertices[0]);
                        GL11.glVertex2f(temp.x, temp.y);
                GL11.glEnd();
            }else if(s instanceof CircleShape){
                CircleShape s2 = (CircleShape)s;
                temp = XForm.mul(body.getXForm(), s2.m_localPosition);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                    for(float a = 0;a < 2*Math.PI;a += 2*Math.PI/100){
                        GL11.glVertex2f(temp.x + (float)Math.cos(a)*s2.m_radius, temp.y + (float)Math.sin(a)*s2.m_radius);
                    }
                GL11.glEnd();
            }
        }
    }

    public static void setColor(Color color) {
        GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
    }

    public static void drawLine(Vec2 from, Vec2 to) {
        GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(from.x, from.y);
            GL11.glVertex2f(to.x, to.y);
        GL11.glEnd();
    }

    public static void drawLines(List<Vec2> lines) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
            for(Vec2 point : lines){
                GL11.glVertex2f(point.x, point.y);
            }
        GL11.glEnd();
    }

	public static void drawFilledRectangle(Rectangle.Float toDraw) {
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(toDraw.x, toDraw.y);
			GL11.glVertex2f(toDraw.x + toDraw.width, toDraw.y);
			GL11.glVertex2f(toDraw.x + toDraw.width, toDraw.y + toDraw.height);
			GL11.glVertex2f(toDraw.x, toDraw.y + toDraw.height);
		GL11.glEnd();
	}
	
	public static void clearStencilBuffer() {
		GL11.glClearStencil(0);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}
	
	public static void fillStencilBuffer() {
		GL11.glClearStencil(1);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}

	public static void fillStencilBuffer(Float toDraw) {
		GL11.glColorMask(false, false, false, false);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);	
		
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(toDraw.x, toDraw.y);
			GL11.glVertex2f(toDraw.x + toDraw.width, toDraw.y);
			GL11.glVertex2f(toDraw.x + toDraw.width, toDraw.y + toDraw.height);
			GL11.glVertex2f(toDraw.x, toDraw.y + toDraw.height);
		GL11.glEnd();
		
		GL11.glColorMask(true, true, true, true);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
	}
}
