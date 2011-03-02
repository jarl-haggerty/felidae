/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Jarl
 */
public class Audio {
	private static Object audioLock;
    private static Map<String, Sound> loadedSounds;
    private static Set<Stream> streaming;
    private static boolean running;
    private static Thread updateThread;
    static{
    	loadedSounds = new TreeMap<String, Sound>();
        streaming = new HashSet<Stream>();
        
        audioLock = new Object();
        updateThread = new Thread(new Updater());
        updateThread.start();
    	running = true;
    }

    public static boolean initialize(){
        return true;
    }

    public static boolean clearMemory(){
        loadedSounds.clear();
        synchronized(Audio.class){
                for(Stream s : streaming){
                    s.stop();
                }
        	streaming.clear();
        }
        return true;
    }
    
    public static synchronized int getStreamingCount(){
    	return streaming.size();
    }

    public static void destroy(){
    	clearMemory();
    	running = false;
        try {
			updateThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static class Updater implements Runnable {
    	public void run(){
    		while(running){
    			synchronized(Audio.class){
	    			List<Stream> toRemove = new ArrayList<Stream>();
			        for(Stream s : streaming){
			        	if (!s.update()){
			        		toRemove.add(s);
			        	}
			        }
			        streaming.removeAll(toRemove);
    			}
    		}
                System.out.println("Audio Done");
    	}
    }

    public static boolean update(){
    	List<Stream> toRemove = new ArrayList<Stream>();
        for(Stream s : streaming){
        	if (!s.update()){
        		toRemove.add(s);
        	}
        }
        streaming.removeAll(toRemove);
        return true;
    }

    public static void pause(){
        for(Stream s : streaming){
            s.stop();
        }
    }

    public static Sound loadSound(String soundName){
        if (loadedSounds.containsKey(soundName)) {
            return loadedSounds.get(soundName);
        }else{
			try {
				File soundFile = new File(soundName);
				AudioInputStream sound;
				sound = AudioSystem.getAudioInputStream(soundFile);
				DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
				Clip clip = (Clip) AudioSystem.getLine(info);
				clip.open(sound);
				return new Sound(clip);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return null;
			}
        }
    }

    public static void playSound(Sound sound){
        sound.start();
    }
    
    public static synchronized Stream loadStream(String soundName){
        Stream stream;
		try {
			stream = new Stream(soundName);
			return stream;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
    }

    public static synchronized boolean playStream(Stream stream){
        try {
            stream.play();
            streaming.add(stream);
            return true;
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }

    public static synchronized boolean playStream(Stream stream, boolean loop){
    	stream.loop = loop;
    	stream.play();
    	streaming.add(stream);
    	return true;
    }

    public static synchronized boolean stopStream(Stream stream){
    	streaming.remove(stream);
    	stream.stop();
    	return true;
    }
}
