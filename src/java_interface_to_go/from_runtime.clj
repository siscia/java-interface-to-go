(ns java-interface-to-go.from-runtime
  (:require [dorothy.core :as d]))

;; The idea here is to generate a tree from any data passed in input, the tree must expand as much as possible, hopefully from just one single data we could get all the clojure schema.

;; (class object) => the class of the object
;; (.getInterfaces class) => all the interface implemented by the class
;; (.getMethods class) => all the method implemented/defined by the class
;; (.getName method) => name of the method
;; (.getParameterTypes method)
;; (.getGenericReturnType method)
;; (.getGenericSuperclass class) => return the class that is implemented by the class itself


(def structure
  {:name "name-of-class"
   :interface ['class-1 'class-2]
   :methods [{:method-name "method-name"
              :parameter-types ['params1 'params2]
              :return-type 'return-type}]
   :super-class 'super-class})

;; cl stands for class
;; met stands for method

(defn expand-one-class [cl]
  (let [methods-seq (.getMethods cl)
        methods (map (fn [met]
                       {:method-name (.getName met)
                        :return-type (.getReturnType met)
                        :parameter-types (vec (.getParameterTypes met))}) methods-seq)]
    {:name (.getName cl)
     :interface (vec (.getInterfaces cl))
     :super-class (.getGenericSuperclass cl)
     :methods methods}))

;; expand the interfaces first then the super-class, i feel like expaxation of the super-class will be way more expensive

(defn make-tree [cl]
  (loop [already-expans #{}
         actual-to-expand [cl]
         tree []]
    (if (seq actual-to-expand)
      (let [next-expansion (expand-one-class (first actual-to-expand))
            next-cl-to-expand (set (conj
                                    (:interface next-expansion)
                                    (:super-class next-expansion)))
            already-expans (conj already-expans (first actual-to-expand))
            _ (println actual-to-expand)]
        (recur already-expans
               (remove nil? (concat (drop 1 actual-to-expand) next-cl-to-expand))
               (conj tree next-expansion)))
      tree)))

(defn make-tree-graph [cl]
  (let [tree (make-tree cl)]
    (map
     (fn [class]
       (let [from-interface (mapv (fn [interface]
                                    [(:name class) (.getName interface)])
                                  (:interface class))
             from-super-class [(if (:super-class class)
                                 [(:name class) (.getName (:super-class class)) {:color :blue}]
                                 [(:name class)])]]
         (concat from-interface from-super-class)))
     tree)))

(defn show-tree [class]
  (-> (apply concat (make-tree-graph class))
      d/digraph
      d/dot
      d/show!))

(defn save-tree [class & {:keys [format] :or {format :pdf}}]
  (-> (apply concat (make-tree-graph class))
      d/digraph
      d/dot
      (d/save! (str class ".pdf") {:format format})))