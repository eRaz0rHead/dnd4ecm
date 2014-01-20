







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