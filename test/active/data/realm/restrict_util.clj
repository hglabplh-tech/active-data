(ns active.data.realm.restrict-util
  (:require [active.data.realm :as realm]))

(def hex-regexp #"\p{XDigit}+")
(def email-regexp #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")

(clojure.core/defn- build-regex-realm [pred-key reg-exp description]
  (let [regexp-bind reg-exp
        descr-bind description
        ]
    (realm/restricted pred-key
                      realm/string
                      (fn [to-check] (re-matches regexp-bind to-check))
                      descr-bind)))

(def hex-check (build-regex-realm :hexstring hex-regexp "hexstring"))
(def email-check (build-regex-realm :emailstring email-regexp "emailstring"))

(clojure.core/defn build-java-type-realm [pred-key java-type]
  (let [java-type-bind java-type
        descr-bind (str "Java class to check for: " java-type)
        ]
    (realm/restricted pred-key realm/any
                      (fn [to-check]
                        (if (instance? java.lang.Object to-check)
                          (instance? java-type-bind to-check)
                          false))
                      descr-bind)))

(def uuid-type-check (build-java-type-realm :uuid-type java.util.UUID))
(def date-type-check (build-java-type-realm :date-type java.util.Date))