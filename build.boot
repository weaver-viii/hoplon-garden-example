#!/usr/bin/env boot

#tailrecursion.boot.core/version "2.0.0"

(set-env!
 :dependencies '[[tailrecursion/boot.task "2.0.0"]
                 [tailrecursion/hoplon "5.1.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [garden "1.1.5"]]
 :src-paths    #{"src"}
 :out-path     "resources/public"
 :garden       '[{:stylesheet garden.css/screen
                  :compiler {:pretty-print? false}}])

(require '[tailrecursion.hoplon.boot :refer :all]
         '[garden.compiler           :refer [compile-css]]
         '[clojure.java.io           :as    io]
         '[tailrecursion.boot.core   :as    boot])

(defn garden-compile
  [{:keys [stylesheet compiler] :as build}]
  (let [tmpdir   (boot/mkoutdir! ::garden-tmp)
        out-file (->> (or (get compiler :output-to)
                          (str (name stylesheet) ".css"))
                      (io/file tmpdir)
                      .getPath)]
    (println "[garden] Compiling" stylesheet "...")
    (require (symbol (namespace stylesheet)) :reload-all)
    (compile-css
     (assoc compiler :output-to out-file)
     @(resolve stylesheet))))

(deftask garden [& opts]
  (fn [continue]
    (fn [event]
      (when-let [builds (get-env :garden)]
        (doseq [build builds] (garden-compile build)))
      (continue event))))
