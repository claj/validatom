(ns validatom.example
  "Usage example in which we create a database, put schemas in it,
do some validations outside transaction, in transaction and "
  (:require
   [validatom
    prepare-db
    [core :refer [validate-datom
                  datom-correct?
                  validate-tx-data
                  tx-data-correct?]]]
   [datomic.api :as d :refer [db entity invoke]]))

(def uri "datomic:mem://valiant")

(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))

(validatom.prepare-db/transact-schema conn)

(def base-db (db conn))

;; invoking validators one and one
(d/invoke base-db :validator/email        [nil :user/email "linus.ericsson@agical.se" nil]) ;; nil
(d/invoke base-db :validator/email        [nil :user/email "no-at-sign-here" nil]) ;; {:msg "email must contain @", :fn :validator/email, :error :cannot-be-empty}
(d/invoke base-db :validator/not-too-long [nil :user/email (apply str (repeat 200 "@"))])
;;{:msg "string beginning with @@@@@@@@@@@@@@@@@@@@ too long 200", :fn :validator/not-too-long, :len 200, :beginning "@@@@@@@@@@@@@@@@@@@@"}

;; validating a single datom
(validate-datom base-db [nil :user/email "linus.ericsson@agical.se"])
(validate-datom base-db [nil :user/email "asfa"])
(validate-datom base-db [nil :user/email (str "asfa" (apply str (repeat 200 "@")))])

;; the shortform: datom-correct?
(datom-correct? base-db [nil :user/email "linus.ericsson@agical.se"]) ;;=> true
(datom-correct? base-db [nil :user/email "asfa"]) ;;=> false
(datom-correct? base-db [nil :user/email (str "asfa" (apply str (repeat 200 "@")))]) ;;=> false

;;validating a bunch of datoms
(validate-tx-data base-db [[1024 :user/email "linus.ericsson@agical.se"]
                           [1024 :user/firstname "Linus"]])

;;validating incorrect datoms
(validate-tx-data base-db [[1024 :user/email "linus.ericssonl.se"]
                           [1024 :user/firstname ""]
                           [7788 :user/firstname "Franz"]])

;; short form:
(tx-data-correct? base-db [[1024 :user/email "linus.ericsson@agical.se"]
                           [1024 :user/firstname "Linus"]])

(tx-data-correct? base-db [[1024 :user/email "linus.ericssonl.se"]
                           [1024 :user/firstname ""]
                           [7788 :user/firstname "Franz"]])

;; REAL DATOMS! (btw how do you mock these for real?!)
(def some-tx-data
  (:tx-data
   @(d/transact conn [{:db/id (d/tempid :db.part/user)
                       :user/email "asda"
                       :user/firstname ""}])))

;;some-tx-data

;;        E              A  V                                     T              added?
;
;; #datom[13194139534329 50 #inst "2015-11-15T14:17:06.433-00:00" 13194139534329 true]
;; #datom[17592186045434 66 "asda"                                13194139534329 true]
;; #datom[17592186045434 64 ""                                    13194139534329 true]


(validate-datom base-db (nth some-tx-data 0))
(validate-datom base-db (nth some-tx-data 1))
(validate-datom base-db (nth some-tx-data 2))

(datom-correct? base-db (nth some-tx-data 0))
(datom-correct? base-db (nth some-tx-data 1))
(datom-correct? base-db (nth some-tx-data 2))


;;validate all datoms in tx-data at once
(validate-tx-data base-db some-tx-data)

(tx-data-correct? base-db some-tx-data)

;; non-validated transaction

(def correct-tx-data
  (:tx-data
   @(d/transact conn [{:db/id (d/tempid :db.part/user)
                       :user/email "linus.ericsson@agical.se"
                       :user/firstname "Linus"}])))

;; validated transaction

(def transaction-with-exception
  (try
    (:tx-data
     @(d/transact conn [[:validate
                         [{:db/id (d/tempid :db.part/user)
                           :user/email "asda"
                           :user/firstname ""}]]]))
    (catch Exception e e)))

(def validated-transaction 
  (:tx-data
   @(d/transact conn [[:validate
                       [{:db/id (d/tempid :db.part/user)
                         :user/email "linus.ericsson@agical.se"
                         :user/firstname "Linus"}]]])))

