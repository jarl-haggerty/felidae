(ns org.curious.state
  (:import java.util.Timer
           java.util.TimerTask)
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.contrib.zip-filter.xml :as zf]
            [org.curious.interface :as interface]
            [org.curious.actor :as actor]
            [org.curious.pounce.simulation :as simulation]))

(def codex
     (let [decodex-xml (zip/xml-zip (xml/parse "codex.xml"))]
       (apply hash-map
              (for [decoding (zf/xml-> decodex-xml :decoding)]
                [(int (zf/xml-> decoding :header zf/text))
                 (rest (map #(-> % zip/node :tag)
                            (zf/xml-> decoding zip/children)))]))))

(defn decode [buffer in]
  (let []
    (for [packet (get codex (.read in))]
      (condp = packet
          :float (.intBitsToFloat
                  (+ (bit-shift-left 24 (.read in))
                     (bit-shift-left 16 (.read in))
                     (bit-shift-left 8 (.read in))
                     (bit-shift-left 0 (.read in))))
          :integer (+ (bit-shift-left 24 (.read in))
                     (bit-shift-left 16 (.read in))
                     (bit-shift-left 8 (.read in))
                     (bit-shift-left 0 (.read in)))))))

(def actor-id (atom 0))

(defn load [file-name]
  (let [level-xml (zip/xml-zip (xml/parse file-name))
        result    {:title (keyword (.replace file-name ".xml" ""))
                   :background-color (zf/xml-> level-xml :level :background-color)
                   :pixels-per-meter (zf/xml-> level-xml :level :pixels-per-meter)
                   :actors (apply hash-map (for [element (zf/xml-> level-xml :elements :element)]
                                             [(if (.isEmpty (zf/xml-> element :title))
                                                (swap! actor-id inc)
                                                (zf/xml-> element :title))
                                              (actor/create (apply hash-map (interleave (map :tag (zf/xml-> element zip/children))
                                                                                        (zf/xml-> element zip/children zf/text))))]))
                   :simulation (simulation/create)
                   :frequency 60}]
    (doseq [a (:actors result)]
      (actor/initialize a result))
    result))

(defn update [input]
  (assoc input
    :actors (for [a (:actors input)]
              (actor/update a (:period input)))
    :simulation (simulation/step (:simulation input) (:period input))))

(defn start-server [xml-file]
  (let [state (atom (load xml-file))]
    (create-server 2718 (fn [in out]
                          (while @state
                            (condp = (.read in)
                                INPUT (let [data (decode in)]
                                        (swap! state (fn [s] (assoc s :actors
                                                                   (apply merge (for [[k v] (:actors s)]
                                                                                  {k (apply process-input data)}))))))
                                RENDER (do (doseq [a (:actors input)]
                                             (actor/render a out))
                                           (.write out RENDERED))))))
    (-> (Timer.)
        (.schedule (proxy [TimerTask] []
                     (run []
                          (swap! state update)
                          (when-not @state
                            (.cancel this))))
                   0
                   (int (/ 1000 (:frequency @state)))))))
