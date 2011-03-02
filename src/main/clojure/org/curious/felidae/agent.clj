(ns org.curious.felidae.agent)

(defmulti create :type)
(defprotocol Agent
  (initialize [this])
  (update [this])
  (render [this])
  (process-input [this input])
  (process-collision [this that]))
