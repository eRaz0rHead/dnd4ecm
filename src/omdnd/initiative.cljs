(ns omdnd.initiative
  (:require
   [clojure.string :as string]
   [clojure.set :as s]
   [om.core :as om :include-macros true]
   )
  )


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



;;;;;;;;;;;;;;;;
; Algorithm for Init used by DarkSir's Combat Manager


(defn die [num rng]
  (repeatedly num #(+ 1 (rand-int rng))))


(defn roll [x]
  (first (die 1 x) ))

;nInitRoll = die.Roll(20) + stats.nInit,
(defn initRoll [nInit]
  (+ (roll 20) nInit ))


(defn newInitMod [nInit]
  (+ (+ (roll 500) 200)
     (* (- 90 nInit) 1000)))

(defn initSeq [nRound nInitRoll newInitMod]
  (+ (* nRound 10000000)
     (* (- 95 nInitRoll) 100000)
     newInitMod ))


; (initSeq 1 (initRoll 20) (newInitMod 20))



