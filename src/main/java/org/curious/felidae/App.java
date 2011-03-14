package org.curious.felidae;

import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Game game = new Game("Test");
        game.start("Test.xml");
    }
}
