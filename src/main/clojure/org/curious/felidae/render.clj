(ns org.curious.felidae.render
  (:require [clojure.java.io :as io])
  (:import java.awt.Font
           javax.media.opengl.GLEventListener
           javax.media.opengl.GLProfile
           javax.media.opengl.GLCapabilities
           javax.media.opengl.awt.GLCanvas
           javax.media.opengl.GL
           com.jogamp.opengl.util.FPSAnimator))

(def gl)
(def gl-queue (atom nil))
(def fonts (into {} (for [font-file (filter #(re-matches #".+\.ttf" %) (.list (io/file "fonts")))]
                      [((re-find #"(.+)\.ttf" font-file) 1) (->> font-file (str "fonts/") io/input-stream (Font/createFont Font/TRUETYPE_FONT) (TextRenderer. ))])))
(def font)
(def color Color/white)
(def depth)

(defn on-gl-thread [input]
  (swap! gl-queue #(conj % input)))

(defn set-clear-color [color]
  (.glClearColor gl (/ (.getRed color) 255.0) (/ (.getGreen color) 255.0) (/ (.getBlue color) 255.0) (/ (.getAlpha color) 255.0)))

(defmacro with-color [new-color & body]
  `(binding [color ~new-color]
     (.glColor4b gl (.getRed color) (.getGreen color) (.getBlue color) (.getAlpha color))
     ~@body))

(defmacro with-texture [new-texture & body]
  `(do (.glBindTexture gl GL/GL.GL_TEXTURE_2D new-texture)
       ~@body))

(defmacro with-font [new-font & body]
  `(binding [font ~(new-font fonts)]
     (.setColor font color)
     (.begin3DRendering font)
     ~@body
     (.end3DRendering font)))

(defmacro with-depth [depth & body]
  `(do (.glMatrixMode gl GL2/GL_MODELVIEW)
       (.glTranslate3f gl 0 0 (/ depth Float/MAX_VALUE))
       ~@body))
 
(defmacro with-transform [x y theta & body]
  `(do (.glMatrixMode gl GL2/GL_MODELVIEW)
       (.glLoadIdentity gl)
       (.glRotatef gl theta 0 0 -1)
       (.glTranslate3f gl x y 0)
       ~@body))

(def make-vbo [& vec-tex-pairs]
     (let [gen-space (int-array 1)
           vbo (do (.glGenBuffersARB gl 1 gen-space) (aget gen-space 0))
           data-space (float-array (* (count vec-tex-pairs) 5/2))]
       (.glBindBufferARB gl GL/GL_ARRAY_BUFFER_ARB vbo)

       (loop [stack vec-tex-pairs index 0]
         (if-let [[one two] (take 2 stack)]
           (aset-float data-space index (first one))
           (aset-float data-space (+ index 1) (second one))
           (aset-float data-space (+ index 2) depth)
           (aset-float data-space (+ index 3) (first two))
           (aset-float data-space (+ index 4) (second two))
           (recur (rest (rest stack)) (+ index 5))))
       
       (.glBufferDataARB gl GL/GL_ARRAY_BUFFER_ARB (-> data-space count (* 5 4)) data-space GL/GL_STATIC_DRAW_ARB)
       {:data vbo :count (/ (count vec-tex-pairs) 2)}))

(defn render-vbo [vbo]
  (.glEnableClientState gl GL/GL_VERTEX_ARRAY)
  (.glEnableClientState gl GL/GL_TEXTURE_COORD_ARRAY)
  
  (.glBindBufferARB gl GL/GL_ARRAY_BUFFER_ARB (:data vbo))
  (.glVertexPointer gl 3 GL/GL_FLOAT 2 0)
  (.glTexCoordPointer gl 2 GL/GL_FLOAT 3 2)
  (.glDrawArrays GL/GL_TRIANGLE_FAN 0 (:count vbo))

  (.glDisableClientState gl GL/GL_VERTEX_ARRAY)
  (.glDisableClientState gl GL/GL_TEXTURE_COORD_ARRAY))

(defn set-view [x y width height]
  (.glMatrixMode gl GL2/GL_PROJECTION)
  (.glLoadIdentity gl)
  (.glOrthof gl x (+ x width) y (+ y height) 0 1))
