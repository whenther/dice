(ns dice.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan <! >! timeout]]))

(def dice-state (atom {:number 3
                       :sides 6
                       :roll [1 2 3]
                       :show-top false
                       :show-side false
                       :show-arrows false}))

(defn roll-die [sides]
  (+ (rand-int sides) 1))

(defn roll-dice [num-dice sides]
  (repeatedly num-dice #(roll-die sides)))

(defn add-show-class [class-name state]
  (str class-name
       (if (true? state) " show" "")))

(defn pulse-arrows! [app]
  (go
    (<! (timeout 500))
    (om/update! app :show-arrows true)
    (<! (timeout 2000))
    (om/update! app :show-arrows false)))

(defn arrows [app owner]
  (reify
    om/IWillMount
      (will-mount [_]
        (pulse-arrows! app))
    om/IRender
    (render [_]
      (dom/img #js {:src "/img/arrows.png"
                    :className (add-show-class "arrows"
                                               (:show-arrows app))}))))

(defn top-bar [app owner]
  (om/component
    (dom/div #js {:className (add-show-class "top-bar"
                                             (:show-top app))}
      (dom/h3 #js {:className "title"} "dice"))))

(defn option-buton [state owner opts]
  (reify
    om/IRenderState
    (render-state [_ {:keys [choose]}]
      (dom/button #js {:className (str (:type opts)
                                       (if (= state (:option opts))
                                          " selected" "")
                                       " option-button")
                       :onClick  #(go (>! choose (:option opts)))}
                  (dom/h2 nil (:option opts))))))

(defn option-label [show-side owner {:keys [:label]}]
  (om/component
    (dom/div #js {:className (str "option-label" (if (true? show-side)
                                                " show" ""))}
       (dom/h4 nil label))))

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
        (om/build option-label (:show-side app) {:opts {:label type-name}})
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
  (option-bar app owner #js {:className "bar sides-bar"} "sides" :sides [2 6 20]))

(defn roll-bar [app owner]
  (om/component
   (dom/div #js {:className "bar roll-bar"}
    (dom/button #js {:className "roll-button"
                     :onClick #(om/update! app :roll (roll-dice (:number app)
                                                                (:sides app)))}
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
     (om/build arrows app)
     (om/build top-bar app)
     (om/build number-bar app)
     (om/build sides-bar app)
     (om/build roll-bar app)
     (om/build result-bar app))))

(defn main []
  (om/root
    main-page
    dice-state
    {:target (. js/document (getElementById "app"))}))
