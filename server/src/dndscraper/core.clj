	(ns dndscraper.core
	  (require [dndscraper.xml :as xml]
             [dndscraper.util :as util]
             [dndscraper.db :as db]
             [dndscraper.login :as login]
             [clojure.zip :as zip]
             [clojure.data.zip.xml :as cdzx]
            ))

	(def base-compendium "http://www.wizards.com/dndinsider/compendium/")
	(def base-search (str base-compendium "CompendiumSearch.asmx/ViewAll?tab="))

	(use 'clj-webdriver.taxi)

  ;
  ; (defn login []
  ;	  (println "logging in")
  ;	  (quick-fill-submit {"input#email", "email@email.com",
  ;	                      "input#password", "******",
  ; 	                      "#InsiderSignin", click }))


	(defn start-session
   ([] (start-session (new-driver {:browser :chrome})))
   ([d]
	 (set-driver! d)
   (to  (str base-compendium "monster.aspx?id=1"))
		(if (exists? "input#email")
		 (login/login)
		 (println "already logged in"))))


	(defn save-url-to-file [url file]
	  (clojure.java.io/make-parents file)
	  (spit file (str (page-source (to url)))))

	(defn wait-time [wait]
	  (+ wait (rand-int 555)))

	(defn save-page
	 ([type id ] (save-page type id 0))
	 ([type id wait]
	 (let [f (util/local-file type id)]
	   (if (not (.exists f))
	     (do (if (pos? wait) (Thread/sleep (wait-time wait)))
	       (save-url-to-file (str base-compendium type ".aspx?id=" id) f))
	     (println "skipping existing file")
      ))))

	(defn save-all-by-type [type wait]
	  (for [i ( xml/all-ids type ) ]
	    (save-page  type i wait )))


 (defn create-compendium-list [type d]
  (let [destiny-hrefs
        (map (fn [e] (attribute e :href))
             (elements (str "a[href*=" (.toLowerCase type) "]" )))]
    (dorun destiny-hrefs)
  (if (exists? {:tag :a :text "Next"})
    (do (click {:tag :a :text "Next"})
      (into d (create-compendium-list type destiny-hrefs)))
  (into d destiny-hrefs)
  )))

(defn scrape-compendium [type]
  (to  base-compendium)
  (select (str "option[value=" type "]"))
  (click "#searchbutton")
  (wait-until #(exists? ".resultsTable table") 5000 20)
  (create-compendium-list type [])
  )


 (defn scrape-all-by-type [type wait]
	  (for [i (scrape-compendium type ) ]
	    (save-page type (util/parse-int i) 0 )))

 (defn retrieve-types-to-local-files []
   (for [i (util/local-type-list)]
     (spit  (str "resources/" i ".xml")  (slurp (str base-search i))  )))

 (defn update-from-local-xml [wait]
   (for [i (util/local-type-list)]
     (save-all-by-type i wait)))


 (defn sanitize-item [item-name]
   (.trim (re-find #"[a-zA-Z' ]+" item-name)))



 (defn cache-result-to-db [found builder-link fixed-link]
   (let [url builder-link
         fixed  (if found builder-link fixed-link)]
     (db/set_valid_pair url fixed)
     fixed
     ))

 (defn fix-link  [item-name builder-link]
   (let [ cached  (db/get_valid builder-link) ]
     (if-not (nil? cached)
       cached
       (if-let [id (xml/id-for-name (sanitize-item item-name) "item") ]
	        (let [linked-page (str (page-source (to builder-link)))
	              found (> (.indexOf linked-page item-name) 0)
	              fixed-link  (str base-compendium  "item.aspx?id=" id)]
	        (cache-result-to-db found builder-link fixed-link))
         builder-link))))




