/*********************************************
* OPL 22.1.0.0 Model
* Author: Ben
* Creation Date: Mar 3, 2023 at 10:45:12 AM
*********************************************/

execute {
    //cplex.tilim = 5*60;   // set time model stop (second)
    cplex.symmetry = 5;
    cplex.NodeFileInd = 3;
}

// Variables for initialising arrays with ints and ranges
int NF = ...;
int NG = ...;
range F = 1..NF;
range G = 1..NG;

// Decision variable assignment, 1 if f_i allocated to g_k, 0 otherwise
dvar int assignment[F,G] in 0..1;
// Indicator variable y, 1 if f_i and f_j allocated to g_k, 0 otherwise
dvar int y[F,F] in 0..1;
// Indicator variable, set to the time equation in constraint
dvar int vehiclePenalty;

// Two arrays holding the arrival and turnaround departure epochs
float ARRIVAL_TIMES[F] = ...;
float DEPART_TIMES[F] = ...;

// Array holding the gate distance between two gates in seconds
int GATE_DISTANCE[G,G] = ...;

// Hard coded integer holding a slack for assigning vehicle teams
int slackTime = ...;
int serviceTime = ...;

// Objective function to minimize the number of gate conflicts, dependent
// on gate assignment and scheduled time
minimize
sum (i in F, j in F : DEPART_TIMES[j] - ARRIVAL_TIMES[i] > 0) (
    y[i,j] / (DEPART_TIMES[j] - ARRIVAL_TIMES[i])
) + vehiclePenalty;

constraints {

    /* -- Gate constraints -- */

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
        y[i,k]*y[j,k]*(DEPART_TIMES[i] - ARRIVAL_TIMES[j])*(DEPART_TIMES[j] - ARRIVAL_TIMES[i]) <= 0;
    }

    /* -- Vehicle cosntraints -- */

    // The arrival time of flight i, plus the travel and service time, should be greater than that of the next flight arrival.    
    forall(i in F, j in F : i<j, k in G, l in G : k<l) {
      (ARRIVAL_TIMES[i]-ARRIVAL_TIMES[j])+((assignment[i,k]*assignment[j,l])*GATE_DISTANCE[k,l]) + ((assignment[i,k]*assignment[j,l])*(serviceTime+slackTime)) <= vehiclePenalty;
    }
}

// Write solution to .csv
execute {
    var file = new IloOplOutputFile("solution.csv");
    file.writeln("Gate Assignment:")
    file.writeln(assignment);
    file.writeln("\ny (Indicator of i and j assigned):")
    file.writeln(y)
    file.writeln("\nVehicle Penalty:")
    file.writeln(vehiclePenalty)
    file.close();
}
