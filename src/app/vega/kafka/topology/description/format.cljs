(ns app.vega.kafka.topology.description.format
  (:require [clojure.set :refer [rename-keys]]))

(defn format-vega-spec
  [description]
  (let [description-edn (first (js->clj description))
        nodes (into [] (map #(select-keys % [:type :name])) (:nodes description-edn))
        edges (into [] (map #(-> % (select-keys [:from :to]) (rename-keys {:from :source :to :target}))) (:edges description-edn))]
    {:nodes nodes
     :edges edges}))
