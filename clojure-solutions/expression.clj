(defn proto-get [obj key]
  (cond
    (contains? obj key) (obj key)
    (contains? obj :prototype) (proto-get (obj :prototype) key)
    :else nil))

(defn proto-call [this key & args]
  (apply (proto-get this key) this args))

(defn field [key]
  (fn [this] (proto-get this key)))

(defn method [key]
  (fn [this & args] (apply proto-call this key args)))

(defn constructor [ctor prototype]
  (fn [& args] (apply ctor {:prototype prototype} args)))

(defn Operation [this op f dx & args] (assoc this
                                        :op op
                                        :f f
                                        :dx dx
                                        :args args
                                        ))

(def _op (field :op))
(def _args (field :args))
(def _f (field :f))
(def _dx (field :dx))
(def toString (method :toString))
(def evaluate (method :evaluate))
(def diff (method :diff))
(def toStringInfix (method :toStringInfix))

(declare ZERO)
(declare ONE)

(def UnaryOperatorToStringInfix (fn [this] (str (_op this) "(" (#(toStringInfix %) (first (_args this))) ")")))

(def BinaryOperatorToStringInfix (fn [this] (let [[f & other] (_args this)] (reduce #(str "(" %1 " " (_op this) " " (toStringInfix %2) ")") (toStringInfix f) other))))

(def OperationsPrototype {
                          :toString (fn [this] (str "(" (_op this) " " (clojure.string/join " " (mapv #(toString %) (_args this))) ")"))
                          :evaluate (fn [this vars] (apply (_f this) (mapv #(evaluate % vars) (_args this))))
                          :diff (fn [this variable] (apply (_dx this) variable (_args this)))
                          })

(def VariablePrototype {
                        :toString (fn [this] (_args this))
                        :evaluate (fn [this vars] (vars (_args this)))
                        :diff (fn [this variable] (if (= (_args this) variable)
                                                    ONE
                                                    ZERO))
                        :toStringInfix toString
                        })

(def ConstantPrototype {
                        :toString (fn [this] (format "%.1f" (double (_args this))))
                        :evaluate (fn [this vars] (_args this))
                        :diff (fn [this variable] ZERO)
                        :toStringInfix toString
                        })

(def const (constructor Operation  (assoc OperationsPrototype :toStringInfix BinaryOperatorToStringInfix)))
(def unaryConst (constructor Operation (assoc OperationsPrototype :toStringInfix UnaryOperatorToStringInfix)))

(defn Constant [arg] {:prototype ConstantPrototype :args arg})
(defn Variable [arg] {:prototype VariablePrototype :args arg})

(def ZERO (Constant 0))
(def ONE (Constant 1))
(def E (Constant Math/E))

(def Negate (partial unaryConst "negate" - #(Negate (diff %2 %1))))

(def Add (partial const "+" + #(apply Add (mapv (fn [x] (diff x %1)) %&))))
(def Subtract (partial const "-" - #(apply Subtract (mapv (fn [x] (diff x %1)) %&))))
(def Multiply (partial const "*" *
                       (fn [x & args] (if (empty? args)
                                        ZERO
                                        (let [[a & b] args]
                                          (Add (apply Multiply (diff a x) b)
                                               (Multiply a (diff (apply Multiply b) x))))))

                       ))
(def Divide (partial const "/"

                     (fn [a & b] (/ (double a) (apply * b)))

                     (fn [x a & b] (if (empty? b)
                                     (Negate (Divide (diff a x) (Multiply a a)))
                                     (Divide
                                       (Subtract
                                         (apply Multiply (diff a x) b)
                                         (Multiply (diff (apply Multiply b) x) a))
                                       (apply Multiply (map #(Multiply % %) b)))))

                     ))

(defn pw [x n]
  (Math/pow x n))

(defn lg [base x]
  (/ (Math/log (Math/abs x)) (Math/log (Math/abs base))))

(def Lg (partial const "lg" lg
                 (fn [v base x] (Divide (Subtract (Multiply (Divide ONE x) (diff x v) (Lg E base))
                                                  (Multiply (Lg E x) (Divide ONE base) (diff base v))) (Multiply (Lg E base) (Lg E base))))))


(def Pw (partial const "pw" pw
                 (fn [v x n] (Multiply (Pw x n)
                                       (Add (Multiply (diff n v) (Lg E x))
                                            (Multiply n (Divide ONE x) (diff x v))))
                   )))

(def Xor (partial const "^" #(Double/longBitsToDouble (bit-xor (Double/doubleToLongBits %1) (Double/doubleToLongBits %2)))
                  (constantly [nil])))

(def And (partial const "&" #(Double/longBitsToDouble (bit-and (Double/doubleToLongBits %1) (Double/doubleToLongBits %2)))
                  (constantly [nil])))

(def Or (partial const "|" #(Double/longBitsToDouble (bit-or (Double/doubleToLongBits %1) (Double/doubleToLongBits %2)))
                 (constantly [nil])))


(def bijection {
                '+      Add
                '-      Subtract
                '*      Multiply
                '/      Divide
                'negate Negate
                'pw     Pw
                'lg     Lg
                (symbol "^")      Xor                     ;(
                '|      Or
                '&      And
                })

(defn parse [s] (cond
                  (seq? s) (apply (bijection (first s)) (map parse (rest s)))
                  (symbol? s) (Variable (str s))
                  (number? s) (Constant s)))

(defn parseObject [s] (parse (read-string s)))
(def exp (diff (Pw (Variable "x") (Variable "y")) "x"))

(load-file "parse_lib.clj")


(def *digit (+char "0123456789"))
(def *space (+char "\n\r\t "))
(def *ws (+ignore (+star *space)))
(def *dot (+char "."))
(def *number (+map (comp Constant read-string)
                   (+seqf str *ws (+opt (+char "-")) (+str (+plus *digit)) (+opt *dot) (+str (+opt (+plus *digit))))))
(def *variable (+map (comp Variable str) (+seqn 0 *ws (+char "xyz"))))
(def *char #(+seqn 0 *ws (+char %)))
(def *symbol #(+map (comp symbol str) (*char %)))
(def *open (+ignore (*char "(")))
(def *close (+ignore (*char ")")))
(declare *prim)
(def *negate (+map #(Negate %) (+seqn 1 *ws (+string "negate") (delay *prim))))

(declare *expression)

(defn *read [*op *next]
  (+map #(reduce (fn [a b] ((bijection (nth b 0)) a (nth b 1))) (first %) (last %))
        (+seq *next (+star (+seq *op *next)))))


(def *prim (+or *number *variable *negate (+seqn 0 *open (delay *expression) *close)))
(def *MulDiv (*read (*symbol "*/") *prim))
(def *AddSub (*read (*symbol "+-") *MulDiv))
(def *And (*read (*symbol "&") *AddSub))
(def *Or (*read (*symbol "|") *And))
(def *Xor (*read (*symbol "^") *Or))

(def *expression *Xor)

(def parseObjectInfix (+parser (+seqn 0 *expression *ws)))
