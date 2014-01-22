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

    om/IRenderState
    (render-state [this state]
            (dom/div #js {:id  "reserve-list" :ref "reserve-list"
                         :className (when (:drag-hover state) "drag-hover")
                          }

            (dom/ul  nil ; #js {:id  "reserve-list" :ref   "reserve-list"}
                     (om/build-all act/actor-init-item (util/reserved actors)
                                   {:opts opts :key :id}
                                   ))
            ; bind mouse-up to handle-reserve-drop here. (or see below)
            ))))


(defn update-drag [owner e]
  (when (dragging? owner)
    (let [loc    (:location e)
          state  (om/get-state owner)
          [_ y]  (from-loc (:location state) loc)
          [_ ch] (:cell-dimensions state)
          drop-index (js/Math.round (/ y ch))]
      (when (not= (:drop-index state) drop-index)
        (doto owner
          (om/set-state! :drop-index drop-index)
          (om/set-state! :sort
            (insert-at ::spacer drop-index (:id e) (:real-sort state))))))))


; TODO : recalculate INITIATIVE based on drop position.
; Q?   : live-recalc init based on ordering, or only when dropped.
(defn handle-init-drag [e app owner opts]
  (when-let [command-chan (ux/command-chan opts)]
     (case (:event e)
      :drag-start (om/set-state! owner :dragging e)
      :drag-end (do (om/set-state! owner :drag-hover nil) (om/set-state! owner :dragging nil) )
      :dragging (om/set-state! owner :dragging e)
      :drop (put! command-chan {:event :to-init :actors [(:drag-item e)]}))))


(defn init-list [{:keys [actors current-init current-order current-round] :as app} owner opts]
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
                              (dom/li  #js { :className "round_marker"} (str "Current Round:" current-round))
                              (om/build-all act/actor-init-item
                                            (util/init-list actors current-init current-order current-round)
                                            {:opts opts :key :id} ))))))


(defn initpane [{:keys [actors current-init] :as app} owner opts]
  (reify
    om/IRenderState
    (render-state [_ state]
                  (dom/div #js {:id "init-pane" :ref "init-pane"}
                           ; (dom/div #js {:id "drag-pane" } "opts=" (str  state) )
                           (om/build init-list app
                                     {:opts opts })
                           (om/build reserve-list app
                                     {:opts  opts}
                                     )))


    ))
