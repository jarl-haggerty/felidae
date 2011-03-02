/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.actors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import felidae.GameState;

/**
 *
 * @author jarl
 */
public class ActorLoader {
    public Actor load(Map<String, String> code){
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String mainClass = stackTrace[stackTrace.length-1].getClassName();
            Class<?> theClass = Class.forName(mainClass.substring(0, mainClass.indexOf(".")) + ".actors." + code.get("Role"));
            Class<?> []parameters = {Map.class};
            Constructor<?> constructor = theClass.getConstructor(parameters);
            Actor a = (Actor)constructor.newInstance(code);
            return a;
        } catch (InstantiationException ex) {
            System.err.println(ex);
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        } catch (SecurityException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.err.println(ex);
             ex.printStackTrace();
        }
        return null;
    }

    public List<Actor> base(GameState currentState){
        return new ArrayList<Actor>();
    }
}
