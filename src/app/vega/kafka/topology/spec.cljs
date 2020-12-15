(ns app.vega.kafka.topology.spec)

(def scales
[{:name "color",
   :type "ordinal",
   :domain {:data "node-data", :field "type"}
   :range ["blue" "green" "red" "orange" "purple"]}])

(def signals
 [{:name "cx" :update "width / 2"}
  {:name "cy" :update "height / 2"}
  {:name "nodeRadius"
   :value 8
   :bind {:input "range" :min 1 :max 50 :step 1}}
  {:name "nodeCharge"
   :value -30
   :bind {:input "range"
          :min -100
          :max 10
          :step 1}}
  {:name "linkDistance"
   :value 30
   :bind {:input "range" :min 5 :max 100 :step 1}}
  {:name "static"
   :value true
   :bind {:input "checkbox"}}
  {:description "State variable for active node fix status."
   :name "fix"
   :value false
   :on [{:events
         "symbol:mouseout[!event.buttons], window:mouseup"
         :update "false"}
        {:events "symbol:mouseover"
         :update "fix || true"}
        {:events "[symbol:mousedown, window:mouseup] > window:mousemove!"
     :update "xy()"
     :force true}]}
  {:description "Graph node most recently interacted with."
   :name "node"
   :value nil
   :on [{:events "symbol:mouseover"
         :update "fix === true ? item() : node"}]}
  {:description "Flag to restart Force simulation upon data changes."
   :name "restart"
   :value false
   :on [{:events {:signal "fix"}
         :update "fix && fix.length"}]}])

(def marks
  [{:name "nodes"
   :type "symbol"
   :zindex 1
   :from {:data "node-data"}
   :on [{:trigger "fix"
         :modify "node"
         :values
         "fix === true ? {fx: node.x, fy: node.y} : {fx: fix[0], fy: fix[1]}"}
        {:trigger "!fix"
         :modify "node"
         :values "{fx: null, fy: null}"}]
    :encode
    {:enter
     {:fill {:scale "color" :field "type"}
      :stroke {:value "white"}
     :tooltip {:signal "datum"}}
     :update
     {:size
      {:signal "2 * nodeRadius * nodeRadius"}
      :cursor {:value "pointer"}}}
    :transform [{:type "force"
                 :iterations 300
                 :restart {:signal "restart"}
                 :static {:signal "static"}
                 :signal "force"
                 :forces
                [{:force "link"
                  :links "link-data"
                  :id "datum['name']"
                  :distance {:signal "linkDistance"}}
                 {:force "center"
                  :x {:signal "cx"}
                  :y {:signal "cy"}}
                 {:force "collide"
                  :radius {:signal "nodeRadius"}}
                 {:force "nbody"
                  :strength {:signal "nodeCharge"}}]}]}
   {:type "path"
    :from {:data "link-data"}
    :interactive false
    :encode
    {:update
     {:stroke {:value "#ccc"}
      :strokeWidth {:value 0.5}}}
    :transform [{:type "linkpath"
                 :require {:signal "force"}
                 :shape "line"
                 :sourceX "datum.source.x"
                 :sourceY "datum.source.y"
                 :targetX "datum.target.x"
                 :targetY "datum.target.y"}]}])

(defn topology-vega-spec
  "Accepts a kafka streams Topology description.
   As of 12/15/2020 this description should be edn
   in the form generated by jackdaw.streams.describe/describe-topology

  Nodes should be a vector of maps containing the keys :type and :name
  :type is the kafka-streams node type and :name is a unique name amongst the nodes

  Edges should be a vector of maps containing the keys :source and :target
  where :source and :target are node names"
  [{:keys [nodes edges] :as description}]
  {:$schema "https://vega.github.io/schema/vega/v5.json"
   :description "Kafka Streams Topology Graph"
   :width 700
   :height 500
   :padding 0
   :autosize "none"
   :signals signals
   :scales scales
   :data [{:name "node-data"
           :values nodes}
          {:name "link-data"
           :values edges}]
   :marks marks})