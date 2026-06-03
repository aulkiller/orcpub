(ns orcpub.datomic
  "Datomic database component with connection management and error handling.

  Provides a component that manages the database connection lifecycle,
  including database creation, connection establishment, and schema initialization.
  All operations include error handling with clear error messages."
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [orcpub.db.schema :as schema]))

(defrecord DatomicComponent [uri conn]
  component/Lifecycle
  (start [this]
    (if (:conn this)
      this
      (try
        (when (nil? uri)
          (throw (ex-info "Database URI is required but not configured"
                          {:error :missing-db-uri})))

        (println "Creating/connecting to Datomic database:" uri)
        (d/create-database uri)

        (let [connection (try
                           (d/connect uri)
                           (catch Exception e
                             (throw (ex-info "Failed to connect to Datomic database. Please verify the database URI and that Datomic is running."
                                             {:error :db-connection-failed
                                              :uri uri}
                                             e))))]
          (try
            @(d/transact connection schema/all-schemas)
            (println "Successfully initialized database schema")
            ;; Auto-seed compendium if empty
            (let [compendium-count (d/q '[:find (count ?e) .
                                          :where [?e :orcpub.compendium/type _]]
                                        (d/db connection))]
              (when (or (nil? compendium-count) (zero? compendium-count))
                (println "Compendium empty — seeding SRD + Blood Hunter data...")
                (try
                  (require 'orcpub.compendium.seed)
                  ((resolve 'orcpub.compendium.seed/seed-compendium!) connection)
                  (println "Compendium seeded successfully.")
                  (catch Exception se
                    (println "WARNING: Compendium seeding failed (non-fatal):" (.getMessage se))))))
            ;; Auto-create admin user if INIT_ADMIN_USER env is set and user doesn't exist
            (when-let [admin-user (System/getenv "INIT_ADMIN_USER")]
              (when (seq admin-user)
                (let [admin-email (or (not-empty (System/getenv "INIT_ADMIN_EMAIL")) (str admin-user "@localhost"))
                      admin-pass (System/getenv "INIT_ADMIN_PASSWORD")
                      existing (d/q '[:find ?e .
                                      :in $ ?username
                                      :where [?e :orcpub.user/username ?username]]
                                    (d/db connection) admin-user)]
                  (when (and (nil? existing) (seq admin-pass))
                    (println (str "Creating admin user: " admin-user " (" admin-email ")"))
                    (try
                      (require 'buddy.hashers)
                      @(d/transact connection
                                  [{:orcpub.user/username admin-user
                                    :orcpub.user/email admin-email
                                    :orcpub.user/password ((resolve 'buddy.hashers/derive) admin-pass)
                                    :orcpub.user/verified? true
                                    :orcpub.user/created (java.util.Date.)}])
                      (println "Admin user created successfully.")
                      (catch Exception ue
                        (println "WARNING: Admin user creation failed (non-fatal):" (.getMessage ue))))))))
            (catch Exception e
              (throw (ex-info "Failed to initialize database schema. The database may be in an inconsistent state."
                              {:error :schema-initialization-failed
                               :uri uri}
                              e))))
          (assoc this :conn connection))
        (catch clojure.lang.ExceptionInfo e
          (throw e))
        (catch Exception e
          (throw (ex-info "Unexpected error during database initialization"
                          {:error :db-init-failed
                           :uri uri}
                          e))))))
  (stop [this]
    (assoc this :conn nil)))

(defn new-datomic [uri]
  (map->DatomicComponent {:uri uri}))
