;;; TODO - drag-exit events : Right now, a component doesn't receive events for other components
;;; If a component responds to drag-over, it won't "revert" state until the drag ends.
(ns omdnd.ux
  (:require-macros
           [cljs.core.async.macros :refer [go alt!]]
   )
  (:require
           [cljs.core.async :refer [put! <! chan close! tap untap mult filter>]]
           [om.core :as om]
           [goog.events :as events]
           [goog.style :as gstyle])
  (:import
           [goog.events EventType])
  )


(enable-console-print!)
;;;;;
;
;  Om Property state transition detection
;

(defn to? [owner next-props next-state k]
  (or (and (not (om/get-state owner k))
           (k next-state))
      (and (not (k (om/get-props owner)))
           (k next-props))))

(defn from? [owner next-props next-state k]
  (or (and (om/get-state owner k)
           (not (k next-state)))
      (and (k (om/get-props owner))
           (not (k next-props)))))




;;; Convenience functions

(defn dragging? [item owner]
  (or (:dragging item)
      (om/get-pending-state owner :dragging)))

(defn mouse-down? [item owner]
  (or (:mouse-down item)
      (om/get-pending-state owner :mouse-down)))

(defn floor [n]
  (let [num (js/Number. n)]
    (- (.toFixed num 0) 0)
  ))

(defn gsize->vec [size]
  [(floor (.-width size)) (floor (.-height size))])

(defn element-offset [el]
  (let [offset (gstyle/getPageOffset el)]
    [(floor (.-x offset))  (floor (.-y offset))]))



(defn location [e]
  [(floor (.-clientX e)) (floor (.-clientY e))])


;;;;;

;  Communication Channels
(defn add-chans [state opts]
  (assoc opts :chans (:chans state) ))
(defn drag-evts [opts]
  (:drag-evts (:chans opts)) )
(defn dims-chan  [opts]
  (:dims-chan (:chans opts)) )
(defn command-chan  [opts]
  (:command-chan  (:chans opts)) )
(defn disperser  [opts]
  (:disperser  (:chans opts)))


;; TODO: remove this example handler
(defn handle-drag-event [ {:keys [event drag-item] :as e} app  owner]
  (let [id (om/read drag-item :id)]
    (om/set-state! owner :drag-event e)
    (case event
      :drop  (om/update! app assoc :messages (str  id  " > " event ))
      :drag-start  (om/update! app assoc :messages (str   id " > " event))
      :dragging nil
      )
  ))


(defn setup-comms [app owner opts]
  (let [drag-evts (chan)
        dims-chan (chan)
        command-chan (chan)
        disperser (mult drag-evts)
        handle-command (:command-handler opts)
        ]
    (om/set-state! owner :chans { :drag-evts  drag-evts
                                  :dims-chan  dims-chan
                                  :disperser disperser
                                  :command-chan command-chan } )

    (go (while true
          (alt!
           ; drag-evts ([e c] (handle-drag-event app  e owner c ))
           ; dims-chan ([e c] (add-child-dimensions app  e owner))
           command-chan ([e c] (handle-command e app owner))
           )))))




;;;;;;;;;;;;
;  Dimension handlers


(defn bound-filter  [owner]
  (fn [evt]
    (let [ [[t l] [b r]] (om/get-state owner :bounds)
          [x y] (:location evt)
           type (:event evt)
           ]
      (or (= type :drag-end)
          (and (> x t)
               (< x b)
               (> y l)
               (< y r))))))

(defn bounds [container]
  (let [ [w h] (-> container gstyle/getSize  gsize->vec)
         [x y] (element-offset container) ]
    [[x y]  [(+ x w) (+ y h)]]
    ))


(defn directed-event-chan [owner]
  (filter>
   (bound-filter owner)
   (chan)))

(defn create-listener [src-mult owner f]
  (let [drag-target-chan  (directed-event-chan owner)]
    (tap src-mult drag-target-chan)
    (go (while
          (when-let [event (<! drag-target-chan)]
            (f event)
            event
            )))
    drag-target-chan
    ))
;;
;; Sub-components call these to register with the ux controller

(defn update-bounds [owner prev-state opts ref-node]
  (when-let  [container (om/get-node owner ref-node)]
    (let [ new-bounds (bounds container)]
     (when-not (= (:bounds prev-state) new-bounds)
      (om/set-state! owner :bounds  new-bounds)))))


;;
;; Sub-components call this on mounting, to send drag

(defn register-dimensions [owner opts ref-node & [event-handler]]
  (when-let [container (om/get-node owner ref-node)]
    (let [ dims (-> container gstyle/getSize  gsize->vec)
           dims-chan (dims-chan  opts)
           disperser (disperser opts)
           new-bounds (bounds container)
           ]

      (om/set-state! owner :dimensions dims)
      (om/set-state! owner :bounds  new-bounds)
      (when event-handler
        (om/set-state! owner :drag-target-chan  (create-listener disperser owner event-handler)))
      (put! dims-chan {:component ref-node
                       :dims dims
                       :bounds new-bounds
                       })
      )))

(defn unregister-dimensions [owner opts ref-node]
 (let [state (om/get-state! owner)
          listener (:drag-target-chan state)
          source   (disperser opts)]
      (untap disperser listener)
      (close! listener)
      (put! dims-chan {:component ref-node
                      ; :bounding-chan drag-target-chan
                       :dims nil
                       })

      ))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Drag handlers


(defn drag-start [e actor owner opts]
  (when-not (dragging? actor owner)
    (let [el (om/get-node owner "drag-container")
          drag-start (location e)
          el-offset (element-offset el)
          drag-offset (vec (map - el-offset drag-start))
          drag-evts (drag-evts opts) ]
      ;; if in a sortable need to wait for sortable to
      ;; initiate dragging

      (doto owner
        (om/set-state! :location el-offset)
        (om/set-state! :dragging (:id actor))
        (om/set-state! :drag-offset drag-offset))

        (put!  drag-evts
          {:event :drag-start
           :drag-item actor
           :location (vec (map + drag-start drag-offset))}))))



(defn drag-end [e actor owner opts]
  (when (dragging? actor owner)
    (when (om/get-state owner :dragging)
      (om/set-state! owner :dragging false))
    (let [drag-evts (drag-evts opts)
          el (om/get-node owner "drag-container")
          drag-start (location e)
          el-offset (element-offset el)
          drag-offset (vec (map - el-offset drag-start))]
      (doto owner
        (om/set-state! :location nil)
        (om/set-state! :drag-offset nil)
        )
      (put! drag-evts
            {:event :drop
             :drag-item actor
             :location (vec (map + drag-start drag-offset))})
      (put! drag-evts
            {:event :drag-end })
      ))
   )


(defn drag [e actor owner opts]
  (when (dragging? actor owner)
      (let [state (om/get-state owner)
            drag-evts (drag-evts opts)
            loc   (vec (map + (location e) (:drag-offset state)))]
        (.preventDefault e)
        (om/set-state! owner :location loc)

        (put! drag-evts
              {:event :dragging
               :drag-item actor
               :location  loc}))))



(defn  drag-listeners [owner next-props next-state opts]
  (when (or (to? owner next-props next-state :dragging))
        (let [mouse-up   (om/bind drag-end next-props owner opts)
              mouse-move (om/bind drag next-props owner opts)]
          (om/set-state! owner :window-listeners
            [mouse-up mouse-move])
          (doto js/window
            (events/listen EventType.MOUSEUP mouse-up)
            (events/listen EventType.MOUSEMOVE mouse-move))))
      ;; end dragging, cleanup window event listeners
      (when (from? owner next-props next-state :dragging)
        (let [[mouse-up mouse-move]
              (om/get-state owner :window-listeners)]
          (doto js/window
            (events/unlisten EventType.MOUSEUP mouse-up)
            (events/unlisten EventType.MOUSEMOVE mouse-move)))))

;;;;;;;;;;;;;;;;;;;;;;;;;
