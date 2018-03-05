(ns toolbelt.datomic.schema
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.macro :as ctm]
            [datomic.api :as d]
            [io.rkn.conformity :as c]))


;; tempids ======================================================================


(def ^:dynamic *part*
  "The default partition for new tempids."
  :db.part/user)


(defn tempid
  "Create a new Datomic `tempid`."
  [& [part]]
  (d/tempid (or part *part*)))


(defmacro with-partition
  "Execute `body` with the default partition set to `part`."
  [part & body]
  `(binding [*part* ~part]
     ~@body))


(defn set-partition!
  "Set the partition to `part`."
  [part]
  (alter-var-root #'*part* (constantly part)))


;; schema =======================================================================


(s/def ::lookup-ref
  (s/cat :key keyword? :v any?))

(s/def ::tx-vector
  (s/cat :op #{:db/add :db/retract}
         :e (s/or :eid integer?
                  :lookup ::lookup-ref
                  :keyword keyword?)
         :a keyword?
         :v any?))

(s/def ::tx
  (s/or :map (s/keys :req [:db/id])
        :tx-vec ::tx-vector))

(s/def ::txes
  (s/+ (s/spec (s/+ ::tx))))

(s/def ::requires
  (s/+ keyword?))

(s/def ::norm
  (s/keys :req-un [::txes] :opt-un [::requires]))

(s/def ::norms
  (s/map-of keyword? ::norm))


(defn gen-tx-key
  [namespace sym]
  (keyword (name namespace) (name sym)))


(defn schema-tx-key [{:keys [ns name tx-key]}]
  (or tx-key (gen-tx-key (ns-name ns) name)))


(defn- read-schema
  [namespace]
  (->> (ns-publics namespace)
       (filter (fn [[_ v]] (::schema (meta v))))))


(defn- assemble-norms [vars]
  (reduce
   (fn [acc var]
     (let [f (var-get var)
           m (meta var)]
       (assoc acc (schema-tx-key m)
              (merge {:txes [(f)]}
                     (when-some [rs (:tx-requires m)]
                       {:requires rs})))))
   {}
   vars))


(defn compile-schema
  "Compile all schemas in `namespace` into a map of conformity norms."
  [namespace]
  (let [norms (->> (read-schema namespace)
                   (map second)
                   (assemble-norms))]
    (assert (s/valid? ::norms norms) (s/explain-str ::norms norms))
    norms))


(defn compile-schemas
  "Compile all schemas found in `namespaces` for schema definitions into one map
  of conformity norms."
  [& namespaces]
  (->> namespaces
       (map compile-schema)
       (apply merge)))


(defn install-schema
  "Install the schema contained in `norms` into the database referenced by
  `conn`."
  [conn norms]
  (c/ensure-conforms conn norms))


(defmacro defschema
  "Helper macro to define datomic schema. Takes a symbol or keyword as its first
  argument, an optional vector of required schema keys, and a body that should
  evaluate to valid Datomic schema."
  [sym-or-kw & body]
  (let [sym        (if (symbol? sym-or-kw) sym-or-kw (symbol (name sym-or-kw)))
        [sym body] (ctm/name-with-attributes sym body)
        [req body] (if (s/valid? ::requires (first body))
                     [(first body) (next body)]
                     [nil body])
        metadata   (merge {::schema true}
                          (meta sym)
                          (when (some? req) {:tx-requires req})
                          (when (keyword? sym-or-kw) {:tx-key sym-or-kw}))]
    `(defn ~(with-meta sym metadata) []
       ~@body)))
