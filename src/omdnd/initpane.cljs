(ns omdnd.initpane
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
           )
  (:require [goog.events :as gevents]
            [cljs.core.async :refer [put! <! chan dropping-buffer]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]

            [omdnd.util :as util]
            [omdnd.actor :as act]
            [omdnd.ux :as ux]
            [sablono.core :as html :refer [html] :include-macros true]

            )

)

(enable-console-print!)

(defn handle-reserve-drag [e app owner opts]

  ; send command message
  (when-let [command-chan (ux/command-chan opts)]
    (case (:event e)
      :drag-start nil
      :drag-end (om/set-state! owner :drag-hover nil)
      :dragging (om/set-state! owner :drag-hover true)
      :drop (put! command-chan {:event :reserve :actors [(:drag-item e)]}))))





(defn reserve-list [{:keys [actors] :as app} owner opts]
  (reify

    om/IDidMount
    (did-mount [t node]
               (ux/register-dimensions owner opts "reserve-list"  (om/bind handle-reserve-drag app owner opts)))


    om/IDidUpdate
    (did-update [_ _ prev-state _]
               (ux/update-bounds owner prev-state opts "reserve-list"))



    om/IWillUnmount
    (will-unmount [_]
                  (ux/unregister-dimensions owner opts "reserve-list"))



    om/IRender
    (render [this]

            (dom/div #js {:id  "reserve-list" :ref "reserve-list"
                         :className (when (om/get-state owner :drag-hover) "drag-hover")
                          }
                     ; (str (om/get-state owner))
            (dom/ul  nil ; #js {:id  "reserve-list" :ref   "reserve-list"}
                     (om/build-all act/actor-init-item (util/reserved actors)
                                   {:opts opts :key :id}
                                   ))
            ; bind mouse-up to handle-reserve-drop here. (or see below)
            ))))



; TODO : recalculate INITIATIVE based on drop position.
; Q?   : live-recalc init based on ordering, or only when dropped.

(defn handle-init-drag [e app owner opts]
  (om/set-state! owner :init-pane-recvd e)
  (om/set-state! owner :drag-hover true)
  ; send command message
  (when-let [command-chan (ux/command-chan opts)]
    (when (= :drop (:event e))
      (put! command-chan {:event :to-init :actors [(:drag-item e)]}))))


(defn init-list [{:keys [actors current-init current-round] :as app} owner opts]
  (reify

    om/IDidMount
    (did-mount [t node]
               (ux/register-dimensions owner opts "init-list" (om/bind handle-init-drag app owner opts)))

     om/IDidUpdate
    (did-update [_ _ prev-state _]
               (ux/update-bounds owner prev-state opts "init-list"))

    om/IWillUnmount
    (will-unmount [_]
                  (ux/unregister-dimensions owner opts "init-list"))

    om/IRender
    (render [_]

            (dom/div #js {:id "init-list" :ref "init-list" }
                     ; (str (om/get-state owner))

            (dom/ul  nil ;#js {:id "init-list" :ref "init-list" }
                     (om/build-all act/actor-init-item
                                   (util/init-list actors current-init current-round)
                                   {:opts opts :key :id} ))))))



(defn initpane [{:keys [actors current-init] :as app} owner opts]

  (reify
    om/IRender
    (render [_]
            (let [state (om/get-state owner)]

              (dom/div #js {:id "init-pane" :ref "init-pane"}
                       ; (dom/div #js {:id "drag-pane" } "opts=" (str  state) )
                       (om/build init-list app
                                 {:opts opts })
                       (om/build reserve-list app
                                 {:opts  opts}
                                 ))))


    ))
