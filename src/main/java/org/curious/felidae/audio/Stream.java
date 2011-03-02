/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.audio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Jarl
 */
public class Stream {
    final int BUFFER_SIZE = 4096 * 8;

    OggInputStream stream;
    public boolean loop;
    public String fileName;
    private SourceDataLine line;
    private AudioFormat format;
    private byte[] data;
    
    public Stream(String fileName) throws FileNotFoundException, LineUnavailableException{
        this(fileName, false);
    }
    
    public Stream(String fileName, boolean loop) throws FileNotFoundException, LineUnavailableException{
    	stream = new OggInputStream(new FileInputStream(fileName));
    	
    	format = new AudioFormat(stream.getRate(),
    							 16,
    							 stream.getFormat() == OggInputStream.FORMAT_MONO16 ? 1 : 2,
    							 true,
    							 false);
    	
    	DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(format);
    	
        this.fileName = fileName;
        this.loop = loop;
        data = new byte[4096];
    }
    
    public void rewind() throws FileNotFoundException, IOException{
    	line.drain();
    	
        stream.close();
        stream = new OggInputStream(new FileInputStream(fileName));
    }

    public void display(){
        System.out.println(stream);
    }

    public boolean playing(){
        return line.isActive();
    }
    
    public void play(){
    	line.start();
    }
    
    public void stop(){
    	line.stop();
    }

    public boolean update(){
	    int read;
		try {
			read = stream.read(data, 0, data.length);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    if(read != -1){
	    	line.write(data, 0, read);
	    	return true;
	    }else if(loop == true){
	    	try {
				rewind();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
	    	return true;
	    }else{
	    	return false;
	    }
    }
}
