(ns ^:figwheel-always rmfu-ui.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    [ajax.core :refer [POST]])
  (:import goog.History))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce form-state (atom {:show-loading false
                           :signing-up   false}))

;; -------------------------
;; HTTP Request

(defn post-sign-in [profile]
  (let [{:keys [email password]} profile]
    (POST "http://localhost:3000/signin"
          ;; TODO: validate these fields
          {:params  {:email    email
                     :password password}
           :format  :json
           ;; :response-format :json
           ;; :keywords? true
           :handler (fn [res]
                      (do
                        ;; (swap! form-state assoc :show-loading (not (:show-loading @form-state)))
                        (println "res:" res)
                        (js/alert res))
                      )})))

(defn post-sign-up [profile]
  (let [{:keys [username password email]} profile]
    ;(swap! form-state assoc :show-loading (not (:show-loading @form-state)))
    (POST "http://localhost:3000/signup"
          ;; TODO: validate these fields
          {:params  {:username username
                     :password password
                     :email    email}
           :format  :json
           ;; :response-format :json
           ;; :keywords? true
           :handler (fn [res]
                      (do
                        ;(swap! form-state assoc :show-loading (not (:show-loading @form-state)))
                        (println "res:" res)
                        (js/alert res))
                      )})))

(defn post-reset-password [profile]
  (POST "http://localhost:3000/send-reset-password-email"
       ;; TODO: validate these fields
       {:params  {:email (:email profile)}
        :format  :json
        ;; :response-format :json
        ;; :keywords?
        :handler (fn [res]
                   (do
                     ;(swap! form-state assoc :show-loading (not (:show-loading @form-state)))
                     (println "res:" res)
                     (js/alert res))
                   )}))

;; -------------------------
;; Utility functions

(defn sign-in [profile]
  (let [{:keys [email password]} profile]
    (swap! form-state assoc :signing-up false)
    (if (not (or (empty? email) (empty? password)))
      (post-sign-in profile))))

(defn sign-up [profile]
  (let [{:keys [email username password]} profile]
    (if (not (or (empty? email) (empty? username) (empty? password)))
      (post-sign-up profile))))

(defn reset-password [profile]
  (if (not (empty? (:email profile)))
    (post-reset-password profile)))

;; -------------------------
;; <Components/>


(defn email-input-field [profile]
  (let [detect-key #(case (.-which %)
                     13 (sign-in @profile)
                     27 (swap! profile assoc :email "")
                     nil)]
    [:input {:type        "email"
             :className   "form-control"
             :value       (:email @profile)
             :placeholder "me@mail.net"
             :on-change   #(swap! profile assoc :email (-> % .-target .-value))
             :on-key-down detect-key}])
  )

(defn passsword-input-field [profile]
  (let [detect-key #(case (.-which %)
                     13 (sign-in @profile)
                     27 (swap! profile assoc :password "")
                     nil)]
    [:input {:type        "password"
             :className   "form-control"
             :value       (:password @profile)
             ;:on-blur     #(sign-in @profile)
             :placeholder "*********"
             :on-change   #(swap! profile assoc :password (-> % .-target .-value))
             :on-key-down detect-key}]))

(defn username-input-field [profile]
  [:input {:type        "text"
           :className   "form-control"
           :value       (@profile :username)
           ;:on-blur     #(sign-in @profile)
           :placeholder "username"
           :on-change   #(swap! profile assoc :username (-> % .-target .-value))}])

;; (add-watch form-state :logger #(-> %4 clj->js js/console.log))

(defn sign-in-sign-up-base-component-wrapper [element]
  "Renders element passed in inside a .jumbotron"
  [:div.container.jumbotron
   [:div.row
    [:div.col-lg-12
     [:p.text-center "welcome to"]
     [:h1.text-center
      [:a {:on-click #(secretary/dispatch! "/")
           :style    {:color     "dimgray"
                      :font-size "1.15em"}} "FEED"]]
     [:hr]
     [:h4.text-center [:small "☴"]]
     [:h4.text-center
      [:small [:em "by "]]
      [:a {:href "http://www.rmfu.org/" :target "blank"}
       [:small [:strong "Rocky Mountain Farmers Union"]]]]
     [:p.text-center "and " [:a {:href "http://www.codefordenver.org/" :target "blank"}
                             [:strong "Code For Denver"]]] element]]])

(defn sing-in-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    [:div.form-group {:style {:padding "1em"}}
     [:p.text-center.bg-primary "Sign in"]
     [:label "email:"]
     [email-input-field profile]
     [:label "password:"]
     [passsword-input-field profile]
     [:br]
     [:div.checkbox
      [:label
       [:input {:type "checkbox"}] "remember me?"]]

     [:br]
     [:button.btn.btn-default {:type     "button"
                               :on-click (fn [e]
                                           (sign-in @profile)
                                           (.preventDefault e))
                               } "sign-in"]

     [:button.btn.btn-default.pull-right {:type     "button"
                                          :on-click #(secretary/dispatch! "/sign-up")
                                          } "sign-up"]
     [:br]
     [:p.text-center
      [:button.btn.btn-sm {:type     "button"
                           :on-click #(secretary/dispatch! "/reset-password")}
       "forgot password?"]]]))


(defn sign-up-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    [:div.form-group {:style {:padding "1em"}}

     [:p.text-center.bg-primary "Create Account"]

     [:label "username:"]
     [username-input-field profile]
     [:label "email:"]
     [email-input-field profile]
     [:label "password:"]
     [passsword-input-field profile]

     [:br]

     [:button.btn.btn-default {:type     "button"
                               :on-click #(secretary/dispatch! "/")
                               } "sign-in"]

     [:button.btn.btn-default.pull-right {:type     "button"
                                          :on-click (fn [e]
                                                      ;; (swap! form-state assoc :signing-up true)
                                                      (sign-up @profile)
                                                      (.preventDefault e))
                                          } "sign-up"]]))
(defn reset-password-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    [:div.form-group {:style {:padding "1em"}}
     [:p.text-center.bg-primary "Reset Your Password"]
     [:label "email:"]
     [email-input-field profile]

     [:br]

     [:p.text-center
      [:button.btn.btn-default {:type     "button"
                                :on-click (fn [e]
                                            (reset-password @profile)
                                            (.preventDefault e))
                                } "reset"]]]))

(defn reset-email-component-wrapper []
  (sign-in-sign-up-base-component-wrapper [reset-password-component]))

(defn show-loading-component []
  [:p.ball-loader.text-center {:style {:left   180
                                       :bottom 69}}])
(defn sign-in-component-wrapper []
  (let []
    [sign-in-sign-up-base-component-wrapper [sing-in-component]]))

(defn sign-up-component-wrapper []
  (let []
    [sign-in-sign-up-base-component-wrapper [sign-up-component]]))

(defn email-verified-component []
  [:div [:h1 "email verified"]])
;; -------------------------
;; Routes

(defn current-page []
  [:div [(session/get :current-page)]])

(secretary/defroute "/" []
                    (session/put! :current-page #'sign-in-component-wrapper))


(secretary/defroute "/sign-up" []
                    (session/put! :current-page #'sign-up-component-wrapper))

(secretary/defroute "/email-verified" []
                    (session/put! :current-page #'email-verified-component))

(secretary/defroute "/reset-password" []
                    (session/put! :current-page #'reset-email-component-wrapper))

;; -------------------------
;; History
;; must be called after routes have been defined

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))



;; -------------------------
;; Bootstrap app

(defn init! []
  (secretary/set-config! :prefix "#")
  (hook-browser-navigation!)
  (reagent/render-component [current-page form-state]
                            (.getElementById js/document "app")))

(init!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

