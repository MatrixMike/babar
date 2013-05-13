(ns babar.parser
  (:require [instaparse.core :as insta]))


(def parser
  (insta/parser
   "expr = item | command | vector
    command = commandkey space vector
    commandkey = operation | special
    map = <'{'> ((space)* item (space)*)+ <'}'>
    <vector>  = svector | bvector
    svector = ((space)* item (space)*)+
    bvector =  <#'\\['> ((space)* item+ (space)*)+ <#'\\]'>
    <space> = <#'[\\s\\t\\n]+'>
    <item> = string / number / boolean / keyword / bvector / map / identifier
    <operation> =  '+' | '-' | '*' | '/'
    identifier =  #'[a-z][0-9a-zA-Z\\-\\_]*' !special
    <special> = 'def' | 'if'
    string =  <'\\\"'> #'([^\"\\\\]|\\\\.)*' <'\\\"'>
    keyword = <#'[:]'> #'\\w+'
    boolean = #'true' | #'false'
    number = integer | decimal
    <decimal> = #'-?[0-9]+\\.[0-9]+'
    <integer> = #'-?[0-9]+'"))


(defn babar-def [s v]
  `(def ~(symbol s) ~v))

(defn babar-if [v]
  (let [[test then else] (eval v)]
   (if test then else)))

(defn eval-operation [op vector]
  (apply op (eval vector)))

(defn eval-command [command vector]
  (case command
    "+" (eval-operation + vector)
    "-" (eval-operation - vector)
    "*" (eval-operation * vector)
    "/" (eval-operation / vector)
    "def" (eval (babar-def (str (first vector)) (second vector)))
    "if" (babar-if vector)))


(def transform-options
  {:number read-string
   :string str
   :keyword keyword
   :boolean read-string
   :svector (comp vec list)
   :bvector (comp vec list)
   :map hash-map
   :identifier read-string
   :commandkey identity
   :command eval-command
   :expr eval})

(defn parse [input]
  (->> (parser input) (insta/transform transform-options)))
