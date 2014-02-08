(ns omdnd.actor
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   )
  (:require [goog.events :as events]
            [goog.debug :as debug]
            [cljs.core.async :refer [put! <! chan]]
            [clojure.string :as string]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html] ]

            [omdnd.util :as util]
            [omdnd.ux :as ux]
            [omdnd.meter :as meter]
            )

)


(enable-console-print!)


(def ENTER_KEY 13)
(def ESCAPE_KEY 27)


(defn set-selected [e id selected actor  owner opts]
  (prn "selecting  " id selected "<< "(:id actor) (:selected actor) )
  ;(om/update! actor assoc :selected (not (:selected @actor)))
  (when-let [command-chan (ux/command-chan opts)]
    (put! command-chan {:event :select :actors [id] :selected selected}))

  )

(defn handle-focus [e  ]
  (let [rng (.createRange js/document)
        sel   (.getSelection js/window)]
      (.selectNodeContents rng (.. e -target ))
      (.removeAllRanges sel)
      (.addRange sel rng)
    ))

(defn handle-submit [e actor owner opts]
  (om/set-state! owner [:init] (- (.. e -target -textContent) 0))
  (when-let [edit-text (om/get-state owner [:init])]
    (when-let [command-chan (ux/command-chan opts)]
      (put! command-chan {:event :to-init :actors  [(assoc actor :init edit-text)]})))
  false)

(defn handle-key-down [e actor owner opts]
  (let [kc (.-keyCode e)]
    (cond
     (identical? kc ESCAPE_KEY)
       (do
         (om/set-state! owner [:init] (:init actor))
           ; (.. e -target -setTextContent (:init @actor) )
         ;   (put! (:comm opts) [:cancel actor])
         ; (.. e -target -setTextContent (:init @actor) )
           (.blur (.. e -target))
         )

     (identical? kc ENTER_KEY)
         (do
           (.preventDefault e)
           (handle-submit e actor owner opts))

     (and (< kc 91) (> kc 57))
         (.preventDefault e)
     )
  ))


(defn actor-init-item [{:keys [id selected] :as actor} owner opts]

  (reify

    om/IDidMount
    (did-mount [t node]
        (ux/register-dimensions owner opts "drag-container"  ))


    om/IWillUpdate
    (will-update [_ next-props next-state]
      (ux/drag-listeners owner next-props next-state opts))

    om/IDidUpdate
    (did-update [_ _ prev-state _]
               (ux/update-bounds owner prev-state opts "drag-container"))

    om/IRenderState
    (render-state [this state]
      (let [m {:owner owner :opts opts}
            style (cond
                    (ux/dragging? owner)
                    (let [[x y] (:location state)
                          [w h] (:dimensions state)]
                      #js {:position "absolute"
                           :top y :left x
                           :width w :height h
                           })
                    :else
                    #js {:position "static" })]

          (if-not (nil? (:new_round actor))
            (dom/li #js {:key (:id actor) :id "next_round_marker" :className "round_marker"} (:new_round actor))
            (html
             [:li.actor.draggable
              {
               :class [(when (ux/dragging? owner) "el_moving")  (when (:selected actor) "selected")]
               :key (:id actor)
               :style style
               :ref "drag-container"

               }
               [:span.drag-handle
                {
                 :ref "drag-handle"
                 :onMouseDown #(ux/drag-start % @actor owner opts)
                 :onMouseUp #(ux/drag-end  % @actor owner opts)
                 :onMouseMove #(ux/drag % @actor owner opts)
                                   }
                "⣿" ]
              [:div
               {:onClick #(set-selected % id (not selected) @actor owner opts)
                }


              [:div.vitals
                [:span.name (:name actor)  ]

                ; [:span  (str  (:bounds state)) ]

                (if (:immediate (:powers actor)) [:span.actions "immediate!" ])
                (meter/meter (:hps actor) (:tmp actor) (:totalhps actor))]
                [:div.effects
                 [:ul
                  (for [effect (:effects actor)]  [:li effect]) ]]
               [:span.init-num {:contentEditable true
                                ; :onFocus  (fn [e] (handle-focus e   ))
                                :onKeyDown #(handle-key-down % @actor owner opts)
                                :onBlur #(handle-submit % @actor owner opts)
                                 } (:init actor)]
              ]
              ]
             )
          )


        ))))















