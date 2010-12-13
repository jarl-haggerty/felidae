(ns org.curious.actor)

(defmulti create :role)
(defmulti initialize :role)
(defmulti render :role)
(defmulti volcalize :role)
(defmulti update :role)
