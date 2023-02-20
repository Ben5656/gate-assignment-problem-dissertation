/*********************************************
 * OPL 22.1.0.0 Model
 * Author: Ben
 * Creation Date: Nov 22, 2022 at 4:15:02 PM
 *********************************************/
 
/* Data Structures */
tuple Flight {
   key int iFlightID;
   float iOnGateTime;
   float iOffGateTime;
};

tuple Gate {
   key int iGateID;
};

/* Constants */
float iSG = ...; //min size of time gap between flight i and j
float iLG = ...; //max siez of time gap between flight i and j
int iDU = ...; //penalty for dummy gate
float iPenalty = iSG;

{Flight} F = ...;
{Gate} G = ...;
{Flight} OVERNIGHT;


int penaltyArrayLength = ...;
int iNumFlights = card(F);
int iNumGates = card(G);

/* Decision Variables */
dvar int X[1..iNumGates+1][1..iNumFlights] in 0..1;
// Indicator variable U
dvar int U[1..iNumGates][1..iNumFlights][1..iNumFlights] in 0..1;

float penalty[1..penaltyArrayLength][1..penaltyArrayLength] =  ...; 

/* Objective */
minimize 
	// Weighted sum of time gap variables and total remote allocations
	// Maximise time gaps between allocations
	// The second objective which minimises the number of
    // remote allocations has to be in the objective function otherwise all 
    // flights would immediately be allocated to the ’dummy gate’
    
	sum(i in 1..iNumFlights, j in 1..iNumFlights, m in 1..iNumGates) (
		penalty[i][j]*U[m][i][j]
		
	) + sum(i in 1..iNumFlights) (
		// Last gate is dummy	
		iDU*X[iNumGates][i]
	);

/* Constraints */
constraints {
  /* Constraint 1
     - Time gap between 2 flights Fi and Fj allocated to same gate Gk
     - If onGateTime of flight j is greater than onGateTime of flight i then minus the
       onGateTime of flight i from j
     - If onGateTime of flight i is greater than onGateTime of flight j then minus the
       onGateTime of flight j from i
     - Flights i and j cannot be the same.
//  */
//
//	forall(i in F){
//		forall(j in F){
//		  	(i.iFlightID != j.iFlightID) =>
//			(i.iOnGateTime >= j.iOffGateTime) =>
//			i.iOnGateTime-j.iOffGateTime;
//			
//		  	(i.iFlightID != j.iFlightID) =>
//			(j.iOnGateTime >= i.iOffGateTime) =>
//			j.iOnGateTime-i.iOffGateTime   
//	    }
//	}    
//  
  /* Constraint 2
  	 - If both flights i and j but there is only a small gap between allocation to k
  	   only set if the time gap between them is between SG and LG, min and max time gap
  */
  
  	 
  /* Constraint 3
  	 - Each of the flights allocated to only one gate
  */
  forall(i in 1..iNumFlights) {
  	sum(k in 1..iNumGates+1) (X[k][i]) == 1;
  }
  
  // Constraint 4 - skipped as Ursula stated was redundant upon further testing
  
  /* Constraint 5
  	 - Guarantee two flights which overlap are within minimum gap SG of each other are not
  	   allocated to same gate
  */
  forall(i in 1..iNumFlights) {
    forall(j in 1..iNumFlights) {
      forall(k in 1..iNumGates) { 
  		X[k][i]+X[k][j] <= 1;
  		
  		iSG >= 0;
      }
    }
  }
  
  
  /* Constraint 6
  	 - Allocates Overnight flight stays, currently achieved programatically
  	 - Skipped for now.
  */
  
 
}
