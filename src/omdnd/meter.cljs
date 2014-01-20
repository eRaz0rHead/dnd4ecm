(ns omdnd.meter
  (:require [clojure.string :as string]
            [omdnd.util :as util]
  )
)

;;  template for the hp meter
(defn meter [hps tmp total]
  (let [meter-names (cond-> ["hp-meter"]
                            (<= (/ hps total) 0.5) (conj "bloodied")) ]

    [:div  { :className  (string/join " " meter-names) }
     [:span.tmp  {:style {:width (util/perc (+ tmp hps) total)} }  nil ]
     [:span.hp  {:style {:width (util/perc hps total)} }  nil ]
     ]
    ))

(defn meter-native [hps tmp total]
  (let [bloodied (/ total 2) ]
  [:meter {:value hps :min 0 :max total :low bloodied :high (- total tmp) :optimum total
           :style {
                   :display "block"
                   :width   "200px"
                   }
           } ]
 ))
;; end template for the hp meter
