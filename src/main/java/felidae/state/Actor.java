/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package felidae.state;

import felidae.Game;
import felidae.exchange.Input;
import javax.media.opengl.GL;

/**
 *
 * @author jarl
 */
public interface Actor {
    public String getName();
    public void render(GL gl);
    public void update(Game game);

    public void processInput(Input input);
}
