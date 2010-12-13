(ns org.curious.state
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.contrib.zip-filter.xml :as zf]
            [org.curious.interface :as interface]
            [org.curious.actor :as actor]
            [org.curious.pounce.simulation :as simulation]))

(defn state [file-name]
  (let [level-xml (zip/xml-zip (xml/parse file-name))
        result    {:title (keyword (.replace file-name ".xml" ""))
                   :background-color (zf/xml-> level-xml :level :background-color)
                   :pixels-per-meter (zf/xml-> level-xml :level :pixels-per-meter)
                   :actors (for [element (zf/xml-> level-xml :elements :element)]
                             (actor/create (apply hash-map (interleave (map :tag (zf/xml-> element zip/children))
                                                                       (zf/xml-> element zip/children zf/text)))))
                   :simulation (simulation/create)
                   :period 1/60}]
    (doseq [a (:actors result)]
      (actor/initialize a result))))

(defn step [input]
  (assoc input
    :actors (for [a (:actors input)]
              (actor/update a (:period input)))
    :simulation (simulation/step (:simulation input) (:period input))))

(defn render [input]
  (interface/clear (:background-color input))
  (doseq [a (:actors input)]
    (actor/render a input)))
