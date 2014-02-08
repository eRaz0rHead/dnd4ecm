(ns omdnd.meter
  (:require [clojure.string :as string]
            [omdnd.util :as util]
            [sablono.core :as html :refer-macros [html] ]
  )
)

;;  template for the hp meter
(defn meter [hps tmp total]
  (html
   [:div  { :class ["hp-meter" (when (<= (/ hps total) 0.5)  "bloodied") ]}
    [:span.tmp  {:style {:width (util/perc (+ tmp hps) total)} }  nil ]
    [:span.hp  {:style {:width (util/perc hps total)} }  nil ]
    ]
   ))

(defn meter-native [hps tmp total]
  (let [bloodied (/ total 2) ]
    (html
     [:meter {:value hps :min 0 :max total :low bloodied :high (- total tmp) :optimum total
              :style {
                      :display "block"
                      :width   "200px"
                      }
              }])
    ))
;; end template for the hp meter
