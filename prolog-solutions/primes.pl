kill(RR, MAX_N, _) :- RR > MAX_N, !.

kill(RR, MAX_N, R) :- assert(composite_table(RR)), RR1 is RR + R, !, kill(RR1, MAX_N, R). 

find_composite(MAX, MAX, _) :- !.

find_composite(R, MAX, MAX_N) :- \+composite_table(R), !, RR is R * R, kill(RR, MAX_N, R), !, R1 is R + 1, find_composite(R1, MAX, MAX_N). 
find_composite(R, MAX, MAX_N) :- !, R1 is R + 1, find_composite(R1, MAX, MAX_N). 

init(MAX_N) :-
MAX is (truncate(sqrt(MAX_N)) + 1),
find_composite(2, MAX, MAX_N).

composite(NUM) :-
  composite_table(NUM).

prime(NUM) :-
  not(composite_table(NUM)).

get_prime(1, _, []) :- !.

get_prime(N, Pred, [H | T]) :- Pred =< H, get_prime(N1, H, T), !, \+composite_table(H),  N is N1 * H. 

get_devisors(1, _, [], _) :- !.
get_devisors(N, MAX, [N], MAX) :- !.

get_devisors(N, R, D, MAX) :- 0 is mod(N, R), \+composite_table(R), !, N1 is div(N, R), get_devisors(N1, R, D1, MAX), append([R], D1, D).  
get_devisors(N, Pred, D, MAX) :- !, Pred1 is Pred + 1, get_devisors(N, Pred1, D, MAX).

prime_divisors(N, D) :- number(N), !, MAX is (truncate(sqrt(N)) + 1), get_devisors(N, 2, D, MAX).

prime_divisors(N, D) :- get_prime(N, 1, D).

nth_prime(2, 3) :- !.

nth_prime(1, 2) :- !.

next_prime(N, P, N, P1) :- !, P is P1.
next_prime(N, P, N1, P1) :- PP is P1 + 2, \+composite_table(PP), !, NN is N1 + 1, next_prime(N, P, NN, PP), asserta((nth(NN, PP) :- !)).
next_prime(N, P, N1, P1) :- !, PP is P1 + 2, next_prime(N, P, N1, PP).

nth_prime(N, P) :- nth_prime(N1, P1), !, next_prime(N, P, N1, P1).