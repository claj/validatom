(ns validatom.core
  "ways to validate tx-data against a database with validators"
  (:require
   [validatom.prepare-db]
   [clojure.set :refer [index]]
   [datomic.api :as d :refer [db entity invoke ident]]))

(defn attribute-exists?
  "for use in :pre, throws a more
informative exception or returns true"
  [db datom]
  (let [a (nth datom 1)
        v (nth datom 2)]
    (when-not (entity db a)
      (throw
       (ex-info
        (str "attribute " a " does not exist!")
        {:a a
         :v v
         :datom datom
         :error :attribute-does-not-exist}))))
  ;; to satisfy :pre
  true)

(defn validate-datom
  "returns a map with

:datom      - the tested datom
:attribute  - usually just a number in the datom
:validators - a list of executed validators
:results    - non-nil error-reports from validators
:correct?   - if the datom passed all validators without error reports"
  [db datom]
  {:pre [(attribute-exists? db datom)]}
  (let [a (nth datom 1)
        v (nth datom 2)
        validators (:attribute/validators (entity db a))
        validator-results (remove nil? (map #(invoke db % datom) validators))]
    {:attribute (ident db a)
     :datom datom
     :validators (map (partial ident db) validators)
     :results validator-results
     :correct? (empty? validator-results)}))

(def datom-correct? (comp :correct? validate-datom))

(defn validate-tx-data
  "returns a seq of maps with reports as validate-datom"
  [db tx-data]
  (mapv (partial validate-datom db) tx-data))

(defn tx-data-correct? [db tx-data]
  (every? true? (map datom-correct? (repeat db) tx-data)))
