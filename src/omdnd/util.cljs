(ns omdnd.util
  (:require
   [clojure.string :as string]
   [sablono.core :as html :refer [html] :include-macros true]
   [clojure.set :as s]
   [om.core :as om :include-macros true]
   )
  )

(enable-console-print!)


(def conditions
  ["Blinded" "Dazed" "Deafened" "Dominated" "Dying" "Helpless" "Immobilized" "Petrified" "Prone"
   "Restrained" "Slowed" "Stunned" "Surprised" "Unconscious" "Weakened" "grabbed" "hidden" "removed from play"])

(defn Griff [] {:id 1
             :name "Griff"
             :initBonus 12
             :init 22
             :hps 77
             :totalhps 126
             :tmp 10
             :effects ["Stunned" "Blinded"]
             :powers {
                      :standard ["Kill 'em all"]
                       :immediate ["Get back!"]
                      }
             })




(def ascending compare)
(def descending #(compare %2 %1))

(defn compare-by [& key-cmp-pairs]
  (fn [x y]
    (loop [[k cmp & more] key-cmp-pairs]
      {:pre [(keyword? k), (fn? cmp), (even? (count more))]}
      (let [result (cmp (k x) (k y))]
        (if (and (zero? result) more)
          (recur more)
          result)))))



(defn die [num rng]
  (repeatedly num #(+ 1 (rand-int rng))))

(defn perc [n d]
  (str (.round js/Math  (* (/ n  d) 100)) "%" ))

(defn random-monster [id ]
  (let [initBonus (apply + 3 (die 3 8))
        initRoll  (apply + (die 1 20))
        totalhps  (apply + 50 (die 7 10))]
    {:id id
     ;:order nil
     :name (str "Actor #" id)
     :initBonus initBonus
     :initRoll initRoll
     :init (+ initRoll initBonus)
     :totalhps totalhps
     :hps (/ totalhps  (first(die 1 4) ))
     :tmp 0
     :reserved false
     :effects (let [x (first (die 1 4))] (into #{} (repeatedly x #(rand-nth conditions))))
     }

    ))
(defn generate-rnd-monsters [num]
  (vec (map random-monster (range num))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;  Initiative filters
;;;;

(defn new-round-marker [current-round]
  (om/to-cursor {:id -1 :key: -1 :new_round (str "Next Round: " (+ 1 current-round))} {:new_round true}))

(defn reserved [actors] (filter #( :reserved %)  actors))

(defn dead [actors] (filter #(== 0 (:hps %)) actors))

(defn active [actors]
  (s/difference (set actors)  (s/union (set (reserved actors)) (set (dead actors)))))

(defn sort-actors [actors]
  (sort-by (juxt :init #(+ 0 (get % :order 0 )) :initBonus) #(compare %2 %1) actors)
  )


(defn establish-order [actors]
  (let [init-groups (partition-by :init (sort-actors actors))]
    (vec (flatten  (map (fn[group]
                          (map-indexed (fn [idx member]
                                         (assoc member :order (- (count group) idx)))
                                       (sort-by (juxt :order :initBonus) #(compare %2 %1) group)))
                        init-groups)
                   ))))

(defn lower-list [current-init current-order]
  (fn [actor]
    (if (=  current-init (:init actor))
      (< current-order (get actor :order 0))
      (if (< current-init (:init actor))
        true
        false)
      )))


(defn init-list [actors current-init current-order & [current-round]]
  (let [sorted-list (sort-actors  (active actors))
        lower       (filter #((lower-list current-init current-order) %) sorted-list)
        upper       (filter #((complement (lower-list current-init current-order)) %) sorted-list)]
    (if current-round
      (vec (flatten (conj lower (new-round-marker current-round) upper)))
      (vec (flatten (conj lower upper)))
    )))



;;;;;;;;

(defn index-of [x v]
  (loop [i 0 v (seq v)]
    (if v
      (if (= x (first v))
        i
        (recur (inc i) (next v)))
      -1)))

(defn insert-at [x idx ignore v]
  (let [len (count v)]
    (loop [i 0 v v ret []]
      (if (>= i len)
        (conj ret x)
        (let [y (first v)]
          (if (= y ignore)
            (recur i (next v) (conj ret y))
            (if (== i idx)
              (into (conj ret x) v)
              (recur (inc i) (next v) (conj ret y)))))))))
