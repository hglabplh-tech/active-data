# The realm definitions possible in detail with examples
Starting with the project I missed a shorthand explanation how to define realms to make it easier for all who start using `àctive-data` I like to geve a short overview: 

## Primitive realms:

- **String: realm/string**

```clojure
(defn my-fun-str :- realm/any ;;function can return any value type 
  [out-string :- realm/string] ;; the argument is only accepted if it is a string 
  (print "I am the out string:" out-string) ;; print the argument
  true)
(my-fun-str "I am a computer scientist")
```
- **Integer: realm/integer**

```clojure
(defn my-fun-int :- realm/integer ;;function can return any value type 
  [label :- realm/string
   op1 :- realm/integer
   op2 :- realm/integer] ;; the argument is only accepted if it is a integer
  (let [result (* op1 op2)]
    (print "I am the op1 %d and op2 %d:" op1 op2) ;; print the arguments
    (print label ": %d" result)
    result))
(my-fun-int 7 8)
```

- **Number: realm/number**

```clojure
(defn my-fun-num :- realm/number ;;function can return any value type 
  [op1 :- realm/number
   op2 :- realm/number] ;; the argument is only accepted if it is a number
  (let [result (* op1 op2)]
    (print "I am the op1 %d and op2 %d:" op1 op2) ;; print the arguments
    (print "Result of multiplication: %d" result)
    result)) 
```

- **Natural: realm/natural**

```clojure
(defn my-fun-nat :- realm/natural ;;function can return any value type 
  [op1 :- realm/natural
   op2 :- realm/natural] ;; the argument is only accepted if it is a natural number
  (let [result (* op1 op2)]
    (print "I am the op1 %d and op2 %d:" op1 op2) ;; print the arguments
    (print "Result of multiplication: %d" result)
    result)) 
```
- **Real: realm/real**

```clojure
(defn my-fun-real :- realm/real ;;function can return any value type 
  [op1 :- realm/real
   op2 :- realm/real] ;; the arguments are only accepted if they are real numbers
  (let [result (* op1 op2)]
    (print "I am the op1 %d and op2 %d:" op1 op2) ;; print the arguments
    (print "Result of multiplication: %d" result)
    result))
(my-fun-real 0.98 6.78)
```

- **Boolean: realm/boolean**

```clojure
(defn my-fun-bool :- realm/boolean ;;function can return any value type 
  [op1 :- realm/boolean ;; boolean value false / true
   op2 :- realm/boolean] ;; the argument is only accepted if it is a boolean
  (let [result (or (not op1) op2)]
    (print "I am the op1 %s and op2 %s:" op1 op2) ;; print the arguments
    (print "Result of logical operation: %s" result) ;; print result
    result))
(my-fun-bool true true)
```

- **Symbol: realm/symbol**

```clojure
(defn my-fun-sym :- realm/symbol ;;function can return any value type 
  [fun-sym :- realm/symbol] ;; the argument is only accepted if it is a symbol
  (let [result (= fun-sym 'my-fun)]
    (pprint result) ;; pretty print the result
    (pprint fun-sym) ;; pretty print the given symbol
    fun-sym))

(my-fun-sym 'any-sym)
```

- **Keyword: realm/keyword**

```clojure
(defn my-fun-keyword :- realm/keyword ;;function can return any value type 
  [fun-key :- realm/keyword] ;; the argument is only accepted if it is a symbol
  (let [result (= fun-key :my-fun)]
    (pprint result) ;; pretty print the result
    (pprint fun-key) ;; pretty print the given key
    fun-key))
(my-fun-keyword :any-key)
```
- **UUID: realm/uuid**

```clojure
(import (java.util UUID))
(defn my-fun-uuid :- realm/uuid ;;function can return any value type 
  [key-id :- realm/uuid] ;; the argument is only accepted if it is a valid UUID  
  (pprint result)
    (pprint key-id) ;; pretty print the UUID argument
    key-id)
(my-fun-uuid (UUID/toString (UUID/random)))
```

## Tuple/Enum definition:

- **Enum: (realm/enum..... )**
```clojure
(defn test-enum :- realm/boolean
      [input :- (realm/enum :start :resume :finish :exit)] 
  ;; the argument is only accepted if the value is in the range of the enum
  (let [result (= input :exit)]
    result))
(test-enum :resume) ;; correct call with enum
```

- **Tuple: (realm/tuple realm1 realm2)**
```clojure
(defn test-tuple :- realm/boolean
  [input :- (realm/tuple realm/string realm/integer)] 
  ;; argument is only accepted if it is a tuple -> tuple(string, integer)
  (pprint input) ;; pretty print the tuple
  false
  )
(test-tuple ["myint" 23]) 
;; correct call - like a sequence but all times two members with 
;; specified realms
```

## Collection enums:

- **Set: (realm/set-of realm)**

```clojure
(defn setof-fun :- realm/any
    [the-set :- (realm/set-of (realm/enum :here :there :then :when))] 
  ;; only valid and accepted if the argument is a set of this enum given 
  (pprint the-set) ;; print the given set
  )
(setof-fun #{:here :there}) ;; a correct call
```

- **Sequence: (sequence-of realm)**
```clojure
(defn seqof-fun :- realm/any
  [the-seq :- (realm/sequence-of realm/real)]
  ;; only valid and accepted if in this case the argument is a sequence of real numbers.
  (pprint the-seq))
(seqof-fun [3.56 7.987]) ;; a correct call
```
- **Map: (realm/map-of key-realm value-realm)**

```clojure
(defn mapof-fun :- realm/any
  [the-map :- (realm/map-of realm/integer (realm/set-of realm/string))]
  ;; only accepted if the argument is a map with a key type of integer and a value type, 
  ;; of a set of strings
  (pprint the-map) 
  (mapof-fun {5 #{"hello" "that" "is" "cool"}})
  )
```

## Record realm

- **Record: (realm/compile record)**
- 
```clojure
(def-record Calculator
            [operation  (realm/function realm/number realm/number -> realm/number)])
(def calc-realm
  'Calculator
  (fn [op] (Calculator operation op))
  (fn [value] (record/is-a? Calculator value))
  [realm/field
   'operation
   (realm/function realm/number realm/number -> realm/number)])
;; defined a realm for the record

(def calc-realm-compiled
  (realm/compile Calculator))
;; compile the whole thing to get a realm out of the record
(def calc-realm-rec
  (realm/record->record-realm Calculator)) 
;;a special compile for records 
(defn plus :- realm/number
  [op1 :- realm/number
   op2 :- realm/number]
  (+ op1 op2))
(def calculator
  (Calculator operation plus))
((operation calculator) 4.5 6.7)
(realm/contains? calc-realm calculator) ;;-> true
```

## Function realm
- **Function: (realm/function realm/number realm/number -> realm/number)**
```clojure
(defn function-fun :- realm/number 
  [op-fun :- (realm/function realm/number realm/number -> realm/number)
   op1 :- realm/number
   op2 :- realm/number]
  )
(realm/restricted)
(defn plus :- realm/number
  [op1 :- realm/number
   op2 :- realm/number]
  (+ op1 op2))
(function-fun plus 9.8 7.7)
```

## REALM restrict

```clojure

```