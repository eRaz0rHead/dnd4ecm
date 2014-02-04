(ns omdnd.modal
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   )
  (:require [goog.events :as events]
            [goog.debug :as debug]
            [cljs.core.async :refer [put! <! chan]]

            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]


            [omdnd.util :as util]
            [omdnd.ux :as ux]

            )

)

;;; EXPECTS :id (string) and :view (component) in "init-state"
(defn modal-dialog [app owner]
  (reify
    om/IRenderState
    (render-state [_ state]
                  (dom/div #js {:id (:id state)}
                           (dom/div #js {:className "modalDialog"}
                                    (dom/a #js { :title "Close" :className "close" } "X" )
                                    (om/build (:view state) app))))))



