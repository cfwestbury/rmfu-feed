(ns rmfu-ui.article
  (:require [rmfu-ui.nav :refer [nav]]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [PUT GET]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]))

(defn article []
  (let [app-state (atom {:title "Dummy Title" :text "Blah blah blah" :date "November 16, 2015"})

        fetch-article (fn [article-id] (GET (str "/article/" article-id)
                                       {
                                        :error-handler   #(secretary/dispatch! "/")
                                        :response-format :json
                                        :keywords?       true
                                        :handler         (fn [res]
                                                           (reset! app-state res)
                                                         )}))
       ]
        

  (reagent/create-class
    {
     :component-will-mount #(fetch-article (session/get :article-id))
     :reagent-render (fn []
          [:div
             [nav]
             [:div.container.jumbotron.largemain
              [:div.row
               [:div.col-lg-12
                [:h1.text-center
                 (:title @app-state)]
                [:div.pre-scrollable.borderbox
                 [:div.dategroup
                  [:h4.articleheading
                   (:subtitle @app-state)]
                  [:p.date
                   (:date @app-state)]]
                 [:p (:article-text @app-state)]]
                [:br]
                [:div.greybox
                 [:ul.list-inline
                  [:li
                   [:button.btn.btn-default
                    {:type "button", :value "add"}
                    "Collapse"]]
                  [:li
                   [:button.btn.btn-default
                    {:type "button", :value "add"}
                    "Topics"]]
                  [:li
                   [:button.btn.btn-default
                    {:type "button", :value "add"}
                    "Comments"]]
                  [:li
                   [:button.btn.btn-default
                    {:type "button", :value "add"}
                    "Share"]]
                  [:li
                   [:button.btn.btn-default
                    {:type "button", :value "add"}
                    "Archive"]]]
                 [:ul.list-inline
                  [:li
                   [:a
                    {:href "#"}
                    "Fertilizer"]]
                  [:li
                   [:a
                    {:href "#"}
                    "Legislation"]]
                  [:li
                   [:a
                    {:href "#"}
                    "Vegetables"]]
                  [:li
                   [:a
                    {:href "#"}
                    "Grain"]]
                  [:li
                   [:a
                    {:href "#"}
                    "Fruit"]]
                  [:li
                   "80246 +5 Miles"]]
                 [:div.pre-scrollable.borderbox.height150
                  [:div.col-lg-3.date
                   "Oct 1, 2015"]
                  [:div.col-lg-9.bullet
                   "Joe - Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididul Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]
                  [:div.col-lg-3.date
                   "Oct 1, 2015"]
                  [:div.col-lg-9.bullet
                   "Bob - Consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et!"]
                  [:div.col-lg-3.date
                   "Oct 1, 2015"]
                  [:div.col-lg-9.bullet
                   "Bill - Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]
                  [:div.col-lg-3.date
                   "Oct 1, 2015"]
                  [:div.col-lg-9.bullet
                   "Roscoe - Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."]
                  [:div.col-lg-3.date
                   "Oct 1, 2015"]
                  [:div.col-lg-9.bullet
                   "Tiberius - Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."]]]]]]])
               
    })))


  
