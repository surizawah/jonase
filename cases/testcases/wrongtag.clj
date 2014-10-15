(ns testcases.wrongtag
  (:import (java.util LinkedList))
  (:require [clojure.test :refer :all]))

;; These two cases should cause a warning, because Clojure 1.6.0
;; treats the tag as if it is the function clojure.core/long.
(def ^long lv1 -2)
(def ^{:tag long} lv2 -2)

;; This is a correct way to tag it.  Should have no warning
(def ^{:tag 'long} lv3 -2)

;; These kinds of tags do not have to be restricted to long or double
;; the way that primitive arguments and return types of functions do.
;; They are only used for avoiding reflection in Java interop calls,
;; not to control the type of object that is used to store the value.

;; Warnings for these, similar to above cases with warnings
(def ^int iv1 -2)
(def ^{:tag int} iv2 -2)
;; No warnings
(def ^{:tag 'int} iv3 -2)
(def ^{:tag 'bytes} bvv1 (byte-array [1 2 3]))

;; Similarly for tags on Vars whose values are functions.  Again, such
;; tags do not cause the return type of the function created to be a
;; primitive.  They are simply used to avoid reflection wherever the
;; return value of the function is used in a Java interop call.

;; Warnings for these
(def ^long lf1 (fn [x y] (+ x y)))
(defn ^long lf2 [x y] (+ x y))
(def ^{:tag long} lf3 (fn [x y] (+ x y)))
(defn ^{:tag long} lf4 [x y] (+ x y))
(def ^int if1 (fn [x y] (+ x y)))
(defn ^int if2 [x y] (+ x y))
(def ^{:tag int} if3 (fn [x y] (+ x y)))
(defn ^{:tag int} if4 [x y] (+ x y))

;; No warnings for these
(def ^{:tag 'long} lf5 (fn [x y] (+ x y)))
(defn ^{:tag 'long} lf6 [x y] (+ x y))
(def ^{:tag 'int} if5 (fn [x y] (+ x y)))
(defn ^{:tag 'int} if6 [x y] (+ x y))


;; When the return type tag is on the arg vector, if it is a primitive
;; it must be long or double or the Clojure compiler will give an
;; error.

;; No warnings
(def plf1 (fn ^long [x y] (+ x y)))
(defn plf2 ^long [x y] (+ x y))
(def pdf1 (fn ^double [x y] (+ x y)))
(defn pdf2 ^double [x y] (+ x y))

;; When the return type tag is on the arg vector and it is a Java
;; class, Clojure can handle any class name just fine when defining
;; the function, and wherever calling it, except in one case described
;; in CLJ-1232:

;; If the class is not imported by default into Clojure (it imports
;; those in java.lang by default), but it is imported where the
;; function is defined, and the tag is on the arg vector, and it is
;; not fully qualified, and the function is called from a different
;; namespace where the class is not imported, then Clojure 1.6.0
;; compiler will throw an exception where the call is, indicating that
;; the class is unknown.

;; Thus Eastwood will warn about function definitions like this, in an
;; effort to help the developer more quickly discover the solution.
;; The thing that works for now in all cases is to fully qualify the
;; class name where the function is defined.  This does not require
;; the caller of the function to import the class.

;; Eastwood avoids issuing a warning if the function is also declared
;; private, since there are cases of this in existing libraries, and
;; it is very unlikely that the error will be hit for such a function
;; (or perhaps even impossible).

;; Warn for these cases

;; wrong way to specify the type
(def avlf1 (fn ^{:tag 'LinkedList} [coll] (java.util.LinkedList. coll)))
(defn avlf2 ^{:tag 'LinkedList} [coll] (java.util.LinkedList. coll))
;; right way to specify the type, but not fully qualified, and public
(def avlf3 (fn ^LinkedList [coll] (java.util.LinkedList. coll)))
(defn avlf4 ^LinkedList [coll] (java.util.LinkedList. coll))

;; No warnings for these cases

;; fully qualified type, public
(def avlf5 (fn ^java.util.LinkedList [coll] (java.util.LinkedList. coll)))
(defn avlf6 ^java.util.LinkedList [coll] (java.util.LinkedList. coll))
(defn ^{:private false} avlf7 ^java.util.LinkedList [coll] (java.util.LinkedList. coll))
;; not fully qualified type, but private
(def ^:private avlf9 (fn ^LinkedList [coll] (java.util.LinkedList. coll)))
(defn ^:private avlf10 ^LinkedList [coll] (java.util.LinkedList. coll))
(def ^{:private true} avlf11 (fn ^LinkedList [coll] (java.util.LinkedList. coll)))
(defn ^{:private true} avlf12 ^LinkedList [coll] (java.util.LinkedList. coll))
(defn- avlf13 ^LinkedList [coll] (java.util.LinkedList. coll))

;; No warnings for these cases, either, strangely enough.  Clojure
;; never seems to throw an exception for non-fully qualified Java
;; class names as type tags on Vars naming functions, only in the
;; cases mentioned above when such a class name is used as a tag on
;; the argument vector.

(def ^LinkedList avlf15 (fn [coll] (java.util.LinkedList. coll)))
(defn ^LinkedList avlf16 [coll] (java.util.LinkedList. coll))
