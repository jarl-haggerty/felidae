(ns org.curious.felidae.game
  (:require [org.curious.felidae.agent :as agent]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.contrib.zip-filter :as zf]
            [clojure.contrib.zip-filter.xml :as zfx]
            [clojure.string :as string])
  (:import org.jbox2d.dynamics.World
           org.jbox2d.collision.AABB
           org.jbox2d.common.Vec2
           java.awt.Color))

(def agents)
(def agent-set (atom nil))
(def simulation (atom nil))
(def pixels-per-meter (atom nil))
(def background (atom nil))
(def width)
(def height)

(defn agent-map [agents]
  (into {} (for [a agents]
             [(:name a) a])))

(defn load-level [file-name]
  (let [data (zip/xml-zip (xml/parse file-name))
        _ (println (zfx/xml-> data :agents :agent zip/node))
        agent-data (map (fn [x] (->> x zf/children (map #(vector (:tag %) (:content %))) (into {}))) (zfx/xml-> data :agents :agent))
        new-agent-set (set (map agent/create agent-data))
        color-components (string/split (zfx/xml-> :level :background-color zfx/text) #",\s*")]
    (swap! pixels-per-meter (fn [x] (Double/parseDouble (zfx/xml-> :level :pixels-per-meter zfx/text))))
    (swap! simulation (fn [x] (World. (AABB. (Vec2. 0 0) (Vec2. (Double/parseDouble (zfx/xml-> :level :width zfx/text))
                                                               (Double/parseDouble (zfx/xml-> :level :height zfx/text))))
                                     (Vec2. 0 0)
                                     true)))
    (swap! background (fn [x] (Color. (first color-components) (second color-components) (last color-components))))
    (swap! agent-set (fn [x] (with-bindings [agent-set new-agent-set
                                            agents (agent-map new-agent-set)]
                              (map agent/initialize agent-set))))))

(defn update []
  (binding [agents (agent-map @agent-set)
            simulation @simulation]
    (swap! agent-set #(map agent/update %)))
  (.step simulation 1/60 6))

(defn render []
     (doseq [a @agent-set]
       (agent/render a)))
