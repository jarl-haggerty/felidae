/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.logging;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import felidae.scripting.Scripting;

/**
 *
 * @author Jarl
 */
public class FelidaeStream extends PrintStream{
    private Color color;
    private PrintStream systemStream;
    private String buffer;

    public FelidaeStream(PrintStream systemStream, Color color){
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        });
        this.color = color;
        this.systemStream = systemStream;
        buffer = "";
    }

    @Override
    public void write(byte[] b, int offset, int len){
        String input = new String(b).substring(offset, offset+len);
        Logger.write(input);

        if(Scripting.initialized){
            Scripting.getConsole().setColor(color);
            Scripting.getConsole().print(input);
        }else{
            buffer += input;
        }

        systemStream.write(b, offset, len);
    }

    public void emptyBuffer(){
        Scripting.getConsole().setColor(color);
        Scripting.getConsole().print(buffer);
        buffer = "";
    }

    public void println(String input, Color color){
        Color temp = this.color;
        this.color = color;
        println(input);
        this.color = temp;
    }
}
