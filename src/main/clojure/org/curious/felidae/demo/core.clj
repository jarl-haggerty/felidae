(ns org.curious.felidae.demo.core
  (:require [org.curious.felidae.core :as felidae]
            org.curious.felidae.demo.quitter))

(felidae/run "Felidae Test" "test.xml")

