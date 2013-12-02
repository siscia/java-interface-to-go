(ns java-interface-to-go.core
  (:use [clojure.tools.cli :only [cli]])
  (:use [clojure.java.io :only [writer]])
  (:use [java-interface-to-go.from-runtime :only [make-tree]]))

;; software to be run as command line, it takes in input a string, and it evals the class it represent:
;; (class (eval input-str))
;; it is dangerous but it is suppose to be used only to generate other source file it stilll should be fine.
;; then the it spit the go interfaces.



(defn capitalize-first-only [s]
  (when (> (count s) 0)
    (str (clojure.string/capitalize (subs s 0 1))
         (subs s 1))))

(defn write-name [name wrtr]
  (.write wrtr (str "package " name "\n")))

(defn write-extended [super-class interface wrtr]
  (.write wrtr (str "\n" "import(" "\n"))
  (when super-class
    (.write wrtr (str "\t" "\"" (.getName super-class) "\"" "\n")))
  (when interface
    (doseq [inter interface]
      (.write wrtr (str "\t" "\""
                        (-> (.getName inter)
                            clojure.string/trim
                            clojure.string/lower-case)
                        "\"" "\n"))))
  (.write wrtr ")\n"))

(defn write-import-super-class [super-class wrtr]
  (when super-class
    (.write wrtr (str "\t"
                      (-> super-class .getName str
                          clojure.string/trim
                          clojure.string/lower-case)
                      ".Interface\n"))))

(defn write-import-extended-interface [interface wrtr]
  (doseq [inter interface]
    (.write wrtr (str "\t"
                      (-> inter .getName str
                          clojure.string/trim
                          clojure.string/lower-case)
                      ".Interface" "\n"))))

(defn write-methods [meth wrtr]
  (doseq [meth meth]
    (let [parameter-string (clojure.string/join ", "
                                                (map #(.getName %) (:parameter-types meth)))]
      (.write wrtr (str "\t"
                        (.getName (:return-type meth)) " "
                        (capitalize-first-only (:method-name meth))
                        "(" parameter-string ")" "\n")))))

(defn write-interface [super-class interface meth wrtr]
  (.write wrtr (str "\n" "type Interface interface{" "\n"))
  (write-import-super-class super-class wrtr)
  (write-import-extended-interface interface wrtr)
  (write-methods meth wrtr)
  (.write wrtr (str "}" "\n")))

(defn make-file [source dir]
  (let [file-name (-> (str dir (:name source) ".go")
                      clojure.string/lower-case)]
    (with-open [wrtr (writer file-name)]
      (println "prova")
      (write-name (:name source) wrtr)
      (write-extended (:super-class source) (:interface source) wrtr)
      (write-interface (:super-class source)
                       (:interface source)
                       (:methods source)
                       wrtr)
      (println file-name))
    file-name))

(defn arguments-receiver [args]
  (cli args
       ["-o" "--output" "Directory where place the output file(s)" :default ""]))

(defn -main [input & args]
  (let [[options args banner] (arguments-receiver args)
        tree (make-tree (class [:a]))]
    (println (options :output))
    (make-file (first tree) (options :output))))
