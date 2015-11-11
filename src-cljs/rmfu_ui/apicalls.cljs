(ns rmfu-ui.apicalls
  (:require [ajax.core :refer [POST GET PUT]]))

(defonce API-END-POINT "")

(defn get-article [id]
  (GET (str API-END-POINT "/article/" id)
       {:handler    (fn [res]
                      (do
                        (println "res:" res)
                        ))
        }))
