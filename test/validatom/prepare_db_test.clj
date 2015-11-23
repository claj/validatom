(ns validatom.prepare-db-test
  (:require [clojure.test :refer :all]
            [validatom.prepare-db :refer :all]
            [datomic.api :refer [create-database connect transact with db]]))

(def uri "datomic:mem://prepare-db-test")

(create-database uri)

(def conn (connect uri))

(deftest testing-bare-schema
  (let [bare-schema-entity (bare-schema-entities [{:db/id #db/id [:db.part/db]
                                  :db/ident :user/lastname
                                  :db/valueType :db.type/string
                                  :db/cardinality :db.cardinality/one
                                  :db.install/_attribute :db.part/db
                                  :attribute/validators [:validator/not-too-long
                                                         :validator/not-empty-string]}])]
    (is (with (db conn) bare-schema-entity))))
