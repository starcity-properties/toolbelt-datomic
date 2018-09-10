(ns toolbelt.datomic.test-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.datomic.test :as sut]))

(deftest can-acquire-connection
  (is (td/conn? (sut/acquire-conn))
      "`acquire-conn` produces a Datomic connection"))


(deftest with-conn
  (testing "`with-conn` produces a Datomic connection"
    (sut/with-conn conn
      (is (td/conn? conn))))

  (testing "nested `with-conn`s produce the same connection"
    (binding [sut/*conn* (sut/acquire-conn)]
      (sut/with-conn conn1
        (sut/with-conn conn2
          (is (= sut/*conn* conn1 conn2)))))))


(deftest conn-fixture
  (testing "the `conn-fixture` produces a binding for `*conn*` without args"
    (is (nil? sut/*conn*))
    ((sut/conn-fixture)
     (fn []
       (is (some? sut/*conn*))
       (sut/with-conn conn
         (is (= sut/*conn* conn))))))

  (let [txn-fn (fn [conn]
                 @(d/transact conn [{:db/id          (d/tempid :db.part/db)
                                     :db/ident       :some/schema
                                     :db/valueType   :db.type/string
                                     :db/cardinality :db.cardinality/one}]))]
    (testing "can supply a transaction fn (`txn-fn`) to transact datoms prior to running tests"
      ((sut/conn-fixture txn-fn)
       (fn []
         (sut/with-conn conn
           (is (td/entityd? (d/entity (d/db conn) :some/schema)))))))))
