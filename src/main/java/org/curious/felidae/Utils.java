/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.input.Mouse;
import felidae.graphics.Graphics;
import java.util.HashMap;
import org.jbox2d.common.Vec2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jarl
 */
public class Utils {
	public static final int Collision = -1;
	public static final int Seperation = -2;
	public static final int LoadLevel = -3;
	
    public static File resourceToFile(String resource) throws URISyntaxException{
        return new File(new URI(Game.class.getResource(resource).toString()));
    }

    public static Map<String, String> parseCode(String code)
    {
        Map<String, String> parsed = new TreeMap<String, String>();
        String []pieces = code.split("\n");
        String []keyValue;
        for(int a = 0;a < pieces.length;a++)
        {
            keyValue = pieces[a].split("=", -1);
            parsed.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return parsed;
    }

    public static String packageCode(Map<String, String> input) {
        String output = "";
        for(String key : input.keySet()){
            output += key + "=" + input.get(key) + "\n";
        }
        return output;
    }

    public static boolean validKey(Map<String, String> code, String key){
        if(code.containsKey(key) && !code.get(key).isEmpty()){
            return true;
        }else{
            return false;
        }
    }
    
    public static Vec2 getMouseInGame(){
    	Vec2 result = new Vec2(Mouse.getX(), Mouse.getY());
    	
    	result.x *= Graphics.getViewPort().getWidth()/Graphics.dimensions.x;
    	result.y *= Graphics.getViewPort().getHeight()/Graphics.dimensions.y;
        result = result.add(new Vec2(Graphics.getViewPort().x, Graphics.getViewPort().y));
    	
    	return result;
    }

    public static boolean xor(boolean x, boolean y) {
        return ( ( x || y ) && ! ( x && y ) );
    }

    public static boolean implies(boolean x, boolean y) {
        return !x || y;
    }

    public static Map<String, String> parseCode(Node root)
    {
        Map<String, String> parsed = new HashMap<String, String>();
        NodeList nodes = root.getChildNodes();
        for(int a = 0;a < nodes.getLength();a++){
            if(nodes.item(a).getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)nodes.item(a);
                if(element.getChildNodes().getLength() > 0){
                    parsed.put(element.getTagName(), element.getChildNodes().item(0).getTextContent());
                }else{
                    parsed.put(element.getTagName(), "");
                }
            }
        }
        return parsed;
    }

    public static Vec2 parseVec2(String input){
        String[] parts = input.split(",");
        return new Vec2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
    }
}
