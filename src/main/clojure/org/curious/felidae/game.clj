(ns org.curious.felidae.game
  (:require [org.curious.felidae.agent :as agent])
  (:import org.jbox2d.dynamics.World
           org.jbox2d.collision.AABB
           org.jbox2d.common.Vec2
           java.awt.Color))

(def agent-set (atom nil))
(def simulation (atom nil))
(def pixels-per-meter (atom nil))
(def width)
(def height)

(defn agent-map [agents]
  (into {} (for [a agents]
             [(:name a) a])))

(def load-level [file-name]
     (let [data (zip/xml-zip (xml/parse "settings.xml"))
           agent-data (map #(->> % zf/children (map #(vector (:tag %) (:content %))) (into {})) (xf/xml-> :agents :agent))
           new-agent-set (set (map agent/create agent-data))
           color-components (string/split (zf/xml-> :level :background-color zf/text) #",\s*")]
       (swap! pixels-per-meter (fn [x] (Double/parseDouble (zf/xml-> :level :pixels-per-meter zf/text))))
       (swap! simulation (fn [x] (World. (AABB. 0 0))) (World. (AABB. (Vec2. 0 0) (Vec2. (Double/parseDouble (zf/xml-> :level :width zf/text))
                                                                                       (Double/parseDouble (zf/xml-> :level :height zf/text))))))
       (swap! background (fn [x] (Color. (first color-components) (second color-components) (last color-components))))
       (swap! agent-set (fn [x] (with-bindings [agent-set new-agent-set
                                               agents (agent-map new-agent-set)]
                                 (map agent/initialize new-agents))))))

(defn update []
  (binding [agents (agent-map @agent-set)
            simulation @simulation]
    (swap! agent-set #(map agent/update %)))
  (.step simulation 1/60 6))

(defn render []
     (doseq [a @agents]
       (agent/render a)))
