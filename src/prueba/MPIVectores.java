package prueba;


import java.util.Random;

import mpi.MPI;

public class MPIVectores {

	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		double start = MPI.Wtime();
		int me,size;
		double sumaParcial[] = new double[1];
		double sumaTotal[]=new double[1];
		args = MPI.Init(args);
		me = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();
		Random rand = new Random();
		int data[]=new int[50000];
		int datosParciales[]=new int[12500];
		
		if (me==0) {
			//inicializo el vector como master con valores de 0 a 9 aleatorios
			for(int i=0;i<50000;i++) {
				data[i]=rand.nextInt(10);
			}
		
		}
		
		//divido los datos en cuatro partes iguales
		MPI.COMM_WORLD.Scatter(data, 0, 12499, MPI.INT, datosParciales, 0, 12499, MPI.INT, 0);
		
		//trabajo de cada hilo que pasa por aca	
		for(int i=0;i<12500;i++) {
			sumaParcial[0]=sumaParcial[0] + datosParciales[i];
		}
		
		System.out.println("Suma parcial hilo : " + me + " " + sumaParcial[0]);
		
		
		//devolver al master la reducción en suma de cada cuenta parcial
		MPI.COMM_WORLD.Reduce(sumaParcial, 0, sumaTotal, 0, 1, MPI.DOUBLE, MPI.SUM, 0);
		
		double end = MPI.Wtime();
		
		//solo el maestro imprime los valores
		if(me==0){
			System.out.println("La suma total es: " + sumaTotal[0] + " y el tiempo es: " + String.valueOf(end-start));
		}
		
		
	}
	
	

}
