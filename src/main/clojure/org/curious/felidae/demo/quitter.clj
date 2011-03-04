(ns org.curious.felidae.demo.quitter
  (:import java.awt.event.KeyEvent)
  (:require [org.curious.felidae.interface :as interface]
            [org.curious.felidae.agent :as agent]
            [org.curious.felidae.core :as felidae]))

(defrecord Quitter []
  agent/Agent
  (initialize [this] this)
  (update [this] this)
  (render [this] this)
  (process-input [this input]
                 (if (= (.getKeyCode input) KeyEvent/VK_ESCAPE)
                   (System/exit 0))
                 this)
  (process-collision [this contact-point] this)
  (process-separation [this contact-point] this))

(defmethod agent/create "quitter" [data]
           (Quitter.))
