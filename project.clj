(defproject starcity/toolbelt-datomic "0.6.3-SNAPSHOT"
  :description "Utility functinos for working with datomic."
  :url "https://github.com/starcity-properties/toolbelt-datomic"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.macro "0.1.2"]
                 [com.datomic/datomic-free "0.9.5544" :scope "provided"]
                 [io.rkn/conformity "0.5.1"]]
  :plugins [[s3-wagon-private "1.2.0"]]
  :deploy-repositories {"releases"  {:url        "s3://starjars/releases"
                                     :username   :env/aws_access_key
                                     :passphrase :env/aws_secret_key}
                        "snapshots" {:url        "s3://starjars/snapshots"
                                     :username   :env/aws_access_key
                                     :passphrase :env/aws_secret_key}})
