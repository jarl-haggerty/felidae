(ns org.curious.felidae.demo.quitter
  (:require [org.curious.felidae.agent :as agent]))

(deftype Quitter []
  agent/Agent
  (initialize [this] this)
  (update [this] this)
  (render [this] this)
  (process-input [this input]
                 (if (= (:key-code input) KeyEvent/VK_ESCAPE)
                   (System/exit 0)))
  (process-collision [this contact-point] this)
  (process-separation [this contact-point] this))

(defmethod create :quitter [data]
           (Quitter.))
