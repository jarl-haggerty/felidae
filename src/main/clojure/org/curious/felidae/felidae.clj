(ns org.curious.felidae.core)

(defn run [given-name entry-point]
  (defonce name given-name)
  (interface/start)
  (game/load-level entry-point))
