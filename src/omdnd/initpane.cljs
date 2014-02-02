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
               (ux/register-dimensions owner opts "reserve-list"  #(handle-reserve-drag % app owner opts)))

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

                           ))))


(defn from-loc [v1 v2]
  (vec (map - v2 v1)))

(defn update-drag [owner e]
  (let [loc    (:location e)
        item   (:drag-item e)
        state  (om/get-state owner)
        [_ y]  (from-loc (:location state) loc)
        [_ ch] (:cell-dimensions state)
        drop-index (js/Math.round (/ y ch))]
    (when (not= (:drop-index state) drop-index)
      (doto owner
        (om/set-state! :drop-index drop-index)
        (om/set-state! :dragging item)
        ))))

(defn avg [n m]
  (/ (+ n m) 2))


(defn insert-between [actor p n]
  (if (nil? n)
    [ (merge actor {:init (:init p) :order (- (:order p) 0.5) }) p ]

    [ (merge actor {:init (:init n) :order (+ (:order n) 0.5) }) p n ]
    ))


(defn handle-drop [{:keys [actors current-init current-order] :as app}  owner opts e]
  (when-let [command-chan (ux/command-chan opts)]
    (let [state (om/get-state owner)
          next-idx (:drop-index state)
          initiative (util/init-list actors current-init current-order)
          next-item (nth initiative  next-idx nil)
          prev-idx (min (dec next-idx) (dec (count initiative)))
          prev-item (nth initiative prev-idx nil)
          dropped-item (:drag-item e)
          ]
      (prn "prev " prev-item "next " next-item)
      (om/set-state! owner :drop-index nil)
      (put! command-chan {:event :to-init :actors (insert-between dropped-item prev-item next-item)})
      )))


(defn handle-init-drag [e app owner opts]
  (when-let [command-chan (ux/command-chan opts)]
    (case (:event e)
      :drag-start (update-drag owner e)
      :drag-end (do (om/set-state! owner :drag-hover nil) (om/set-state! owner :dragging nil) )
      :dragging (update-drag owner e)
      :drop  (handle-drop @app owner opts e)
      )))

(defn sortable-spacer [height]
  (dom/li #js {:key "spacer-cell"
               :style #js {:height height}}))

(defn sorting-state [init-list owner]
  (if  (ux/dragging? owner)
    (let [state (om/get-state owner)
          drop-index (:drop-index state)
          drag-id (:id (:dragging state))]
      (util/insert-at ::spacer drop-index drag-id init-list))
    init-list))

(defn init-list [{:keys [actors current-init current-order current-round] :as app} owner opts]
  (reify

    om/IDidMount
    (did-mount [t node]
               (ux/register-dimensions owner opts "init-list" #(handle-init-drag % app owner opts)))

     om/IDidUpdate
    (did-update [_ _ prev-state _]
               (ux/update-bounds owner prev-state opts "init-list"))

    om/IWillUnmount
    (will-unmount [_]
                  (ux/unregister-dimensions owner opts "init-list"))

    om/IRenderState
    (render-state [_ state]
            (dom/div #js {:id "init-list" :ref "init-list" }
                     (dom/ul  nil
                              (dom/li #js { :className "round_marker"} (str "Current Round:" current-round))
                              (apply dom/ul #js {:className "sortable" :ref "sortable"}
                                     (map
                                      (fn [actor]
                                        (if-not (= actor ::spacer)
                                          (om/build act/actor-init-item actor {:opts opts :key :id} )
                                          (sortable-spacer (second (:cell-dimensions state)))))
                                      (sorting-state (util/init-list actors current-init current-order current-round) owner))))))

    ))

(defn initpane [{:keys [actors current-init current-order current-round] :as app} owner opts]
  (reify
    om/IRenderState
    (render-state [_ state]
                  (dom/div #js {:id "init-pane" :ref "init-pane"}
                           ; (dom/div #js {:id "drag-pane" } "opts=" (str  state) )
                           (om/build init-list app
                                     {:opts opts
                                      :init-state  {  :cell-dimensions [180 73]}
                                      })
                           (om/build reserve-list app
                                     {:opts  opts}
                                     )))


    ))
