/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.scripting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.lwjgl.input.Keyboard;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.model.AutoCompletionDataSource;
import de.matthiasmann.twl.model.AutoCompletionResult;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import felidae.graphics.Graphics;

/**
 *
 * @author Jarl
 */
public class Console extends ResizableFrame {
    private TextArea output;
    private EditField input;
    private ScrollPane scrollPane;
    private String outputString;
    private LinkedList<String> history;
    private ListIterator<String> position;
    private Timer scroller;
    private int currentMax;
    private Color color;

    public Console(){
        setTitle("Console");

        output = new TextArea(new HTMLTextAreaModel());
        input = new EditField();
        input.setAutoCompletion(new ConsoleAutoCompletionDataSource());
        outputString = "";
        history = new LinkedList<String>();
        position = history.listIterator();
        color = Color.white;

        input.addCallback(new EditField.Callback() {
            public void callback(int key) {
                if(key == Keyboard.KEY_RETURN) {
                    setColor(Color.yellow);
                    System.out.println(input.getText());
                    setColor(Color.white);
                    Scripting.runString(input.getText());
                    input.setText("");
                }else if(key == Keyboard.KEY_UP){
                    if(position.hasNext()){
                        input.setText(position.next());
                    }
                }else if(key == Keyboard.KEY_DOWN){
                    if(position.hasPrevious()){
                        input.setText(position.previous());
                    }
                }
            }
        });

        scroller = new Timer(Graphics.gui);
        scroller.setContinuous(true);
        scroller.setDelay(100);
        scroller.setCallback(new Runnable() {
            public void run() {
                if(currentMax != scrollPane.getMaxScrollPosY()){
                    scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY());
                    scroller.stop();
                }
            }
        });

        scrollPane = new ScrollPane(output);
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        DialogLayout l = new DialogLayout();
        l.setTheme("content");
        l.setHorizontalGroup(l.createParallelGroup(scrollPane, input));
        l.setVerticalGroup(l.createSequentialGroup(scrollPane, input));
        add(l);
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    public class ConsoleAutoCompletionResult extends AutoCompletionResult {
        private ArrayList<String> results;

        public ConsoleAutoCompletionResult(ArrayList<String> results, String text, int prefixLength){
            super(text, prefixLength);
            this.results = results;
        }

        @Override
        public int getNumResults() {
            return results.size();
        }

        @Override
        public String getResult(int idx) {
            return results.get(idx);
        }
        
    }

    public class ConsoleAutoCompletionDataSource implements AutoCompletionDataSource {
        public AutoCompletionResult collectSuggestions(String text, int cursorPos, AutoCompletionResult prev) {
            ArrayList<String> results = new ArrayList<String>();
            for(String s : history){
                if(s.substring(0, Math.min(cursorPos, s.length())).equals(text.substring(0, Math.min(cursorPos, text.length())))){
                    results.add(s);
                }
            }

            return new ConsoleAutoCompletionResult(results, text, cursorPos);
        }
    }

    public void requestFocus(){
        input.requestKeyboardFocus();
    }
    
    public void print(String input){
        input = input.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace(" ", "&nbsp;");

        String colorString = "";
        colorString += String.format("%02x", getColor().getRed());
        colorString += String.format("%02x", getColor().getGreen());
        colorString += String.format("%02x", getColor().getBlue());
        String finalString = "<div style=\"font:" + colorString + "\">" + input + "</div><br/>";

        outputString += finalString;
        ((HTMLTextAreaModel)output.getModel()).setHtml(outputString);
        currentMax = scrollPane.getMaxScrollPosY();
        scroller.start();
    }
}
