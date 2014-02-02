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


            )
  ;(:import [goog History]
    ;       [goog.history EventType])
)

(enable-console-print!)

(def ENTER_KEY 13)

(def app-state (atom {:actors  (util/establish-order (util/generate-rnd-monsters 7))
                      :current-init 10000
                      :current-round 1
                      }
                     ))


(defn set-actors-value [ids app k v]
  (om/transact! app  :actors
                (fn [actors]
                  (into [] (map #(if (some #{(:id %)} ids )
                                   (if-not (nil?  v)
                                     (assoc % k v)
                                     (dissoc % k))
                                   %) actors)))))

(defn by-id [id actors]
  (filter #(= (:id %) id) actors))

(defn replace-actors [to-replace actors]
  (let [ids (vec (map :id to-replace))]
    (map (fn [actor]
           (let [id (:id actor)]
             (if  (some #{id} ids)
               (do
                 ;(prn "replacing actor:" id  " with  "   (by-id id to-replace))
                   (merge actor (by-id id to-replace)))

               actor
               )))
           actors)))

(defn replace-actors-state [to-replace app]
  (om/transact! app  :actors
                (fn [actors]
                  (into []  (replace-actors to-replace actors)
                        ))))


 (defn normalize-order [ app]
  (om/transact! app  :actors
                (fn [actors]
                  (into []  (util/establish-order actors)
                        ))))


(defn handle-next-turn [app ]
  (let [next-actor  (second  (util/init-list (:actors @app ) (:current-init @app) (:current-order @app)))
        is_new_round  (= (first (util/sort-actors (util/active (:actors @app ))))   next-actor)]

   (om/update! app assoc :current-init  (:init next-actor ))
   (om/update! app assoc :current-order (:order next-actor ))
   (when is_new_round
      (om/update! app assoc :current-round (inc (:current-round @app))))
  ))

(defn set-reserved [ids app]
  (prn ids)
  (om/update! app assoc :messages (str  " reserved > " ids ))
  (set-actors-value ids app :reserved "true"))

(defn add-to-init [actors app]
  (om/update! app assoc :messages (str  " TO COMBAT > " (set (map :id actors))  (first (map :init actors))))
  (let [ids (set (map :id actors))]
    (replace-actors-state actors app)
    (set-actors-value ids app :reserved nil)
    (normalize-order app)
    (om/update! app assoc :current-order (max (map :order  (filter #(= (:current-init @app) (:init %)) actors ))))
    ))

(defn handle-command [{:keys [event actors] :as e} app  owner]
    (om/set-state! owner :command-event e)
    (case event
      :next-turn (handle-next-turn  app)
      :reserve  (set-reserved (set (map #(:id %) actors)) app)
      :to-init  (add-to-init actors app)
      )
  )


(defn main-panel [app owner opts]
      (reify
      om/IWillMount
      (will-mount [_]
                  (ux/setup-ux-master app owner {:command-handler handle-command}))

      om/IRenderState
      (render-state [_ state]
              (let [  m       {:opts (ux/add-chans state {}) }]
                (dom/div #js { :id "main"}

                         (om/build initpane/initpane app m)

                         (om/build centerpane/centerpane app  m )
                         ;(dom/div #js { :id "center-pane"} (str state))
                         (om/build rightpane/rightpane app  m )

                         )))))


(om/root app-state main-panel   (.getElementById js/document "container"))




