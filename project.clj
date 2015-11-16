(defproject validatom "0.1.0-SNAPSHOT"
  :description "an example of transactor enforced database validator"
  :url "https://github.com/claj/validatom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0-RC1"]
                 [com.datomic/datomic-free "0.9.5302"]]
  :repositories [["sonatype"
                  {:url "https://oss.sonatype.org/content/repositories/snapshots"
                   :update :always}]])
