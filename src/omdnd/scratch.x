function getOffsetRect(elem) {
    // (1)
    var box = elem.getBoundingClientRect()

    var body = document.body
    var docElem = document.documentElement

    // (2)
    var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop
    var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft

    // (3)
    var clientTop = docElem.clientTop || body.clientTop || 0
    var clientLeft = docElem.clientLeft || body.clientLeft || 0

    // (4)
    var top  = box.top +  scrollTop - clientTop
    var left = box.left + scrollLeft - clientLeft

    return { top: Math.round(top), left: Math.round(left) }
}


(defn offset-rect [elem]
  (let [box (.getBoundingClientRect elem)
        offset (gstyle/getPageOffset el)]
        ])

)


(defn insert-between [actor p n]
  ;PREREQ  :init p >= :init n
  ;Assumes p and n are adjacent .. have no intervening elements.
  (cond
   (nil? n)
   [ (merge actor {:init  (:init p)  :order 1 })  p ]
   ; update the ordering of all three elements
   (nil? p)
   [ (merge actor {:init (:init n) :order (inc (:order n))  })  n ]

   (= (:init p) (:init n))
   (let [order (inc (:order n))]
     [ (merge actor {:init (:init n) :order order }) (merge p {:order (inc order)}) n ])
   :else
   [ (merge actor {:init (:init n) :order (inc (:order n))}) p n ]
   ))


(swap! app-state update-in [:todos]
        (fn [todos]
          (map #(assoc-in % [:completed] not) todos))))









(let [drag-chan (chan)]
        (om/set-state! owner :drag-chan  drag-chan )
        (go (while true
              (let [e (<! drag-chan)]
                (handle-drag-event app e owner)
                )
              ))))




(go (while true
          (let [event (<! drag-target-chan)]
            (f event)
            )))



(def drag-target-chan (chan))



(defn dl [drag-target-chan]
  (go (while
        (let [event (<! drag-target-chan)]
          (prn event)
          event
        )))
        (prn "end go")
      )


(dl drag-target-chan)

(for [x (range 10)]
  (put! drag-target-chan (str "test" x))

)

(close! drag-target-chan)





(defn listen-bounds [owner state opts ref-node]
  (when-let  [container (om/get-node owner ref-node)]
    (let [ dims (-> container gstyle/getSize gsize->vec)
           dims-chan (dims-chan opts)
           disperser (disperser opts)
           drag-chan (:drag-target-chan state)
           new-bounds (bounds container)
           track-fn (fn [evt]
                       (om/set-state! owner :last-evt evt)
                       )
           ]


      (if (= (:bounds state) new-bounds)
        (do
          (if (nil? drag-chan)
            (create-listener disperser new-bounds track-fn)
            drag-chan)
          )
        (do

          (when-not (nil? drag-chan)
            (untap disperser drag-chan)
            (close! drag-chan))

          (om/set-state! owner :bounds new-bounds)
          (let [new-drag-chan (create-listener disperser new-bounds track-fn)]
             (om/set-state! owner :drag-target-chan new-drag-chan)
            new-drag-chan)

          )))))
