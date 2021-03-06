## kafka-topology-viz
A small app used to generate various Kafka Stream Topology Visualizations 

This app was produced to help visualize some Kafka Stream topologies for some specific projects that are responsible for generating changelog data from various sources, enrich that data via stream processors, and then sink that data to various sources.

## Usage
Download the project and in the root directory run 
`clj -M:dev`.

This will start the app and open a page in your browser.

To build the visualizations we need to load text into the application. The text needs a contain a certain json structure

`{"edn-topo": <some-edn-topo>
  "ascii-topo": <some-ascii-topo>}`

Where the `edn-topo` is the topology description generated by `describe-topology` function from the FundingCircle/Jackdaw Clojure Kafka library.
The `ascii-topo` is the ascii string generated by `describe()` method from the Topology Kafka class `https://kafka.apache.org/23/javadoc/org/apache/kafka/streams/Topology.html`.

You can enter this json manually or you can inject it via an http request by selecting the `curl` radio button and entering the url.