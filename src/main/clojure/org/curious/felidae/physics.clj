(ns org.curious.felidae.physics
  (:refer-clojure :exclude [remove]))

(def subscriptions (atom {}))

(def contact-listener (proxy [ContactListener] []
                        (add [contact-point] (let [this (-> contact-point .shape1 .m_body .m_userData)
                                                   that (-> contact-point .shape2 .m_body .m_userData)
                                                   subjects (get @subscriptions this)]
                                               (cond (not subjects) nil
                                                     (empty? subjects) (process-collision contact-point)
                                                     (contains? (get @subscriptions this) that) (process-collision contact-point))))
                        (persist [contact-point])
                        (remove [contact-point] (let [this (-> contact-point .shape1 .m_body .m_userData)
                                                      that (-> contact-point .shape2 .m_body .m_userData)
                                                      subjects (get @subscriptions this)]
                                                  (cond (not subjects) nil
                                                        (empty? subjects) (process-collision contact-point)
                                                        (contains? (get @subscriptions this) that) (process-collision contact-point))))
                        (result [contact-point])))

(defn subscribe
  ([this] (swap! subscriptions #(assoc % this #{})))
  ([this that] (swap! subcriptions #(if (contains? % this)
                                      (assoc % this #{that})
                                      (assoc % this (conj (get % this) that))))))

(defn unsubscribe
  ([this] (swap! subscriptions #(dissoc % this)))
  [this that] (swap! subscriptions #(assoc % this (disj (get % this) that))))
