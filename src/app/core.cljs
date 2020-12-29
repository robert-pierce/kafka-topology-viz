(ns ^:figwheel-hooks app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as r.dom]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [app.vega.kafka.topology.spec :as v]
            [app.vega.kafka.topology.description.format :as vf]))

(def radio-state (r/atom "curl"))

(defn parse-json
  [raw]
  (js->clj (.parse js/JSON raw) :keywordize-keys true))

(defn get-input-value-by-id
  [id]
  (-> js/document
      (.getElementById id)
      (.-value)))

(defn reset-text-input!
  []
 (-> js/document
     (.getElementById "input")
     (.-value)
     (set! "")))

(defn radio-on-change
  [input]
  (js/console.log "The radio input is: " (.. input -target -value))
  (reset! radio-state (.. input -target -value)))

(defn draw-rough-viz!
  [ascii-spec]
  (js/updateViz ascii-spec))

(defn update-vega-viz!
  [vega-spec]
  (-> (js/vegaEmbed "#vega-viz" (clj->js vega-spec))
      (.then (fn [result]))
      (.catch (.-error js/console))))

(defn parse-raw-input
  [input]
  (try
    (let [{:keys [ascii-topo edn-topo] :as json} (if (map? input) input  (parse-json input))]
      (js/console.log "Parse-raw-input ascii-topo is: " (v/topology-vega-spec (vf/format-vega-spec edn-topo)))
      
      {:vega-spec (-> (vf/format-vega-spec edn-topo) v/topology-vega-spec)
       :ascii-spec ascii-topo})
    (catch :default e
      (js/alert "Error parsing json"))))

(defn process-raw-viz
  [val]
  (let [{:keys [vega-spec ascii-spec]} (parse-raw-input val)]
    (when ascii-spec
      (js/updateViz ascii-spec))
    (when vega-spec
      (update-vega-viz! vega-spec))))

(defn process-curl-request
  [value]
  (try
    (go (let [response (<! (http/get value {:with-credentials? false}))]
          (process-raw-viz (:body response))))
    (catch :default e
      (js/alert "Error executing curl ", e))))

(defn draw-viz
  []
  (let [radio-st @radio-state]
    (js/console.log "The state is: " radio-st)
    (cond
      (= radio-st "curl") (process-curl-request (get-input-value-by-id "input"))               
      (= radio-st "raw")  (process-raw-viz (get-input-value-by-id "input"))
      :none (js/alert "No input state available"))))

(defn topology-input
  []
  [:div.row
   [:h4 "Input Kafka Topology"]
   [:span "Enter a url or raw text"]
   [:div.col
    [:input {:type "radio" :id "curl-radio-btn" :name "topo-input" :value "curl" :defaultChecked true :onChange radio-on-change }]
    [:label {:for "curl-radio-btn"} "curl"]]
   [:div.col
    [:input {:type "radio" :id "ascii-radio-btn" :name "topo-input" :value "raw" :onChange radio-on-change}]
    [:label {:for "ascii-radio-btn"} "raw"]]
   [:div.col
    [:button {:onClick draw-viz} "update"]]
   [:textarea {:id "input"}]
   [:div.col
    [:button {:onClick reset-text-input!} "clear"]]])

(defn hand-drawn-div
  []
  [:div
   [:h4 "Sketch Topology Graph"] 
   [:canvas {:id "canvas"}]])

(defn vega-div
  []
  [:div 
   [:h4 "Vega Topology Graph"]
   [:div {:id "vega-viz"}]])

(defn vizs
  []
  [:div.row
   [:div.col [topology-input]]
   [:div.col [hand-drawn-div]]
   [:div.col [vega-div]]])

(defn app []
  (js/console.log "calling app")
  [:div.container
   [:h3.row
    [:div.col.text-center
     [:span.app-text "Kafka Topology Visualizations"]]]
   [vizs]])

(defn mount []
  (js/console.log "calling mount")
  (r.dom/render [app] (js/document.getElementById "root")))

(defn ^:after-load re-render []
  (js/console.log "calling after-load")
  (mount))

(defonce start-up 
  (do 
    (js/console.log "calling start-up")
    (mount) true))
