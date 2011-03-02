(ns org.curious.interface
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.contrib.zip-filter.xml :as zf]
            [clojure.contrib.prxml :as prxml])
  (:import java.awt.GraphicsEnvironment
           java.awt.GraphicsDevice
           java.awt.DisplayMode
           javax.swing.JFrame
           javax.media.opengl.GLEventListener
           javax.media.opengl.GLProfile
           javax.media.opengl.GLCapabilities
           javax.media.opengl.awt.GLCanvas
           javax.media.opengl.GL
           com.jogamp.opengl.util.FPSAnimator))

(def graphics-environment (GraphicsEnvironment/getLocalGraphicsEnvironment))
(def graphics-device (.getDefaultScreenDevice graphics-environment))
(def display-mode (atom nil))
(def fullscreen (atom false))

(def gl-listener (proxy [GLEventListener] []
                   (display [drawable] (binding [gl (-> drawable .getGL .getGL2)]
                                         (game/draw)))
                   (dispose [drawable])
                   (init [drawable])
                   (reshape [drawable x y width height])))

(GLProfile/initSingleton)
(def gl-profile (GLProfile/getDefault))
(def gl-capabilities (GLCapabilities. gl-profile))
(def canvas (doto (GLCanvas. gl-capabilities)
              (.addGLEventListener gl-listener)))

(def animator (doto (FPSAnimator. canvas 60)
                (.add canvas)))

(def frame (doto (JFrame. game/title)
             (.setResizable false)
             (.add gl-canvas)))

(defn load-settings
  (let [data (zip/xml-zip (xml/parse "settings.xml"))
        screen-width (Integer/valueOf (zf/xml-> data :screen-width zf/text))
        screen-height (Integer/valueOf (zf/xml-> data :screen-height zf/text))
        new-fullscreen (and (.isFullScreenSupported graphics-device)
                            (.isDisplayChangeSupported graphics-device)
                            (Boolean/valueOf (zf/xml-> data :fullscreen zf/text)))]
    (if-let [new-display-mode (first (filter #(and (= screen-width (.getWidth %)) (= screen-height (.getHeight %))) (.getDisplayModes graphics-device)))]
      (swap! display-mode (fn [x] new-display-mode))
      (swap! display-mode (fn [x] (min-key #(.getWidth %) (.getDisplayModes graphics-device)))))
    (swap! fullscreen (fn [x] new-fullscreen))
    (with-open [output (writer "settings.xml")]
      (.write output (with-str-out
                       (binding [prxml/*prxml-indent* 2]
                         (proxml prxml [:settings
                                        [:screen-width (.getWidth @display-mode)]
                                        [:screen-height (.getHeight @screen-height)]
                                        [:fullscreen fullscreen]])))))))

(defn init-display []
  (if fullscreen
    (do (.setUndecorated frame true)
        (.setIgnoreRepaint frame true)
        (.setVisible frame true)
        (.setFullScreenWindow graphics-device frame)
        (.setDisplayMode graphics-device @display-mode))
    (do (.setUndecorated frame false)
        (.setIgnoreRepaint frame false)
        (.setVisible frame true)))
  (.start animator))

(defn init []
  (load-settings)
  (init-display))

(defn set-color [gl red green blue alpha]
  (.glColor gl red green blue alpha))

(defn set-clear-color [gl red green blue alpha]
  (.glClearColor gl red green blue alpha))

(defn draw-lines [gl & points]
  (.glBegin gl GL/GL_LINE_STRIP)
  (doseq [point points]
    (apply #(.glVertex3f gl %1 %2 %3) point))
  (.glEnd gl))
