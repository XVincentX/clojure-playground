(ns app.data (:require [datomic.client.api :as d]
                       [clojure.spec.alpha :as s]))


(def db-schema [{:db/ident :person/id
                 :db/valueType :db.type/uuid
                 :db/unique :db.unique/identity
                 :db/cardinality :db.cardinality/one}

                {:db/ident :person/name
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one}

                {:db/ident :person/surname
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one}

                {:db/ident :person/notes
                 :db/valueType :db.type/ref
                 :db/isComponent true
                 :db/cardinality :db.cardinality/many}

                {:db/ident ::note
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one}])

(def seed-data [{:person/id (java.util.UUID/randomUUID)
                 :person/name "Vincenzo"
                 :person/surname "Chianese"
                 :person/notes ["note1" "note2"]}

                {:person/id (java.util.UUID/randomUUID)
                 :person/name "Elio"
                 :person/surname "Bencini"
                 :person/notes ["note2" "note1"]}

                {:db/id "note1"
                 ::note "Nessuno mi vuole"}

                {:db/id "note2"
                 ::note "Sono un babbasone"}])

(def client (d/client {:server-type :dev-local
                       :system "dev"
                       :storage-dir "/tmp/app/src/data/"}))

(def db-name "db")
(defn reset-db! [db-name] (d/delete-database client {:db-name db-name}))
(defn get-current-conn [] (d/connect client {:db-name db-name}))
(defn get-current-db [] (d/db (get-current-conn)))
(defn init-db! "If the Datomic instance has no database, it will create one"
  ([] (init-db! "db"))
  ([db-name] (when (zero? (count (d/list-databases client {})))
               (d/create-database client {:db-name db-name})
               (let [conn (get-current-conn)]
                 (d/transact conn {:tx-data db-schema})
                 (d/transact conn {:tx-data seed-data})))))

(s/def :person/id uuid?)
(s/def :person/name string?)
(s/def :person/surname string?)
(s/def ::note string?)
(s/def :person/notes (s/coll-of :note :kind vector?))
(s/def ::person (s/keys :req [:person/name :person/surname] :opt [:person/notes]))
