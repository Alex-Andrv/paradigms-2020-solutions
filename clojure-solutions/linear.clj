(defn elementwise-operation [fun]
  (fn [& coll] (apply (fn [& args] {:pre [(reduce
                                (fn [cond mat] (and cond
                                                    (== (count mat) (count (nth args 0)))
                                                    (vector? mat)))
                                true args)]
                        :post [(== (count %) (count (nth args 0)))]
                        } (apply mapv fun args)) coll)))

(defn multiplication [fun] {
                             :pre [(fn? fun)]
                            } #(reduce
                              (fn [first second] (mapv (fn [el] (fun el second)) first))
                              %1 %&))

(def v+ (elementwise-operation +))
(def v- (elementwise-operation -))
(def v* (elementwise-operation *))

(def m+ (elementwise-operation v+))
(def m- (elementwise-operation v-))
(def m* (elementwise-operation v*))

(def v*s (multiplication *))

(defn m*s [m & s] {
                   :pre [(vector? m)]
                   :post [(== (count %) (count m))]
                   } (mapv #(apply v*s % s) m))


(defn transpose [m] {
                     :pre [(vector? m)]
                     :post [(vector? %)]
                     } (apply mapv vector m))

(defn scalar [& xs] {
                     :pre [(> (count xs) 0)]
                     :post [(number? %)]
                     } (apply + (apply v* xs)))

(def m*v (multiplication scalar))

(defn m*m [& mm] (reduce #(mapv (fn [n] (mapv (fn [m] (scalar n m)) (transpose %2))) %1) mm))


(defn vect [& vm] {
                   :pre [(reduce #(and %1 (vector? %2) (== (count %2) 3)) true vm)]
                   :post [(== (count %) 3)]
                   } (reduce #(let [[a1 a2 a3] %1 [b1 b2 b3] %2]
                                [(- (* a2 b3) (* a3 b2)),
                                 (- (* a3 b1) (* a1 b3)),
                                 (- (* a1 b2) (* a2 b1))]) vm))

(defn helper [fun] {
                     :pre [(fn? fun)]
                    } (fn [& vm] (apply
                                      (fn [& vv] (if (vector? (nth vv 0)) (apply mapv
                                                                                 (helper fun) vv) (apply fun vv))) vm)))

(def s+ (helper +))
(def s* (helper *))
(def s- (helper -))
