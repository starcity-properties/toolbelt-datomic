(defproject starcity/toolbelt-datomic "0.1.0"
  :description "Utility functinos for working with datomic."
  :url "https://github.com/starcity-properties/toolbelt-datomic"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [com.datomic/datomic-free "0.9.5544" :scope "provided"]]
  :deploy-repositories [["releases" :clojars]])
