(ns validatom.prepare-db
  (:require
   [clojure.java.io :as io]
   [datomic.api :as d])
  (:import [datomic Util]))

(defn read-resource [edn-filename]
  {:pre [(string? edn-filename)
         (not (empty? edn-filename))]
   :post [(coll? %)]}
  (-> edn-filename
      io/resource
      io/reader
      Util/readAll))

(defn bare-schema-entities
  "all the keys beginning with db and having the install-attribute set.
supposedly transactable schema on empty db"
  [resource]
  (map #(into {} (filter (fn [[k v]] (when-let [ns-str (namespace k)] (.startsWith ns-str "db"))) %))
       (filter :db.install/_attribute resource)))

(defn validator?
  [{ident :db/ident :as validator}]
  (= (namespace ident) (namespace :validator/example)))

(defn validator-entities [resource]
  {:post [(every? :db/fn %)]}
  (filter validator? resource))

(defn validator-relations
  "the couplings attribute->validators"
  [resource]
  (->> resource
       (filter :db.install/_attribute)
       (filter :attribute/validators)
       (map (fn [m] (select-keys m [:db/id :db/ident :attribute/validators])))))

(defn transact-schema
  "dissects the schema resource and transacts parts of it in the order

1. bare schema entities
2. validator-entities (things named :validator/ expected to have :db/fn
3. the couplings from schema-entities to validators
4. transact everything once more to not miss everything else.

to the given conn.

returns the tx-data from the three transactions"
  [conn]
  {:pre [(instance? datomic.Connection conn)]}
  (let [schema-resource (read-resource "schema.edn")]
    (mapv :tx-data
          [@(d/transact conn (bare-schema-entities schema-resource))
           @(d/transact conn (validator-entities schema-resource))
           @(d/transact conn (validator-relations schema-resource))
           @(d/transact conn schema-resource)])))

(assert (bare-schema-entities (read-resource "schema.edn")))
(assert (validator-entities (read-resource "schema.edn")))
(assert (validator-relations (read-resource "schema.edn")))

(comment
  "Usage: 
create an empty db and do (transact-schemas conn)"
  
  (d/delete-database "datomic:mem://validatom")
  (d/create-database "datomic:mem://validatom")
  (def conn (d/connect "datomic:mem://validatom"))
  (transact-schema conn))

