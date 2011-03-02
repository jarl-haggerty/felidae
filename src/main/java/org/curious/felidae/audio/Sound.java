/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.audio;

import java.nio.IntBuffer;

import javax.sound.sampled.Clip;

import org.lwjgl.BufferUtils;

/**
 *
 * @author Jarl
 */
public class Sound {
    private Clip clip;

    public Sound(Clip clip){
        this.clip = clip;
    }
    
    public void start(){
    	if(clip.isActive()){
    		clip.setFramePosition(0);
    	}
    	clip.start();
    }
}
