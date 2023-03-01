/*********************************************
		* OPL 22.1.0.0 Model
		* Author: Ben
		* Creation Date: Dec 15, 2022 at 12:24:12 PM
		*********************************************/

		execute {
		//cplex.tilim = 5*60;   // set time model stop (second)
		cplex.symmetry = 5;
		cplex.threads=1;
		cplex.NodeFileInd = 3;
		}

		int NF = ...;
		int NG = ...;
		int NV = ...;
		range F = 1..NF;
		range G = 1..NG;
		range V = 1..NV;

		// Decision variable assignment, 1 if f_i allocated to g_k, 0 otherwise
		dvar int assignment[F,G] in 0..1;
		// Indicator variable y, 1 if f_i and f_j allocated to g_k, 0 otherwise
		dvar int y[F,F] in 0..1;


		// Decision variable vehicle, 1 if f_i allocated to V_v, 0 otherwise
		dvar int vehicle[F,V] in 0..1;
		// Indicator variable x, 1 if f_i and f_j allocated to V_v, 0 otherwise
		dvar int x[F,F] in 0..1;

		float ARRIVAL_TIMES[F] = ...;
		float DEPART_TIMES[F] = ...;

		int VEHICLES[V] = ...;
		int GATE_DISTANCE[G,G] = ...;

		// Objective function to minimize the number of gate conflicts, dependent
		// on gate assignment and scheduled time
		minimize
		sum (i in F, j in F : DEPART_TIMES[j] - ARRIVAL_TIMES[i] > 0) (
		y[i,j] / (DEPART_TIMES[j] - ARRIVAL_TIMES[i])
		) + sum (i in F, j in F : DEPART_TIMES[j] - ARRIVAL_TIMES[i] > 0) (
		x[i,j] / (DEPART_TIMES[j] - ARRIVAL_TIMES[i])
		);

		// Next obj needs to minimise GATE_DISTANCE

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

		// Compute variable x, shows 1 if both flights i and j are allocated to the same vehicle
		forall(i in F, j in F : j<i, v in V) {
		vehicle[i,v]*vehicle[j,v] <= x[i,j];
		}

		// Each flight is assigned to one and only one vehicle
		forall(i in F){
		sum(v in V) vehicle[i,v] == 1;
		}

		// Ensures one vehicle can only be assigned to one and only one flight at the same time
		forall(i in F, j in F, v in V : j<i){
		x[i,v]*x[j,v]*(DEPART_TIMES[i] - ARRIVAL_TIMES[j])*(DEPART_TIMES[j] - ARRIVAL_TIMES[i]) <= 0;
		}
		}

		execute {
		var file = new IloOplOutputFile("solution.csv");
		file.writeln("Gate Assignment:")
		file.writeln(assignment);
		file.writeln("\ny (Indicator of i and j assigned):")
		file.writeln(y)
		file.writeln("\nVehicle Assignment:")
		file.writeln(vehicle);
		file.writeln("\nx (Indicator of i and j assigned):")
		file.writeln(x)
		file.close();
		}