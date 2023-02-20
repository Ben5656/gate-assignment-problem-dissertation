/*********************************************
 * OPL 22.1.0.0 Model
 * Author: Ben
 * Creation Date: Dec 15, 2022 at 12:24:12 PM
 *********************************************/
execute timeTermination {
    cplex.tilim = 5*60;   // set time model stop (second)
}
int NF = ...;
int NG = ...;
range F = 1..NF;
range G = 1..NG;

// Decision variable assignment, 1 if f_i allocated to g_k, 0 otherwise
// Indicator variable y, 1 if f_i and f_j allocated to g_k, 0 otherwise
dvar int assignment[F,G] in 0..1;
dvar int y[F,F] in 0..1;

float ARRIVAL_TIMES[F] = ...;
float DEPART_TIMES[F] = ...;

// Objective function to minimize the number of gate conflicts, dependent
// on gate assignment and scheduled time
minimize
    sum (i in F, j in F : ARRIVAL_TIMES[j] - DEPART_TIMES[i] > 0) (
        y[i,j] / (ARRIVAL_TIMES[j] - DEPART_TIMES[i])
    );
    
constraints {
    // Compute variable y, shows 1 if both flights i and j are allocated to the same gate
    forall(i in F, j in F : j<i, k in G) {
        assignment[i,k]*assignment[j,k] <= y[i,j];
    }
    
    // Each flight is assigned to one and only one gate.
    forall(i in F) {
        sum(k in G) assignment[i,k] == 1;
    }
    
    // Ensures one gate can only be assigned to one and only one flight at the same time
    forall(i in F, j in F, k in G : j<i){
        y[i,k]*y[j,k]*(ARRIVAL_TIMES[i] - DEPART_TIMES[j])*(ARRIVAL_TIMES[j] - DEPART_TIMES[i]) <= 0;
    }
}