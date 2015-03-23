(ns dice.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan <! >! timeout]]))

(def dice-state (atom {:number 3
                       :sides 6
                       :roll []
                       :show-top false
                       :show-side true
                       :show-about false
                       :play-audio true}))

(defn roll-die [sides]
  (+ (rand-int sides) 1))

(defn roll-dice [num-dice sides]
  (repeatedly num-dice #(roll-die sides)))

(defn get-sound [file]
  (let [audio (.createElement js/document "audio")]
    (aset audio "src" file)
    audio))

(def click-sound (get-sound "/audio/click.wav"))
(def roll-sound (get-sound "/audio/roll.wav"))

(defn play-sound! [sound]
  (if (:play-audio @dice-state)
    (do
      (.pause sound)
      (aset sound "currentTime" 0)
      (.play sound)
      true))
    false)

(defn sound-toggle [app owner]
  (om/component
    (dom/button #js {:className "sound-toggle"
                     :onClick #(om/update! app :play-audio (not (:play-audio app)))}
      (dom/span #js {:className (str "fa " (if (:play-audio app)
                                                            "fa-volume-up"
                                                            "fa-volume-off"))}))))

(defn about-toggle [app owner]
  (om/component
    (dom/button #js {:className "about-toggle"
                     :onClick #(om/update! app :show-about (not (:show-about app)))}
      (dom/span #js {:className (str "fa fa-info-circle")}))))

(defn add-show-class [class-name state]
  (str class-name
       (if (true? state) " show" "")))

(defn about-page [app owner]
  (om/component
    (dom/div #js {:className (add-show-class "about-page" (:show-about app))
                  :onClick #(om/update! app :show-about false)}
       (dom/h1 #js {:className "about-title"} "DICE")
       (dom/h2 #js {:className "about-subtitle"} "A simple dice-rolling app")
       (dom/div #js {:className "about-content"}
          (dom/p nil "DICE is designed to be a simple as possible. Select a number of dice,
                      the number of sides, and click Roll to roll some dice! You can also
                      click the row labels to hide them, and click the hamburger button
                      (that's the three little lines) to show the top bar,
                      and turn off the sound or view this page. But if you're reading this,
                      I guess you already figured that out.")
          (dom/p nil
                 (dom/span nil "The app was built, with love by Will Lee-Wagner.
                                It's written in ClojureScript with the om library, mostly to
                                learn the language. For more about the technology,
                                and to report any issues, see ")
                 (dom/a #js {:href "https://github.com/whenther/dice"
                             :target "_blank"} "github.com/whenther/dice")
                 (dom/span nil ". For more about my other projects, or if you want to chat,
                                see my ")
                 (dom/a #js {:href "http://whentheresawill.net"
                             :target "_blank"} "website")
                 (dom/span nil " or ")
                 (dom/a #js {:href "www.linkedin.com/in/willlw"
                             :target "_blank"} "LinkedIn")
                 (dom/span nil " profile!"))
          (dom/p nil "cc Will Lee-Wagner 2015, MIT License")))))

(defn hamburger-button [app owner]
  (om/component
    (dom/div #js {:className "hamburger-button"
                   :onClick #(if (not (:show-about app))
                                (om/update! app :show-top (not (:show-top app))))}
       (apply dom/div #js {:className "hamburger-container"}
         (map #(dom/div #js {:className "hamburger-bar"}), [1 2 3])))))

(defn top-bar [app owner]
  (om/component
    (dom/div #js {:className (add-show-class "top-bar"
                                             (:show-top app))}
      (dom/div #js {:className "top-bar-buttons"}
        (om/build about-toggle app)
        (om/build sound-toggle app))
      (dom/h3 #js {:className "title"} "DICE")
      (om/build hamburger-button app))))

(defn option-buton [state owner opts]
  (reify
    om/IRenderState
    (render-state [_ {:keys [choose]}]
      (dom/button #js {:className (str (:type opts)
                                       (if (= state (:option opts))
                                          " selected" "")
                                       " option-button")
                       :onClick  #(go
                                   (play-sound! click-sound)
                                   (>! choose (:option opts)))}
                  (dom/h2 nil (:option opts))))))

(defn option-label [app owner {:keys [:label]}]
  (om/component
    (dom/div #js {:className (add-show-class "option-label" (:show-side app))}
       (dom/h4 nil label)
       (dom/div #js {:className "option-label-click-zone"
                     :onClick #(om/update! app :show-side (not (:show-side app)))}))))

(defn option-bar [app owner opts type-name type-index choices]
  (reify
    om/IInitState
      (init-state [_]
        {:choose (chan)})
    om/IWillMount
      (will-mount [_]
        (let [choose (om/get-state owner :choose)]
          (go (loop []
            (let [new-number (<! choose)]
              (om/update! app type-index new-number)
              (recur))))))
    om/IRenderState
    (render-state [_ {:keys [choose]}]
      (dom/div opts
        (om/build option-label app {:opts {:label type-name}})
        (apply dom/div #js {:className "option-buttons"}
               (map #(om/build option-buton
                               (type-index app)
                               {:opts {:type type-name
                                       :option %}
                                :init-state {:choose choose}})
                    choices))))))

(defn number-bar [app owner]
  (option-bar app owner #js {:className "bar number-bar"} "number" :number [1 2 3 5]))

(defn sides-bar [app owner]
  (option-bar app owner #js {:className "bar sides-bar"} "sides" :sides [3 6 8 10 12 20]))

(defn roll-bar [app owner]
  (om/component
   (dom/div #js {:className "bar roll-bar"}
    (dom/button #js {:className "roll-button"
                     :onClick #(do
                                  (play-sound! roll-sound)
                                  (om/update! app :roll (roll-dice (:number app)
                                                                (:sides app))))}
                (dom/h1 nil "ROLL")))))

(defn result-entry [roll owner]
  (om/component
   (dom/div #js {:className "result"}
    (dom/h1 nil roll))))

(defn result-bar [app owner]
  (om/component
   (apply dom/div #js {:className "bar result-bar"}
    (om/build-all result-entry (:roll app)))))

(defn main-page [app owner]
  (om/component
   (dom/div #js {:className "container"}
     (om/build top-bar app)
     (om/build number-bar app)
     (om/build sides-bar app)
     (om/build roll-bar app)
     (om/build result-bar app)
     (om/build about-page app))))

(defn main []
  (om/root
    main-page
    dice-state
    {:target (. js/document (getElementById "app"))}))
