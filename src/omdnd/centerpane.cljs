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
            [sablono.core :as html :refer [html] :include-macros true]
            )



)



(defn centerpane [app owner opts]
  (om/component
     (dom/div #js { :id "center-pane"})


   )

  )