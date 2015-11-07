(ns ^:figwheel-always rmfu-ui.core
  (:require
    [ajax.core :refer [POST GET PUT]]
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [reagent.validation :as validation]
    [rmfu-ui.createarticle :refer [createarticle]]
    [rmfu-ui.profile :refer [profile]]
    [secretary.core :as secretary :include-macros true])
  (:import goog.History))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce form-state (atom {:show-loading false
                           :signing-up   false}))

(defonce API-END-POINT "")

;; -------------------------
;; HTTP Request

(defn post-sign-in [profile]
  (let [{:keys [email password]} profile]
    (POST (str API-END-POINT "/signin")
          ;; TODO: validate these fields
          {:params        {:email    email
                           :password password}
           :format        :json
           :error-handler #(js/alert %)
           :handler       (fn [res]
                            (do
                              ;; (swap! form-state assoc :show-loading (not (:show-loading @form-state)))
                              (println "res:" res)
                              (js/alert res))
                            )})))

(defn post-sign-up [profile]
  (let [{:keys [username password email]} profile]
    ;(swap! form-state assoc :show-loading (not (:show-loading @form-state)))
    (POST (str API-END-POINT "/signup")
          ;; TODO: validate these fields
          {:params        {:username username
                           :password password
                           :email    email}
           :format        :json
           :error-handler #(js/alert %)
           :handler       (fn [res]
                            (do
                              ;(swap! form-state assoc :show-loading (not (:show-loading @form-state)))
                              (println "res:" res)
                              (js/alert res)
                              (secretary/dispatch! "/"))
                            )})))

(defn request-password-reset [profile]
  (GET (str API-END-POINT "/send-reset-password-email")
       ;; TODO: validate these fields
       {:params        {:email (:email profile)}
        :error-handler #(js/alert %)
        :handler       (fn [res]
                         (do
                           (println "res:" res)
                           (js/alert res))
                         )}))

(defn update-password [profile]
  (PUT (str API-END-POINT "/reset-password-from-form")
       ;; TODO: validate these fields
       {:params        {:email        (:email profile)
                        :new-password (:password profile)}
        :format        :json
        :error-handler #(js/alert %)
        :handler       (fn [res]
                         (do
                           (println "res:" res)
                           (.replaceState js/history #js {} "welcome" "/")
                           (js/alert res))
                         )}))

;; -------------------------
;; Utility functions

;; (add-watch form-state :logger #(-> %4 clj->js js/console.log))

(defn- is-valid-signup-data
  "Checks if the email, username, and password are valid for signing up a new user"
  [{:keys [email username password]}]
  (and (not-any? empty? [email username password])
       (validation/min-length? password 8)
       (validation/has-value? username)
       (validation/is-email? email)
       (validation/has-value? password)))

(defn sign-in [profile]
  (let [{:keys [email password]} profile]
    (if-not (or (empty? email) (empty? password))
      (if (validation/is-email? email)
        (post-sign-in profile)
        (js/alert "Invalid Email")))))

(defn sign-up [profile]
  (if (is-valid-signup-data profile)
      (post-sign-up profile)
      (js/alert "Invalid Credentials")))

(defn reset-password-email [profile]
  (if-not (empty? (:email profile))
      (if (validation/is-email? (:email profile))
        (request-password-reset profile)
        (js/alert "Invalid Email")
      )))

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
             :placeholder "8 or more characters"
             :on-change   #(swap! profile assoc :password (-> % .-target .-value))
             :on-key-down detect-key}]))

(defn username-input-field [profile]
  [:input {:type        "text"
           :className   "form-control"
           :value       (@profile :username)
           ;:on-blur     #(sign-in @profile)
           :placeholder "username"
           :on-change   #(swap! profile assoc :username (-> % .-target .-value))}])

(defn welcome-component-wrapper
  "Renders element passed in inside a .jumbotron"
  [element]
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
    [welcome-component-wrapper
     [:div.form-group {:style {:padding "1em"}}
      [:p.text-center.bg-primary "Sign in"]
      [:label "email:"]
      [email-input-field profile]
      [:label "password:"]
      [passsword-input-field profile]
      [:br]
      [:div.checkbox
       [:label
        [:input {:type "checkbox"}] "remember me?"]
       [:p.pull-right
        [:button.btn.btn-sm {:type     "button"
                             :on-click #(secretary/dispatch! "/reset-password")}
         "forgot password?"]]]
      [:br]
      [:button.btn.btn-default {:type     "button"
                                :on-click (fn [e]
                                            (sign-in @profile)
                                            (.preventDefault e))
                                } "sign-in"]

      [:button.btn.btn-default.pull-right {:type     "button"
                                           :on-click #(secretary/dispatch! "/sign-up")
                                           } "sign-up"]]]))

(defn sign-up-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    [welcome-component-wrapper
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
                                           } "sign-up"]]]))

(defn reset-password-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    [welcome-component-wrapper
     [:div.form-group {:style {:padding "1em"}}
      [:p.text-center.bg-primary "Reset Your Password"]
      [:label "email:"]
      [email-input-field profile]
      [:br]
      [:p [:button.btn.btn-default {:type     "button"
                                    :on-click (fn [e]
                                                (reset-password-email @profile)
                                                (.preventDefault e))
                                    } "reset"]]]]))

(defn new-password-component []
  (let [profile (atom {:username ""
                       :email    ""
                       :password ""})]
    (do
      (swap! profile assoc :email (session/get :email))
      (welcome-component-wrapper
        [:div.form-group {:style {:padding "1em"}}
         [:p.text-center.bg-primary "New Password"]
         [:label "password:"]
         [passsword-input-field profile]
         [:br]
         [:button.btn.btn-default {:type     "button"
                                   :on-click (fn [e]
                                               (if (or (empty? (:password @profile)) (not (validation/has-value? (:password @profile))))
                                                 (js/alert "Invalid Password")
                                                 (update-password @profile))
                                               (.preventDefault e))
                                   } "reset"]]))))

(defn show-loading-component []
  [:p.ball-loader.text-center {:style {:left   180
                                       :bottom 69}}])
(defn email-verified-component []
  [welcome-component-wrapper
   [:div
    [:h3.text-center
     [:u "Your email has been verified"]
     [:br]
     [:p [:a {:on-click #(secretary/dispatch! "/")} "you may now proceed to login"]]]]])

;; -------------------------
;; Routes

(defn current-page []
  [:div [(session/get :current-page)]])

(secretary/defroute "/" []
                    (session/put! :current-page #'sing-in-component))

(secretary/defroute "/sign-up" []
                    (session/put! :current-page #'sign-up-component))

(secretary/defroute "/email-verified" []
                    (session/put! :current-page #'email-verified-component))

(secretary/defroute "/reset-password" []
                    (session/put! :current-page #'reset-password-component))

(secretary/defroute "/new-password" [query-params]
                    (session/put! :email (:email query-params))
                    (session/put! :current-page #'new-password-component))

(secretary/defroute "/profile" []
                    (session/put! :current-page #'profile))

(secretary/defroute "/createarticle" []
                    (session/put! :current-page #'createarticle))

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
  (reagent/render-component [current-page]
                            (.getElementById js/document "app")))

(init!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
