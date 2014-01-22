(ns omdnd.rightpane
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


(defn handle-drag [e app owner opts]
     (om/set-state! owner :right-pane-recvd e))


(defn messages [app owner opts]
  (om/component
     (dom/div #js { :id "messages"}
           (:messages app)


  )))

(defn rightpane [app owner opts]

  (reify

    om/IDidMount
    (did-mount [t node]
               (ux/register-dimensions owner opts "right-pane"  ));  (om/bind handle-drag app owner opts)))


    om/IDidUpdate
    (did-update [_ _ prev-state _]
                (ux/update-bounds owner prev-state opts "right-pane"))

    om/IRender
    (render  [_]
            (dom/div #js { :id "right-pane" :ref "right-pane"}
                     (om/build messages app {:opts opts})
                     (dom/div nil (str (om/get-state owner)))
                     )

            )))