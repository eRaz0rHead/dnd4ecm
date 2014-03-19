(ns dnd.formats.c-builder
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [net.cgrand.enlive-html :as e]

            [dndscraper.xml :as x]
            [clojure.pprint :as pp]
            [dnd.data.combatant :as cbt]

            )
  (:use [clojure.data.zip.xml]
        [clojure.string :only [split]]))



