/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.logging;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JOptionPane;

/**
 *
 * @author Jarl
 */
public class Logger {
    private static java.util.logging.Logger logger;
    private static FelidaeStream out, err;
    private static FileWriter writer;

    public static boolean initialize(){
        out = new FelidaeStream(System.out, Color.white);
        err = new FelidaeStream(System.err, Color.red);
        System.setOut(getOut());
        System.setErr(getErr());
        writer = null;
        try {
            writer = new FileWriter("Felidae.log");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to create Felidae.log", "I/O Exception", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(null, "Do not have permission to create Felidae.log", "Security Exception", JOptionPane.ERROR_MESSAGE);
        }
        return true;
    }

    /**
     * @return the out
     */
    public static FelidaeStream getOut() {
        return out;
    }

    /**
     * @return the err
     */
    public static FelidaeStream getErr() {
        return err;
    }

    public static void write(String input){
        try {
            writer.write(input);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the logger
     */
    public static java.util.logging.Logger getLogger() {
        return logger;
    }

    public static void destroy(){
        try {
            if(writer != null){
                writer.close();
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
