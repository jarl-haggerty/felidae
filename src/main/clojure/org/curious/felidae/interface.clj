(ns org.curious.felidae.interface
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.contrib.zip-filter.xml :as zf]
            [clojure.contrib.prxml :as prxml]
            [org.curious.felidae.game :as game]
            [org.curious.felidae.render :as render])
  (:import java.awt.GraphicsEnvironment
           java.awt.GraphicsDevice
           java.awt.DisplayMode
           java.awt.event.KeyListener
           java.awt.event.WindowListener
           javax.swing.JFrame
           javax.media.opengl.GLEventListener
           javax.media.opengl.GLProfile
           javax.media.opengl.GLCapabilities
           javax.media.opengl.awt.GLCanvas
           javax.media.opengl.GL
           com.jogamp.opengl.util.FPSAnimator))

(def graphics-environment (GraphicsEnvironment/getLocalGraphicsEnvironment))
(def graphics-device (.getDefaultScreenDevice graphics-environment))
(def desktop-display-mode (.getDisplayMode graphics-device))
(def display-mode (atom nil))
(def fullscreen (atom false))
(def frame)

(def gl-listener (proxy [GLEventListener] []
                   (display [drawable]
                            (binding [render/gl (-> drawable .getGL .getGL2)]
                              (.glClear render/gl GL/GL_COLOR_BUFFER_BIT)
                              (swap! render/gl-queue #(doseq [x %] (x)))
                              (game/render)))
                   (dispose [drawable] (println (.hashCode (Thread/currentThread))))
                   (init [drawable]
                         )
                   (reshape [drawable x y width height])))
(def key-listener (proxy [KeyListener] []
                    (keyPressed [event]
                                (game/process-input event))
                    (keyReleased [event])
                    (keyTyped [event])))
(def window-listener (proxy [WindowListener] []
                       (windowActivated [event])
                       (windowClosed [event] (.dispose frame))
                       (windowClosing [event] (System/exit 0))
                       (windowDeactivated [event])
                       (windowDeiconified [event])
                       (windowIconified [event])
                       (windowOpened [event])))

(GLProfile/initSingleton true)
(def gl-profile (GLProfile/getDefault))
(def gl-capabilities (GLCapabilities. gl-profile))
(def gl-canvas (doto (GLCanvas. gl-capabilities)
                 (.addGLEventListener gl-listener)
                 (.addKeyListener key-listener)
                 (.setFocusable true)))

(def animator (doto (FPSAnimator. gl-canvas 60)
                (.add gl-canvas)))

(def frame (doto (JFrame.)
             (.addWindowListener window-listener)
             (.setResizable false)
             (.add gl-canvas)))

(defn shutdown []
  (System/exit 0))

(defn load-settings []
  (let [data (zip/xml-zip (xml/parse "settings.xml"))
        screen-width (Integer/parseInt (string/trim (first (zf/xml-> data :screen-width zf/text))))
        screen-height (Integer/parseInt (string/trim (first (zf/xml-> data :screen-height zf/text))))
        new-fullscreen (and (.isFullScreenSupported graphics-device)
                            (Boolean/parseBoolean (string/trim (first (zf/xml-> data :fullscreen zf/text)))))]
    (if-let [new-display-mode (first (filter #(and (= screen-width (.getWidth %)) (= screen-height (.getHeight %))) (.getDisplayModes graphics-device)))]
      (swap! display-mode (fn [x] new-display-mode))
      (swap! display-mode (fn [x] (min-key #(.getWidth %) (.getDisplayModes graphics-device)))))
    (swap! fullscreen (fn [x] new-fullscreen))
    (with-open [output (io/writer "settings.xml")]
      (binding [prxml/*prxml-indent* 2
                *out* output]
        (prxml/prxml [:settings
                      [:screen-width (.getWidth @display-mode)]
                      [:screen-height (.getHeight @display-mode)]
                      [:fullscreen @fullscreen]])))))

(defn init-display []
  (if @fullscreen
    (do (.setUndecorated frame true)
        (.setIgnoreRepaint frame true)
        (.setVisible frame true)
        (.setFullScreenWindow graphics-device frame)
        (.setDisplayMode graphics-device @display-mode))
    (let [insets (.getInsets frame)]
        (.setUndecorated frame false)
        (.setIgnoreRepaint frame false)
        (.setSize frame (+ (.getWidth @display-mode) (.left insets) (.right insets))
                        (+ (.getHeight @display-mode) (.bottom insets) (.top insets)))
        (.setVisible frame true)))
  (.start animator))

(defn init [title]
  (.setTitle frame title)
  (load-settings)
  (init-display))
