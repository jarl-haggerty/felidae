(ns org.curious.felidae.render
  (:import javax.media.opengl.GLEventListener
           javax.media.opengl.GLProfile
           javax.media.opengl.GLCapabilities
           javax.media.opengl.awt.GLCanvas
           javax.media.opengl.GL
           com.jogamp.opengl.util.FPSAnimator))

(def gl)
(def gl-queue (atom nil))
(def fonts (atom (into {} (for [font-file (filter (re-matches #".+\.ttf" (.list (file "fonts"))))]
                            [((re-find #"(.+)\.ttf" font-file) 1) (->> font-file input-stream (Font/createFont Font/TRUETYPE_FONT))]))))

(defn on-gl-thread [input]
  (swap! gl-queue #(conj % input)))

(defn set-clear-color [color]
  (println color)
  (.glClearColor gl (/ (.getRed color) 255.0) (/ (.getGreen color) 255.0) (/ (.getBlue color) 255.0) (/ (.getAlpha color) 255.0)))

(defn render-string [x y text]
  ())
