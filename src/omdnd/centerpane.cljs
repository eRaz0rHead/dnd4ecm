(ns omdnd.centerpane
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
            )



)

(defn next-turn [e app owner opts]
  (when-let [command-chan (ux/command-chan opts)]
    (put! command-chan {:event :next-turn} )

  ))


(defn centerpane [app owner opts]
  (om/component
     (dom/div #js { :id "center-pane"}
              (dom/div #js {:className "turn-control"}
                       (dom/div #js {:type "button"  :className "undo"})
                       (dom/div #js {:className "redo"})
                       (dom/div #js {:className "turn-marker"}
                                "Start of Turn")
                       (dom/div #js {:className "action-button"
                                     :onClick (om/bind next-turn app owner opts)}
                                "Next Turn"
                                ))

              (dom/div #js {:className "instructions"}
              (dom/div #js {:className "hover-handle"})
              (dom/ul nil
                      (dom/li nil "Regeneration")
                      (dom/li nil "Ongoing")
                      (dom/li nil "Recharge")
                      )))))