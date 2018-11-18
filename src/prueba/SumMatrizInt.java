package prueba;

import java.util.Scanner;
import java.util.Random;
import mpi.MPI;

public class SumMatrizInt {
	
	public static void main(String[] args) {
		
		double tComienzo = MPI.Wtime();
		
		//---> variables 
		int idProceso, cantProcesos;
		
		args = MPI.Init(args);
		idProceso = MPI.COMM_WORLD.Rank();
		cantProcesos = MPI.COMM_WORLD.Size();
		Random rand = new Random();
			
		final int N = 4 ;    //------> 4014		
		int tamanioBloque = (int) Math.ceil((double) N / (double) cantProcesos);  //---> ceil lo usa para redondear el numero 
		int tamanioBloqueUltimo = N - (tamanioBloque * (cantProcesos-1));   //---> cuenta que usa para calcular el tamaño de cada bloque

		int[][] matrizA = new int[N][N];   //---> la matriz 
		int[][] matrizAux;   //---> una matriz auxiliar

		Object bloqueEnvio[] = new Object[cantProcesos];   //---> esta seria la inicializacion del bloque que se envia desde Po(la submatriz)
		Object bloqueRecepcion[] = new Object[1];  //----> inicializacion del bloque que se va a recibir por los Pn procesos

		int sumaParcial[] = new int[1];  //---> se inicializa la variable que va a contener la suma parcial de los elementos de la matriz 
		int sumaTotal[] = new int[1];  //----> se inicializa ls variable que va a contener la suma total de todos los bloques
		//int sumaCorrecta=0;
		int procesador = 0;
		int indiceInfMatriz = 0;
		int indiceSupMatriz = tamanioBloque;
		
		
		if (idProceso == 0) {
			
			System.out.println("----------------------------------------------------");
			System.out.println("----------------------------------------------------");
			System.out.println("Cantidad de procesos: " + cantProcesos);
			
			//-------> inicializo la matriz con todos los valores
			for (int i = 0; i < N; i++) {
				System.out.println("fila "+i);
				for (int j = 0; j < N; j++) {
					matrizA[i][j] = rand.nextInt(10);
					System.out.print("["+matrizA[i][j]+"] ");
					//sumaCorrecta+= matrizA[i][j];
				}
				System.out.println();
			}
			System.out.println("----------------------------------------------------");
			System.out.println("Tamaño bloque para procesos hasta (n-1): " + tamanioBloque);
			System.out.println("----------------------------------------------------");
			System.out.println("Tamaño bloque para el ultimo proceso: " + tamanioBloqueUltimo);
			System.out.println("----------------------------------------------------");
			
			while (indiceSupMatriz <= N) {

				int[][] bloque = new int[tamanioBloque][N];
				//System.out.println(size);
				int cont = 0;
				System.out.println("Matriz para el procesador: " + (procesador));

				for (int j = indiceInfMatriz; j < indiceSupMatriz; j++) { // Controla los indices para cada bloque
					System.out.println("fila "+j);
					for (int k = 0; k < N; k++) {
						bloque[cont][k] = matrizA[j][k];
						System.out.print("["+bloque[cont][k]+"]");
						//System.out.print(bloque[cont][k] + " ");
					}

					cont++;
					System.out.println();
				}

				System.out.println("----------------------------------------------------");
				// send matriz a procesadores
				System.out.println("guardo matriz para procesador: " + procesador);
				bloqueEnvio[procesador] = (Object) bloque;
				System.out.println("----------------------------------------------------");

				procesador++;

				if (procesador != (cantProcesos - 1)) {// si no es el ultimo procesador
					indiceSupMatriz += tamanioBloque;
				} else {
					indiceSupMatriz = N;
				}

				indiceInfMatriz += tamanioBloque;
				//System.out.println(indiceInfMatriz);
				
			}// Finaliza While
		
			
		}// finaliza if proceso 0
		
		
		//-----> el Scatter divide todo lo que le mando por diferentes secciones y se lo asigna uno a uno a cada proceso
		MPI.COMM_WORLD.Scatter(bloqueEnvio, 0, 1, MPI.OBJECT, bloqueRecepcion, 0, 1, MPI.OBJECT, 0);
		
		
		boolean marca=true;
		//------> Solo verifica que se haya enviado todo en el Scatter
		if (idProceso==0 && marca) {
			marca= false;
			System.out.println("matrices enviadas a cada proceso 'SCATTER'");
			System.out.println("----------------------------------------------------");
		}

		
		
		matrizAux = new int[tamanioBloque][N];
		//--------> Verifico que si es el numero impar entonces va a haber uno que tenga una submatriz mas chiquita que los otros
		if (idProceso == 3) {
			tamanioBloque = tamanioBloqueUltimo;
			matrizAux = new int[tamanioBloque][N];
		}

		
		matrizAux = (int[][]) bloqueRecepcion[0];
		sumaParcial[0] = 0;

		for (int i = 0; i < tamanioBloque; i++) {
			
			for (int j = 0; j < N; j++) {
				// System.out.print(matrizAux[i][j] + " " ); //System.out.println( "p=" + me);
				sumaParcial[0] = sumaParcial[0] + matrizAux[i][j];
			}
			
			//System.out.println();
			// System.out.println("------------------");
		}

		
		
		System.out.println("Resultado suma parcial hilo " + idProceso + ": " + sumaParcial[0]);
		System.out.println("----------------------------------------------------");
				
		
		//-----> Le devuelve los resultados en la variable sumaTotal a el Proceso 0
		MPI.COMM_WORLD.Reduce(sumaParcial, 0, sumaTotal, 0, 1, MPI.INT, MPI.SUM, 0);		
		
		
		//-------> marca para verificar que el reduce se hizo hacia el proceso 0
		marca=true;
		if (idProceso==0 && marca) {
			marca= false;
			System.out.println("recepcion sumas parciales procesos a proceso 0 'REDUCE'");
			System.out.println("----------------------------------------------------");
		}
		
		
		//-----> marca de tiempo final para medir el tiempo
		double tFinal = MPI.Wtime();
		
		//----> Calcula el tiempo que tardo en hacerse el MPI
		if (idProceso == 0) {
			//System.err.println("      Suma esperada: " + sumaCorrecta);
			System.out.println("     Suma calculada: " + sumaTotal[0] + " y el tiempo es: "
					+ String.valueOf(tFinal - tComienzo) + " segundos") ;
			
		}
		
		
		//---> termina el MPI
		MPI.Finalize();
		
	}
	
	
}