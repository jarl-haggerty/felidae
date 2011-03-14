/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.curious.felidae.media;

/**
 *
 * @author jarl
 */
public class VBO {
    public int vertices, texels, length;

    public VBO(int vertices, int texels, int length) {
        this.vertices = vertices;
        this.texels = texels;
        this.length = length;
    }
}
