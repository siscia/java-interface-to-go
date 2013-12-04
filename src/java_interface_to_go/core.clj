(ns java-interface-to-go.core
  (:use [clojure.tools.cli :only [cli]])
  (:use [clojure.java.io :only [writer]])
  (:use [java-interface-to-go.from-runtime :only [make-tree]])
  (:use [clojure.set :only [difference]]))

;; software to be run as command line, it takes in input a string, and it evals the class it represent:
;; (class (eval input-str))
;; it is dangerous but it is suppose to be used only to generate other source file it stilll should be fine.
;; then the it spit the go interfaces.

(def obj-meth
  (-> java.lang.Object
      make-tree
      first
      :methods))

(defn capitalize-first-only [s]
  (when (> (count s) 0)
    (str (clojure.string/capitalize (subs s 0 1))
         (subs s 1))))

(defn get-name-class [name]
  (-> 
   (re-seq #"(clojure.lang.|java.lang.)(.*)" name)
   first
   (nth 2)
   capitalize-first-only))

(defn make-file-name [name]
  (let [name (get-name-class name)]
    (str "cljgo/interface/" name)))

(defn name-import [name]
  (let [name (get-name-class name)]
    (println name)
    (str "cljgo.interface." name)))

(defn write-name [name wrtr]
  (.write wrtr (str "package " name "\n")))

(defn write-extended [super-class interface wrtr]
  (.write wrtr (str "\n" "import(" "\n"))
  (when super-class
    (.write wrtr (str "\t" "\"" (make-file-name (.getName super-class)) "\"" "\n")))
  (when interface
    (doseq [inter interface]
      (.write wrtr (str "\t" "\""
                        (-> inter
                            .getName
                            name-import
                            )
                        "\"" "\n"))))
  (.write wrtr ")\n"))

(defn write-import-super-class [super-class wrtr]
  (when super-class
    (.write wrtr (str "\t"
                      (-> super-class .getName name-import
                          clojure.string/trim)
                      ".Interface\n"))))

(defn write-import-extended-interface [interface wrtr]
  (doseq [inter interface]
    (.write wrtr (str "\t"
                      (-> inter .getName  get-name-class
                          clojure.string/trim)
                      ".Interface" "\n"))))

(defn write-methods [meth wrtr]
  (doseq [meth meth]
    (let [parameter-string (clojure.string/join ", "
                                                (map #(.getName %) (:parameter-types meth)))]
      (.write wrtr (str "\t"
                        (.getName (:return-type meth)) " "
                        (capitalize-first-only (:method-name meth))
                        "(" parameter-string ")" "\n")))))

(defn write-interface
  ([super-class interface meth wrtr]
     (.write wrtr (str "\n" "type Interface interface{" "\n"))
     (write-import-super-class super-class wrtr)
     (write-import-extended-interface interface wrtr)
     (when meth
       (do (println meth)
           (write-methods meth wrtr)))
     (.write wrtr (str "}" "\n"))))

(defn already-define-meth [interface super-class]
  (let [interfaces (flatten (map make-tree interface))
        int-meth (map :methods interfaces)
        super-class (when super-class
                      (make-tree super-class))
        super-meth (:methods super-class)]
    (apply set [(concat int-meth super-meth)])))

(defn make-file
  ([source]
     (make-file source ""))
  ([source dir]
     (let [name (get-name-class (:name source))
           file-name (str (make-file-name (:name source)) ".go")]
       (with-open [wrtr (writer file-name)]
         (write-name name wrtr)
         (write-extended (:super-class source)
                         (:interface source)
                         wrtr)
         (write-interface (:super-class source)
                          (:interface source)
                          (when-let [meth (difference
                                           (set (remove nil?
                                                        (:methods source)))
                                           (already-define-meth
                                            (:interface source)
                                            (:super-class source)))]
                            meth)
                          wrtr)
         (.write wrtr "\n"))
       (println file-name)
       file-name)))

(defn arguments-receiver [args]
  (cli args
       ["-o" "--output" "Directory where place the output file(s)" :default ""]))

(defn -main [input & args]
  (let [[options args banner] (arguments-receiver args)
        tree (make-tree (eval input))]
    (make-file (first tree) (options :output))))
