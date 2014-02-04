(ns dndscraper.db)

(use 'korma.db)
(require '[clojure.string :as str])


(def db (h2 {:db "~/Programming/workspace/dndscraper/resources/db/compendium"
             :user "sa"
             :password ""
             :naming {:keys str/lower-case
                      ;; set map keys to lower
                      :fields str/upper-case}}))
(defdb comp-db db)

(use 'korma.core)

(declare item power charclass feat valid_urls)

(defentity item
  (entity-fields :name :lvl :cost :type :subtypes :bonus :enhances :critical  :property :powers))

(defentity power
  (entity-fields :name :lvl :type :usage :kwords :action :target :range :source :description )
  (belongs-to charclass))

(defentity charclass
  (entity-fields :name :desc)
  (has-many power))

(defentity feat
  (entity-fields :name :tier :prereq :benefit))

(defentity validurls
  (pk :url)
  (entity-fields  :fixed))

(defn get_valid [url] 
  (let [result (select validurls 
                  (fields :fixed)
                  (where {:url url}))]
    (:fixed (first result))))
    

(defn set_valid_pair [url fixed]
 (insert validurls
  (values {:url url :fixed fixed})))