(ns org.curious.felidae.game
  (:require [org.curious.felidae.agent :as agent]
            [org.curious.felidae.render :as render]
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

(defn agent-map [input]
  (into {} (for [a input]
             [(:name a) a])))

(defn load-level [file-name]
  (let [data (zip/xml-zip (xml/parse (str "levels/" file-name)))
        agent-data (map #(into {} (for [property (:content %)]
                                    [(:tag property) (first (:content property))]))
                        (zfx/xml-> data :agents :agent zip/node))
        new-agent-set (set (map agent/create agent-data))
        _ (println (first (zfx/xml-> data :level :background-color zfx/text)))
        color-components (map #(Integer/parseInt %) (string/split (first (zfx/xml-> data :level :background-color zfx/text)) #",\s*"))]
    (swap! pixels-per-meter (fn [x] (Double/parseDouble (first (zfx/xml-> data :level :pixels-per-meter zfx/text)))))
    (swap! simulation (fn [x] (World. (AABB. (Vec2. 0 0) (Vec2. (Double/parseDouble (first (zfx/xml-> data :level :width zfx/text)))
                                                               (Double/parseDouble (first (zfx/xml-> data :level :height zfx/text)))))
                                     (Vec2. 0 0)
                                     true)))
    (swap! background (fn [x] (Color. (first color-components) (second color-components) (last color-components))))
    (render/on-gl-thread (fn [] (render/set-clear-color @background)))
    (binding [agents (agent-map new-agent-set)]
      (swap! agent-set (fn [x] (keep agent/initialize new-agent-set))))))

(defn update []
  (binding [agents (agent-map @agent-set)
            simulation @simulation]
    (swap! agent-set #(keep agent/update %)))
  (.step simulation 1/60 6))

(defn process-input [event]
  (let [temp (doall (keep #(agent/process-input % event) @agent-set))]
    (swap! agent-set (fn [x] temp))))

(defn render []
     (doseq [a @agent-set]
       (agent/render a)))
