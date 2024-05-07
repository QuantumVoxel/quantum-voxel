(ns dev.ultreon.quantum.clojure.testmod.init.ModItems
  (:require [clojure.test :refer :all])
  (:import (dev.ultreon.quantum.item Item Item$Properties)
           (dev.ultreon.quantum.registry DeferRegistry Registries DeferredElement)
           (java.util.function Supplier)))
  (:require [Item]
            [DeferRegistry]
            [Registries]
            [DeferredElement])

(def ^:field ^:static REGISTER (DeferRegistry/of (dev.ultreon.quantum.clojure.testmod.ClojureTestMod/MOD_ID, Registries/ITEM) DeferRegistry))

(def TEST_ITEM (REGISTER .register ("test_item", Supplier #(Item. (Item$Properties.))) DeferredElement))

(defn register []
  (.register REGISTER))

