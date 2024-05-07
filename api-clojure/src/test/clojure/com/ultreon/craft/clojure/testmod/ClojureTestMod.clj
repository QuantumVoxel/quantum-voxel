(ns dev.ultreon.quantum.clojure.testmod.ClojureTestMod
  (:import (dev.ultreon.quantum ModInit)))

(def ^:public ^:static MOD_ID "clojure_testmod")
(defn- on-initialize []
  (println (format "Hello from Clojure! Mod ID: %s" MOD_ID)))

(deftype ^:public ClojureTestMod []
  ModInit
  MOD_ID
  (onInitialize [_] (on-initialize)))
