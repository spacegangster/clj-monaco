;;;;
;; Copyright © 2019-2020 Ilshat Sultanov. All rights reserved.
;; The use and distribution terms for this software are covered by the license which
;; can be found in the file license at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by the terms
;; of this license. You must not remove this notice, or any other, from this software.
;;;;

(ns monaco.example
  "A simple example of library usage."
  (:require
    [clojure.string :as str]
    [reagent.dom :as dom]
    [re-frame.core :as rf]
    [monaco.core :as monaco]
    [monaco.js-interop :as j]
    [monaco.api.editor :as monaco.editor]
    [monaco.api.keyboard :as monaco.keyboard]
    [monaco.api.languages :as monaco.languages])
  (:require-macros
    [monaco.build :refer [read-info]]))

;;;;
;; Helper functions
;;;;

(defn get-value [input]
  (j/get-in input [:target :value]))



;;;;
;; Custom language
;;;;

(defn register! []
  (monaco.languages/register {:id "custom"})

  (monaco.languages/set-monarch-tokens-provider "custom"
    {:tokenizer {:root [[#"\[error.*" "custom-error"]
                        [#"\[notice.*" "custom-notice"]
                        [#"\[info.*" "custom-info"]
                        [#"\[[a-zA-Z 0-9:]+\]" "custom-date"]]}})

  (monaco.languages/register-completion-item-provider "custom"
    {:provide-completion-items (fn []
                                 {:suggestions [{:label       "simpleText"
                                                 :insert-text "simpleText"
                                                 :kind        (monaco.languages/completion-item-kind :text)}
                                                {:label             "testing"
                                                 :insert-text       "testing(${1:condition})"
                                                 :insert-text-rules (monaco.languages/completion-item-kind :keyword)
                                                 :kind              (monaco.languages/completion-item-insert-text-rule :insert-as-snippet)}]})})

  (monaco.editor/define-theme "custom"
    {:base    "vs"
     :inherit false
     :rules   [{:token "custom-info" :foreground "808080"}
               {:token "custom-error" :foreground "ff0000" :font-style "bold"}
               {:token "custom-notice" :foreground "ffa500"}
               {:token "custom-date" :foreground "008800"}]}))



;;;;
;; Events & subscriptions
;;;;

(def build-info (read-info))

(def custom-text
  "[Sun Mar 7 16:02:00 2004] [notice] Apache/1.3.29 (Unix) configured -- resuming normal operations
[Sun Mar 7 16:02:00 2004] [info] Server built: Feb 27 2004 13:56:37
[Sun Mar 7 16:02:00 2004] [notice] Accept mutex: sysvsem (Default: sysvsem)
[Sun Mar 7 21:16:17 2004] [error] [client xx.xx.xx.xx] File does not exist: /home/httpd/twiki/view/Main/WebHome")

(def custom-actions
  [{:id          "cursor-position"
    :label       "Show the current cursor position"
    :keybindings [(monaco.keyboard/with-combo :ctrl-cmd :f10)
                  (monaco.keyboard/with-chord
                    (monaco.keyboard/with-combo :ctrl-cmd :key-k)
                    (monaco.keyboard/with-combo :ctrl-cmd :key-m))]
    :run         (fn [editor]
                   (let [position (monaco.editor/get-position editor)]
                     (js/confirm (str "Position: " position))))}])


(rf/reg-event-db
  ::init
  (fn [_ _]
    {:languages ["clojure" "javascript" "custom"]
     :themes    {"vs"       "Light"
                 "vs-dark"  "Dark"
                 "hc-black" "High Contrast"
                 "custom"   "Custom"}
     :info      build-info
     :editor    {:width                  "100%"
                 :height                 "100%"
                 :value                  custom-text
                 :default-value          ""
                 :language               "custom"
                 :theme                  "custom"
                 :minimap                {:enabled true}
                 :auto-indent            true
                 :select-on-line-numbers true
                 :rounded-selection      false
                 :read-only              false
                 :cursor-style           "line"
                 :automatic-layout       false
                 :editor-did-mount       (fn [editor]
                                           (monaco.editor/add-actions editor custom-actions)
                                           (monaco.editor/focus editor))
                 :on-change              (fn [new-value event] (rf/dispatch [::set-value new-value]))
                 :override-services      {}}}))


(rf/reg-sub
  ::info
  (fn [db]
    (:info db)))

(rf/reg-sub
  ::languages
  (fn [db]
    (->> db
      :languages
      (reduce
        (fn [acc k]
          (assoc acc k (str/capitalize k)))
        {})
      (sort-by val))))

(rf/reg-sub
  ::themes
  (fn [db]
    (sort-by val (:themes db))))

(rf/reg-sub
  ::editor
  (fn [db]
    (:editor db)))

(rf/reg-sub
  ::language
  :<- [::editor]
  (fn [editor]
    (:language editor)))

(rf/reg-sub
  ::theme
  :<- [::editor]
  (fn [editor]
    (:theme editor)))

(rf/reg-event-db
  ::set-theme
  (fn [db [_ theme]]
    (assoc-in db [:editor :theme] theme)))

(rf/reg-event-db
  ::set-language
  (fn [db [_ language]]
    (assoc-in db [:editor :language] language)))

(rf/reg-event-db
  ::set-value
  (fn [db [_ value]]
    (assoc-in db [:editor :value] value)))



;;;;
;; Components
;;;;

(defn select-theme []
  (let [theme  @(rf/subscribe [::theme])
        themes @(rf/subscribe [::themes])]
    [:div.mr-2
     [:span "Theme:"]
     (into [:select.form-select.mt-1.block
            {:value     theme
             :on-change #(rf/dispatch [::set-theme (get-value %)])}]
       (for [[k v] themes]
         ^{:key k}
         [:option {:value k} v]))]))

(defn select-language []
  (let [language  @(rf/subscribe [::language])
        languages @(rf/subscribe [::languages])]
    [:div.mr-2
     [:span "Language:"]
     (into [:select.form-select.mt-1.block
            {:value     language
             :on-change #(rf/dispatch [::set-language (get-value %)])}]
       (for [[k v] languages]
         ^{:key k}
         [:option {:value k} v]))]))

(defn config []
  [:div.mt-6.mb-10.flex.flex-auto.w-full
   [select-theme]
   [select-language]])

(defn header [info]
  [:div.flex.justify-between
   [:h1 "Monaco Editor"]
   [:a {:href (:git/url info) :target "_blank"} "GitHub"]])

(defn build [info]
  (when (seq info)
    [:div.flex.flex-col.items-end
     [:span.text-xs (str "v" (:version info) " (" (:timestamp info) ")")]]))

(defn notes []
  [:div.mt-6.flex.flex-col
   [:h2 "Notes"]
   [:span.mt-2.text-sm "Keyboard shortcuts:"]
   [:ul.list-disc.ml-6
    [:li
     [:div.text-xs
      [:span "Show the current cursor position:"]
      [:span.font-bold.ml-1 "cmd+f10"]
      [:span.ml-1 "or"]
      [:span.font-bold.ml-1 "cmd+k cmd+m"]]]]])

(defn root []
  (let [info @(rf/subscribe [::info])]
    [:div.m-6
     [header info]
     [build info]
     [notes]
     [config]
     [monaco/editor @(rf/subscribe [::editor])]]))



;;;;
;; Application entry point
;;;;

(defn mount-root
  "Mount root component."
  {:dev/after-load true}
  []
  (rf/clear-subscription-cache!)
  (dom/render [root]
    (j/invoke js/document :get-element-by-id "root")))


(defn init
  "Monaco UI initializer."
  {:export true}
  []
  (register!)
  (rf/dispatch-sync [::init])
  (mount-root))
