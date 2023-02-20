/*********************************************
 * OPL 22.1.0.0 Model
 * Author: benrf
 * Creation Date: 2 Feb 2023 at 17:02:48
 *********************************************/

tuple Flight {
  key int iFlightNumber;
  int iArrivalTime;
  int iDepartureTime;
};

// Values
{Flight} F = ...;

// 2 gates - then an apron and passenger entrance/exit
int NG = 2; 

// Multiple ranges defined as different parts of the equation require
range NG_range = 1..NG+1;
range NG_range_no_apron = 1..NG;

// Transfers - TransferSelf, NumberTransferToOtherF1, NumberTransferToF2
int transfers[1..card(F)][1..card(F)] = [[0,2,2],[0,3,3],[0,4,4]];
// Gate Distance - DistanceSelf, DistanceOtherGate, DistanceDummy Also Apron Dummy Gate
int gateDistance[NG_range][NG_range] = [[0,5,0], [0,5,0], [0,0,0]];

// Binary decision variable
dvar int y[F][NG_range] in 0..1;

// Objective 1 - Minimise flights not assigned to any terminal gate and therefore assigned to the apron
// Objective 2 - Minimise total passenger walking distance
minimize 
	sum(i in F) (
		y[i][card(NG_range)]	

	) + sum (i in F, j in F, k in NG_range, l in NG_range) (
		transfers[i.iFlightNumber][j.iFlightNumber]*gateDistance[k][l]*(y[i][k])*(y[j][l])
		
	) + sum(i in F, k in NG_range) (
		transfers[1][i.iFlightNumber]*gateDistance[1][k]*y[i][k]
		
	) + sum(i in F, k in NG_range) (
		transfers[i.iFlightNumber][1]*gateDistance[k][1]*y[i][k]
		
	);

// Constraints
constraints {
  
  // Constraint 1 - Every flight assigned to exactly one gate, including apron
  forall(i in F){
    sum (k in NG_range) (y[i][k]) == 1;
  }
  
  // Constraint 2 - Flights cannot be assigned to the same gate if their time at the airport overlaps
  forall(i in F){
    forall(j in F){
      forall(k in NG_range_no_apron){
        (y[i][k])*(y[j][k])*(j.iDepartureTime-i.iArrivalTime)*(i.iDepartureTime-j.iArrivalTime) <= 0;
      }      
    }
  }
}