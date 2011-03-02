/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.scripting;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.python.core.PyBoolean;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import felidae.Game;
import felidae.audio.Audio;
import felidae.graphics.Graphics;
import felidae.logging.FelidaeStream;
import felidae.logging.Logger;


/**
 *
 * @author Jarl
 */
public class Scripting {
    private static PythonInterpreter interp;
    private static Console console;
    private static boolean lastTilde, secondLastTilde;
    private static int nextFunction, nextInt;
    public static boolean initialized;
    static{
        initialized = false;
    }

    public static boolean initialize(){
        console = new Console();
        System.out.println("Felidae 1.0");

        Graphics.desktop.add(getConsole());
        getConsole().setVisible(false);
        getConsole().setPosition(0, 0);
        getConsole().setSize(Graphics.desktop.getWidth()/2, Graphics.desktop.getHeight()/2);
        lastTilde = false;
        secondLastTilde = false;
        nextFunction = 0;
        nextInt = 0;

        interp = new PythonInterpreter();
        interp.setOut(Logger.getOut());
        interp.setErr(Logger.getErr());
        interp.set("Game", new Game());
        interp.set("Graphics", new Graphics());
        interp.set("Sound", new Audio());
        interp.set("true", new PyBoolean(true));
        interp.set("false", new PyBoolean(false));
        initialized = true;
        ((FelidaeStream)System.out).emptyBuffer();
        ((FelidaeStream)System.err).emptyBuffer();
        return true;
    }

    public static void update(){
        if(!lastTilde && Keyboard.isKeyDown(Keyboard.KEY_GRAVE)){
            getConsole().setVisible(!console.isVisible());
        }else if(!secondLastTilde && lastTilde){
            getConsole().requestFocus();
        }
        secondLastTilde = lastTilde;
        lastTilde = Keyboard.isKeyDown(Keyboard.KEY_GRAVE);
    }

    public static void runString(String toRun){
        if(toRun.length() >= 6 && toRun.substring(0, 6).equals("import")){
            System.err.println("No importing allowed!");
        }
        try{
            interp.exec(toRun);
        }catch(Exception e){
            System.err.println(e);
        }
    }

    public static Map<String, PyFunction> compile(String script){
        if(script.matches(".+\\.py$")){
            return compile(new File(script));
        }else{
            String functionName = "";
            int temp = nextFunction++;
            while(temp != 0){
                functionName += (char)((temp % 27) + 97);
                temp -= temp % 27;
                temp /= 27;
            }
            String code = "def " + functionName + "()\n\t" + script;
            ((FelidaeStream)System.out).println("Compiling:\n" + code, Color.blue);
            interp.exec(code);
            Map<String, PyFunction> result = new HashMap<String, PyFunction>();
            result.put("", (PyFunction)interp.get(functionName));
            return result;
        }
    }

    public static Map<String, PyFunction> compile(File file){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            Map<String, PyFunction> result = new HashMap<String, PyFunction>();
            String contents = "";
            String line, functionName = getUniqueString(), originalName = "";
            while ((line = reader.readLine()) != null) {
                if(line.matches("^def\\s+\\S+\\([^\\)]*\\):")){
                    if(!originalName.isEmpty()){
                        ((FelidaeStream)System.out).println("Compiling:\n" + contents, Color.blue);
                        interp.exec(contents);
                        result.put(originalName, (PyFunction)interp.get(functionName));
                        contents = "";
                        functionName = getUniqueString();
                    }
                    originalName = line.split("\\s+")[1];
                    originalName = originalName.substring(0, originalName.indexOf("("));
                    contents += "def " + functionName + line.substring(line.indexOf("(")) + "\n";
                }else{
                    contents += line + "\n";
                }
            }
            ((FelidaeStream)System.out).println("Compiling:\n" + contents, Color.blue);
            interp.exec(contents);
            result.put(originalName, (PyFunction)interp.get(functionName));
            return result;
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static String getUniqueString(){
        String functionName = "";
        int temp = nextFunction++;
        while(true){
            functionName += (char)((temp % 27) + 97);
            temp -= temp % 27;
            temp /= 27;
            if(temp == 0){
                break;
            }
        }
        return functionName;
    }

    /**
     * @return the console
     */
    public static Console getConsole() {
        return console;
    }

    public static void exec(File file) {
        try {
            interp.execfile(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
             System.err.println(ex);
        }
    }

    public static int getUniqueInt(String name) {
        interp.exec(name + " = " + nextInt++);
        return interp.get(name).asInt();
    }

    public static void set(String name, Object item) {
        interp.set(name, item);
    }

    public static void destroy() {
        if(interp != null){
            interp.cleanup();
        }
    }
}
