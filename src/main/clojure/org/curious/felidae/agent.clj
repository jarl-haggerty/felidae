(ns org.curious.felidae.agent)

(defmulti create :role)
(defprotocol Agent
  (initialize [this])
  (update [this])
  (render [this])
  (process-input [this input])
  (process-collision [this that])
  (process-separation [this that]))
