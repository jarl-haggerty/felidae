/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jarl
 */
public class Utils {
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
}
