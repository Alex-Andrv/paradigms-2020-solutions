prime_table(2, 1).

loop(MAX, MAX, _) :- !.

loop(R, MAX, NUM) :-
eratosfen(R, NUM, NUM1), !,
R1 is R + 2,
loop(R1, MAX, NUM1).


check(N, R, NUM, NUM1) :- N * N =< R -> 0 is mod(R, N), NUM1 is NUM, !; NUM1 is NUM + 1, assert(prime_table(R, NUM1)), !.

eratosfen(R, NUM, NUM1) :- prime_table(N, _), check(N, R, NUM, NUM1), !.

init(MAX_N) :-
MAX is MAX_N + 1,
loop(3, MAX, 2).

nth(N, P) :- prime_table(N, P).

get_divisors(1, _, []) :-!.

get_divisors(N, Pred, D) :- prime_table(H, _), Pred =< H, 0 is mod(N, H), !, N1 is div(N, H),
get_divisors(N1, H, D1), append([H], D1, D).


prime_divisors(N, Divisors) :- number(N), !, get_divisors(N, 1, Divisors).

check_divisors(1, _, []).

check_divisors(N, Pred, [H | T]) :- !, Pred =< H, prime_table(H, _), check_divisors(N1, H, T), N is N1 * H.

prime_divisors(N, Divisors) :- check_divisors(N, 1, Divisors).

prime(R) :- prime_table(R, _), !.

composite(R) :- not(prime(R)), !.