(ns org.curious.felidae.demo.text
  (:import java.awt.event.KeyEvent)
  (:require [org.curious.felidae.interface :as interface]
            [org.curious.felidae.agent :as agent]
            [org.curious.felidae.core :as felidae]
            [org.curious.felidae.render :as render]))

(defrecord Text [x y text]
  agent/Agent
  (initialize [this] this)
  (update [this] this)
  (render [this] (render/render-string x y text))
  (process-input [this input] this)
  (process-collision [this contact-point] this)
  (process-separation [this contact-point] this))

(defmethod agent/create "text" [data]
           (Text. {:x data} {:y data} {:text data}))
