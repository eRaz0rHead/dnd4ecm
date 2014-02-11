(ns dice-parser.core
  (:require
    [instaparse.core :as insta]
    [clojure.walk]
    [clojure.edn])
  )

(defn die [num rng]
  (repeatedly num #(+ 1 (rand-int rng))))

(defn die-with-reroll [num rng rr]
  {:pre [(< rr rng)]}
  "rolls num dice of size rng , rerolling values less than rr"
  (take num  (filter #(> % rr) (repeatedly  #(+ 1 (rand-int rng))) )))


(defn total [d-map]
  (if (number? d-map) d-map
        (:= d-map)))

(defn d
  ([num rng] (d num rng 0))
  ([num rng rr]
    (let [reroll? (> rr 0)
          rolls (if reroll? (die-with-reroll  num rng rr) (die num rng))]
      {:dice  (str num "d" rng)
       :rolls rolls
       := (reduce + rolls)}
       )))


(defn sum-d
  ([l]  (sum-d 1 l))
  ([num l]
    (total (apply d num l))))


(defn rng
  ([x] (list (clojure.edn/read-string x)))
  ([x y] (list (clojure.edn/read-string x) y)))



; /*********************************************************/


(def dice-grammar
    "dkd = dice [ keep | drop ]
     dice = [number] die
     die = ( <'d'>number| weapon ) [reroll]
     weapon = <'[w]'> | <'[W]'>
     keep = <'k'>#'[0-9]+'
     drop = <'d'>#'[0-9]+'
     reroll= <'r'>#'[0-9]+'
     number = #'[0-9]+'
     ws = #'[\\s,.]*'"
    )

(def dice-expr-grammar
   "<dice-expr> = add-sub
     <add-sub> = mul-div | add | sub
     add = add-sub <[ws]'+'[ws]> mul-div
     sub = add-sub <[ws]'-'[ws]> mul-div
     <mul-div> = term | mul | div
     mul = mul-div <[ws]'*'[ws]> term
     div = mul-div <[ws]'/'[ws]> term
     <term> = dkd | number | <[ws]'('[ws]> add-sub <[ws]')'[ws]>
     ")

(def damage-type-grammar
  " total =  damage-expr  ( <[ws] ('+'|'plus' |'and') [ws]> damage-expr )*
    damage-expr = dice-expr <[ws]> [damage] [ crit ]
    damage = (type)* <['damage'] [ws]>
    crit = <[ws]'(' 'crit'[ws] > damage-expr <[ws] ')' [ws]>
    <type> =  <[ws]> ('acid' | 'force' | 'necrotic'| 'psychic' | 'radiant' | 'cold' | 'fire' | 'lightning' | 'thunder' | 'disease' | 'poison' ) <[ws] ['and']>
  ")

(def dice-calc-parse
  (insta/parser
    (str
     damage-type-grammar
     dice-expr-grammar
     dice-grammar
    )
    :output-format :hiccup))



;/******************************************************************/


(def d-parse
  (insta/parser
    dice-grammar
    :output-format :hiccup))


(defn apply-to-dice
  [sym f d1 d2]
  (let [left (if (map? d1) d1 {:= d1})
        right (if (map? d2) d2 {:= d2})]
    {
     sym (list left right),
     := (apply f (list (total d1) (total d2)))
     }
    ))





(defn d-calc
  ([ds] (d-calc 1 ds))
  ([num ds]
    (let [die-exp (:die ds)
          die   (first die-exp)
          {:keys [rr] :or {rr 0}} (second die-exp)]
      (d num die rr))))

(defn keep-or-drop
  ([dice] dice)
  ([dice kd]
    (let [num  (first (vals kd))
          sortfn  (if (:keep kd) > <)
          headfn  (if (:keep kd) take drop)
          rolls   (headfn num (sort sortfn (:rolls dice)))
          new-expr (str (:dice dice) (if (:keep kd) "k" "d") num)  ]
      (assoc dice :rolls rolls :dice new-expr  := (reduce + rolls)))))


(defn d-tree [expr]
  (let [m {}]
    (->> (d-parse expr)
      (insta/transform
        {
         :dkd keep-or-drop
         :dice d-calc
         :die (fn [& args] (assoc {} :die args))
         :reroll  #(assoc m :rr (clojure.edn/read-string %))
         :keep  #(assoc m :keep (clojure.edn/read-string %))
         :drop  #(assoc m :drop (clojure.edn/read-string %))
         :number clojure.edn/read-string
          }))))




(defn dice-tree [expr]
  (->> (dice-calc-parse expr)
    (insta/transform
      {

       ; :total (fn [& args]  (group-by :type (seq args) ))
       :add (fn [d1 d2] (apply-to-dice :add + d1 d2))
       :sub (fn [d1 d2] (apply-to-dice :sub - d1 d2))
       :mul (fn [d1 d2] (apply-to-dice :mul * d1 d2))
       :div (fn [d1 d2] (apply-to-dice :div / d1 d2))

       :range rng,
       ; TODO handle CRITS
       :damage-expr (fn ([dice damage]  { :expr dice :type damage }) ([dice]  { :expr dice :type "untyped" }))
       :damage (fn ([] "untyped") ([d] d))
       :dkd keep-or-drop
       :dice d-calc
       :die (fn [& args] { :die args })
       :reroll  #(assoc {} :rr (clojure.edn/read-string %))
       :keep  #(assoc {} :keep (clojure.edn/read-string %))
       :drop  #(assoc {} :drop (clojure.edn/read-string %))
       :number clojure.edn/read-string

       }
      )))



;  (map #( total (:expr %)) (vals (group-by :type (dice-tree "2d6+3  plus 3 fire"))))




