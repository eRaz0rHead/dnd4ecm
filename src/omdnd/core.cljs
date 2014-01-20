(ns omdnd.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
           )
  (:require [goog.events :as gevents]
            [cljs.core.async :refer [put! <! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]

            [omdnd.actor :as act]
            [omdnd.util :as util]
             [omdnd.ux :as ux]
            [omdnd.initpane :as initpane]
            [omdnd.centerpane :as centerpane]
            [omdnd.rightpane :as rightpane]
            [sablono.core :as html :refer [html] :include-macros true]

            [goog.dom :as gdom]
            [goog.fx.DragListGroup :as dlg]
            [ goog.fx.DragListDirection :as dldir]
            [goog.fx.DragDrop :as dd]


            )
  ;(:import [goog History]
    ;       [goog.history EventType])
)

(enable-console-print!)

(def ENTER_KEY 13)



;; =============================================================================
;; Routing

;(defroute "/" [] (swap! app-state assoc :showing :all))

;(defroute "/:filter" [filter] (swap! app-state assoc :showing (keyword filter)))

;(def history (History.))

;(events/listen history EventType/NAVIGATE
;  (fn [e] (secretary/dispatch! (.-token e))))

;(.setEnabled history true)




(def app-state (atom {:actors  (util/generate-rnd-monsters 7)
                      :current-init 0
                      :current-round 0
                      }
                     ))



(defn set-actors-value [ids app k v]
  (om/transact! app  :actors
                (fn [actors]
                  (into [] (map #(if (contains? ids (:id %))
                                   (if-not (nil?  v)
                                     (assoc % k v)
                                     (dissoc % k))
                                   %) actors)))))



(defn set-reserved [ids app]
  (om/update! app assoc :messages (str  " reserved > " ids ))
  (set-actors-value ids app :reserved "true"))

(defn add-to-init [ids app]
  (om/update! app assoc :messages (str  " TO COMBAT > " ids ))
  (set-actors-value ids app :reserved nil))


(defn handle-command [{:keys [event actors] :as e} app  owner]
    (om/set-state! owner :command-event e)
    (case event
      :reserve  (set-reserved (set (map #(om/read % :id) actors)) app)
      :to-init  (add-to-init (set (map #(om/read % :id) actors)) app)
      )
  )



(defn main-panel [app owner opts]
      (reify
      om/IWillMount
      (will-mount [_]
                  (ux/setup-comms app owner {:command-handler handle-command}))

      om/IRender
      (render [_]
              (let [state   (om/get-state owner)
                    m       {:opts (ux/add-chans state {}) }]
                (dom/div #js { :id "main"}

                         (om/build initpane/initpane app m)

                        ; (om/build centerpane/centerpane app  m )
                           (dom/div #js { :id "center-pane"} (str state))
                         (om/build rightpane/rightpane app  m )


                         )))))


(om/root app-state main-panel   (.getElementById js/document "container"))




