(ns org.curious.felidae.core
  (:require [org.curious.felidae.game :as game]
            [org.curious.felidae.interface :as interface]))

(defn run [name entry-point]
  (interface/init name)
  (game/load-level entry-point))
