(ns dndscraper.powers
 (:require [net.cgrand.enlive-html :as e]
           [instaparse.core :as insta]
           [dndscraper.html :as dh]
           [dndscraper.util :as util]))


 (defn extract-attacks [s]
   (re-seq #": (\w+) vs. (\w+)" s))


 (defn  ^:private  power-header- [ id]
   (apply :content (e/select (dh/detail "power" id) [:h1 ])))


 (defn  ^:private  power-level- [ id]
   (apply :content (e/select (dh/detail "power" id) [:h1 :> :span.level])))

 (defn parse-power [id]
   {:level (power-level- id)})


 (defn power-contents [id]
    (e/select (dh/detail "power" id) [:p]))


 (defn convert-power [node]
   (str (util/compress-whitespace
          (reduce str
            (e/emit*
              (flatten
                (map :content
                     (e/at node
                           [:i] nil; (fn [t]  [(assoc t :tag :flavor )])
                           [:nbsp] nil
                           [:b] (fn [t]  [(assoc t :tag :kw )])
                           [:img] nil
                           [:.publishedIn] nil
                           ))))))
        "\nTERMINATOR"))


 (def parser (insta/parser
  "power = <separator*> usage keyword* <[br]>
            action range
            [trigger] [requirement]
            [target]
            [special]
            (effect | attack-group)
            effect*
            [special]
            <TERM>
    <br> = '<br />'
    usage= <kws> ('Daily' | 'Encounter' | 'At-Will' ) [' (Special)'] <kwe>  <separator*>
    keyword = <kws> #'\\w+' <kwe> <separator*>
    action = <kws> ('Standard Action' | 'Move Action' | 'Minor Action' |
                  'Immediate Interrupt' | 'Immediate Reaction' |
                  'Free Action' | 'Opportunity Action' ) <kwe> <separator*>
    range = <kws>ranges [' or ' ranges]<kwe>   <separator*> [text]
    <ranges> = ('Melee' | 'Ranged' | 'Close' | 'Personal' | 'Area' )

    trigger = <kws  'Trigger'  kwe> [<': '>text]
    requirement = <kws  'Requirement'  kwe> [<': '>text]
    effect = <kws  'Effect'  kwe> [<': '>text]
    special = <kws  'Special'  kwe> [<': '>text]
    target = <kws  'Target'  kwe> [<': '>text]

    <attack-group> = attack hit [miss]
    attack = <kws  'Attack' kwe> <': '> text
    hit = <kws 'Hit'  kwe> <': '> text
    miss = <kws  'Miss'  kwe> <': '> text
    <text> = #'[^<\\n]*' | text br text
    <kws> = '<kw>'
    <kwe> = '</kw>'
    <separator>= #'[,\\s]+'
    <TERM> = '\\nTERMINATOR'"
               ))
 ;; TODO:  ADD SUPPORT FOR Secondary Attacks under hit line

 ; (parser (convert-power (power-contents 13024)))
 ; (convert-power (power-contents 15913))

(def damage-and-effects-grammar
    "expr   = dice <ws> damage
     dice = [number] die
     die = ( <'d'>number| weapon )
     weapon = <'[w]'> | <'[W]'>
     damage =   [type <ws>]  <'damage'> [ (<ws> plus <ws> expr) | <ws> ]
     type= ('cold' | 'fire' | 'psychic' )   [<ws> ('and' | 'or') <ws> type]
     plus =  <('+' | 'plus' | 'and') >
     <number> = #'[0-9]+'
     <ws> = #'[,.\\s]+'
 ")

(def damage-parser
  (insta/parser damage-and-effects-grammar ))

