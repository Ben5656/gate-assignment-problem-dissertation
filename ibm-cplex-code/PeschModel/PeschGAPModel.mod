/*********************************************
 * OPL 22.1.0.0 Model
 * Author: Ben
 * Creation Date: Nov 2, 2022 at 2:31:01 PM
 *********************************************/

/* ------CONSTANTS------ */
 
// Number of flights and gates respectively
int n = 5;
int m = 3;

// Range of flights from 0 to 4
range flightNum = 0..n-1;
// Range of gates from 1 to 5, allows gate 0 and m + 1 for the two dummy gates required.
range gateNum = 0..m+1;

tuple Flight {
   key int iFlightID; // ID per flight
   int iArrivalTime; // Aririval time as integer
   int iDepartureTime; // Departure time as integer
};

// Array of flight tuples, IDs, arrival times and departure respectively 
Flight N[flightNum] = [<1, 1, 3>, <2, 2, 4>, <3, 3, 5>, <4, 4, 6>, <5, 5, 7>];

// Separate integer arrays for flight/gate information
//int iPassengerTransfer[1..5][1..5] = [[0, 1, 2, 3, 4], [1, 0, 2, 3, 4], [1, 2, 0, 3, 4], [1, 2, 3, 0, 4], [1, 2, 3, 4, 0]];
//int iWalkingDistance[1..3][1..3] = [[0, 5, 10], [5, 0, 5], [10, 5, 0]];

// Binary decision variable provides implementation of 3rd constraint in paper
dvar int y[flightNum][gateNum] in 0..1;

/* ------OBJECTIVES------ */

// Objective 1
minimize sum(i in flightNum) (
    y[i][m+1]
);

// Objective 2
//minimize {
//  sum(i in N, j in N, k in 1..m+1, l in 1..m+1) {
//    passengerTransfer[i][j] * k.iWalkingDistance[l] * y[i][k] * y[j][l]
//
//  } + sum (i in N, k in 1..m+1) {
//    first(N).iPassengersTransferring[i] * first(M).iWalkingDistance[k] * y[i][k]
//  } + sum (i in N, k in 1..m+1) {
//    i.iPassengersTransferring[0] * k.iWalkingDistance[0] * y[i][k] 
//  }
//};

/* ------CONSTRAINTS----- */
constraints {
    // Constraint One
    forall(i in flightNum) { 
        sum(k in gateNum) y[i][k] == 1;
    }
    
    // Constraint Two
    forall(i in flightNum) {
      forall(j in flightNum){
        forall(k in 1..m){
          (y[i][k] * y[j][k]) * (N[j].iDepartureTime - N[i].iArrivalTime) * (N[i].iDepartureTime - N[j].iArrivalTime) <= 0;
        }
      }
    }
};