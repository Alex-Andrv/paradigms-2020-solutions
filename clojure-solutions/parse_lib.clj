(defn -return [value tail] {:value value :tail tail})
(def -valid? boolean)
(def -value :value)
(def -tail :tail)

(defn _show [result]
  (if (-valid? result) (str "-> " (pr-str (-value result)) " | " (pr-str (apply str (-tail result))))
                       "!"))
(defn tabulate [parser inputs]
  (run! (fn [input] (printf "    %-10s %s\n" (pr-str input) (_show (parser input)))) inputs))

(defn _empty [value] (partial -return value)) ;почему 1? а не nil. Потому что мы так выбрали value

(defn _char [p]
  (fn [[c & cs]]                                            ;возвращает nil
    (if (and c (p c)) (-return c cs))))


(defn _map [f result]                                       ;применяет функцию f к value, не парсер
  (if (-valid? result)
    (-return (f (-value result)) (-tail result))))


(defn _combine [f a b]                                      ;применяет парсер a а затем b
  (fn [str]
    (let [ar ((force a) str)]
      (if (-valid? ar)
        (_map (partial f (-value ar))
              ((force b) (-tail ar)))))))


(defn _either [a b]                                         ;применяет или парсер a или парсер b
  (fn [str]
    (let [ar ((force a) str)]
      (if (-valid? ar) ar ((force b) str)))))

(defn _parser [p]                                           ;парсер, который парсит всю строчку целиком и возвращает value,
  (fn [input]                                               ;если не получилось, то nill
    (-value ((_combine (fn [v _] v) p (_char #{\u0000})) (str input \u0000)))))


(defn +char [chars] (_char (set chars)))                    ;более удобные версии старых функций

(defn +char-not [chars] (_char (comp not (set chars))))


(defn +map [f parser] (comp (partial _map f) parser))       ;зменяем out парсера

(def +parser _parser)

(def +ignore (partial +map (constantly 'ignore)))           ;игнорит out арсера

(defn iconj [coll value]
  (if (= value 'ignore) coll (conj coll value)))
(defn +seq [& ps]
  (reduce (partial _combine iconj) (_empty []) ps))

(defn +seqf [f & ps] (+map (partial apply f) (apply +seq ps)))


(defn +seqn [n & ps] (apply +seqf (fn [& vs] (nth vs n)) ps))

(defn +or [p & ps]
  (reduce _either p ps))

(defn +opt [p]
  (+or p (_empty nil)))

(defn +star [p]
  (letfn [(rec [] (+or (+seqf cons p (delay (rec))) (_empty ())))] (rec)))

(defn +plus [p] (+seqf cons p (+star p))) ;хотябы 1 раз парсер должен отработать

(defn +str [p] (+map (partial apply str) p))

(defn +string [chars] (+str (apply +seq (map #(+char %) (map str (seq chars))))))


