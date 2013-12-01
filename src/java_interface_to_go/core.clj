(ns java-interface-to-go.core
  (:use [clojure.tools.cli :only [cli]])
  (:use [clojure.java.io :only [writer file]]))

(defn resume-interface [str]
  (when-let [name (is-interface? str)]
    (let [methods (get-methods str)
          not-nil (remove #(nil? (second %)) methods)
          meth (mapv (fn [a]
                       {:name (nth a 2)
                        :return-type (nth a 1)
                        :params (type-params (nth a 3))})
                not-nil)]
      {:name (get-name-interface str)
       :extendee (get-extendee str)
       :methods meth})))

(defn capitalize-first-only [s]
  (when (> (count s) 0)
    (str (clojure.string/capitalize (subs s 0 1))
         (subs s 1))))

(defn make-file [source dir]
  (if-let [resume (resume-interface source)]
    (let [file-name (-> (str (resume :name))
                        clojure.string/lower-case)]
      (with-open [wrtr (writer (str dir file-name ".go"))]
        (.write wrtr (str "package " file-name "\n"))
        (.write wrtr "\nimport(\n")
        (doseq [x (:extendee resume)]
          (.write wrtr (str "    \"" (-> (clojure.string/trim x)
                                         clojure.string/lower-case) "\"\n")))
        (.write wrtr ")\n")
        (.write wrtr (str "\ntype Interface interface{\n"))
        (doseq [ex (:extendee resume)]
          (.write wrtr (str "    " (-> (clojure.string/trim ex)
                                       clojure.string/lower-case) ".Interface\n")))
        (doseq [meth (:methods resume)]
          (.write wrtr (str "    "
                            (:return-type meth) " "
                            (capitalize-first-only
                             (:name meth))
                            "("
                            (:params meth)
                            ")\n")))
        (.write wrtr "}"))
      (str file-name ".go"))))

(defn arguments-receiver [args]
  (cli args
       ["-f" "--file" "Single interface.java file to convert to a interface.go file" :default nil]
       ["-d" "--directory" "Whole directory of file that will be analyzed" :default nil]
       ["-o" "--output" "Directory where place the output file(s)" :dafault ""]))

(defn -main [& args]
  (let [[options args banner] (arguments-receiver args)]
    (when-let [dir (:directory options)]
      (doseq [files (file-seq (file dir))]
        (when (.isFile files)
          (try (if-not (make-file (slurp files) (:output options))
                 (println files))
            (catch Exception e (println files ":<--- Exception"))
            (catch Error e (println files ":<--- Error, STACK"))))))
    (when-let [file-name (:file options)]
      (when (.isFile (file file-name))
        (try
          (if-not (make-file (slurp file-name) (:output options))
            (println file-name))
          (catch Exception e (println file-name ":<--- Exception"))
          (catch Error e (println file-name ":<--- Error, STACK")))))))

