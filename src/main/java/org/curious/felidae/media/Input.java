/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

import java.awt.event.InputEvent;


/**
 *
 * @author jarl
 */
public class Input {
    public InputEvent event;
    public int value;
    public Input(InputEvent event, int value) {
        this.event = event;
        this.value = value;
    }

}
