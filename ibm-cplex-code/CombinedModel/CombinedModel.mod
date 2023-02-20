/*********************************************
 * OPL 22.1.0.0 Model
 * Author: ben
 * Creation Date: Jan 10, 2023 at 11:01:06 AM
 *********************************************/

/* Data Structures */
tuple Flight {
  key int iFlightNumber;
  int iArrivalTime;
  int iDepartureTime;
  float iOnGateTime;
  float iOffGateTime;
};

{Flight} F = ...;
int NF = card(F);

int NG = ...;

// Objective 1 - Minimise flights not assigned to any terminal gate and therefore assigned to the apron/dummy gate
// Objective 2 - Minimise total passenger walking distance
// Objective 3 - Minimise weighted sum of time gap variables, and total number of remote allocations,
//				 created with objective of maximising time gaps between allocations


