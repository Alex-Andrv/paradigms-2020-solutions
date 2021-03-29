map_build([], null) :- !.

map_build([(Kay, Value) | T], TreeMap) :- print(T), map_build(T, OldTree), map_put(OldTree, Kay, Value, TreeMap).

map_get(null, Key, Val) :- !, fail.
map_get(node(x(Key, Value), Y, Left, Right), Key, Value) :- !.
map_get(node(x(Key, Value), Y, Left, Right), NewKey, NewVal) :- Key < NewKey, !, map_get(Right, NewKey, NewVal).
map_get(node(x(Key, Value), Y, Left, Right), NewKey, NewVal) :- Key > NewKey, !, map_get(Left, NewKey, NewVal).


split(null, K, null, null) :- !.
split(node(x(Key, Value), Y, Left, Right), K, node(x(Key, Value), Y, Left, NewRight), Tree2) :- Key < K, !, split(Right, K, NewRight, Tree2).
split(node(x(Key, Value), Y, Left, Right), K, Tree1, node(x(Key, Value), Y, NewLeft, Right)) :- !, split(Left, K, Tree1, NewLeft).



merge(null, OldTree2, OldTree2) :-!.
merge(OldTree1, null, OldTree1) :-!.
merge(node(x(Key1, Value1), Y1, Left1, Right1),  node(x(Key2, Value2), Y2, Left2, Right2), node(x(Key1, Value1), Y1, Left1, NewRight)) :- Y1 > Y2, !, merge(Right1, node(x(Key2, Value2), Y2, Left2, Right2), NewRight).
merge(OldTree1,  node(X, Y, L, R), node(X, Y, NewLeft, R)) :- !, merge(OldTree1, L,  NewLeft).

insert(null, X, Result) :- !, rand_int(2147483647, Y), Result = node(X, Y, null, null).
insert(TreeMap, x(Key, Value), Result) :- split(TreeMap, Key, Tree1, Tree2), rand_int(2147483647, Y),
merge(node(x(Key, Value), Y, null, null), Tree2, Temp), merge(Tree1, Temp, Result). 


map_put(TreeMap, Key, Value, Result) :- map_get(TreeMap, Key, _), !, map_remove(TreeMap, Key, Temp), insert(Temp, x(Key, Value), Result).
map_put(TreeMap, Key, Value, Result) :- !, insert(TreeMap, x(Key, Value), Result).

map_remove(TreeMap, Key, Result) :- !.

map_remove(null, X, null) :- !.
map_remove(TreeMap, x(Key, _), Res) :- split(TreeMap, Kay, Tree1, Tree2), 
Key1 is Key + 1, split(Tree1, X1, Tree21, Tree22), merge(Tree1, Tree22, Res).