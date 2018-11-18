package prueba;

import java.util.Random;
import mpi.MPI;


public class EjercicioMatrices {
	
	
	
	public static void main(String[] args) {
		
		
		//----------> variables 
		int nroProceso,n,cantProcesos;
		double tiempoInicial,tiempoFinal,tiempoTotal;
		
		
		tiempoInicial = MPI.Wtime();   //---> el tiempo inicial donde arranca el MPI
		args = MPI.Init(args);
		nroProceso = MPI.COMM_WORLD.Rank();   //---> el numero del proceso actual
		cantProcesos = MPI.COMM_WORLD.Size();   //---> el total de procesos que hay en total
		n = 6 ;    //------> el numero de filas o columnas que tendra la matriz, sera NxN , TIENE QUE SER MULTIPLO DE 2
		int[][] datos = new int[n][n];   //---> creo la matriz datos
		Random rand = new Random();

		int tamanioBloque = (int) Math.ceil((double) n / (double) cantProcesos);  //---> ceil lo usa para redondear el numero 
		int tamanioBloqueUltimo = n - (tamanioBloque * (cantProcesos-1));   //---> cuenta que usa para calcular el tamaño de cada bloque

		
		int[][] matAux;   //---> una matriz auxiliar
		Object bloqueEnvio[] = new Object[cantProcesos];   //---> esta seria la inicializacion del bloque que se envia desde Po(la submatriz)
		Object bloqueRecepcion[] = new Object[1];  //----> inicializacion del bloque que se va a recibir por los Pn procesos

		int sumaParcial[] = new int[1];  //---> se inicializa la variable que va a contener la suma parcial de los elementos de la matriz 
		int sumaTotal[] = new int[1];  //----> se inicializa ls variable que va a contener la suma total de todos los bloques
	
		int procesador = 0;
		int indiceInfMatriz = 0;
		int indiceSupMatriz = tamanioBloque;
		

		
		
		if (nroProceso == 0) {
			System.out.println("----------------------------------------------------");
			System.out.println("                       COMIENZO                     ");
			System.out.println(" * Proceso Master     => "+ nroProceso);
			System.out.println(" * Numero de procesos => " + cantProcesos);
			System.out.println("----------------------------------------------------");
			
			//-------> inicializo la matriz con todos los valores
			System.out.println("-----------------------------------------------------");
			System.out.println("             GENERO MATRIZ CON DATOS                 ");
			for (int i = 0; i < n; i++) {
				System.out.print("fila "+ i + "  =>   ");
				for (int j = 0; j < n; j++) {
					datos[i][j] = rand.nextInt(10);
					System.out.print("["+datos[i][j]+"] ");
					//sumaCorrecta+= matrizA[i][j];
				}
				System.out.println();
			}
			System.out.println("-----------------------------------------------------");
			
			System.out.println("----------------------------------------------------");
			System.out.println("Tamaño bloque para procesos hasta (n-1): " + tamanioBloque);
			System.out.println("----------------------------------------------------");
			System.out.println("Tamaño bloque para el ultimo proceso: " + tamanioBloqueUltimo);
			System.out.println("----------------------------------------------------");
			
			while (indiceSupMatriz <= n) {

				int[][] bloque = new int[tamanioBloque][n];
				//System.out.println(size);
				int cont = 0;
				System.out.println("Matriz para el procesador: " + (procesador));

				for (int j = indiceInfMatriz; j < indiceSupMatriz; j++) { // Controla los indices para cada bloque
					System.out.println("fila "+j);
					for (int k = 0; k < n; k++) {
						bloque[cont][k] = datos[j][k];
						System.out.print("["+bloque[cont][k]+"]");
					}

					cont++;
					System.out.println();
				}

				System.out.println("----------------------------------------------------");
				
				// send matriz a procesadores
				
				System.out.println("guardo matriz para procesador: " + procesador);
				bloqueEnvio[procesador] = (Object) bloque;
				System.out.println("----------------------------------------------------");

				procesador++;  //----> 

				if (procesador != (cantProcesos - 1)) {
					//--->  si no es el ultimo procesador
					indiceSupMatriz += tamanioBloque;
				} else {
					//--->  si es el ultimo procesador
					indiceSupMatriz = n;
				}

				indiceInfMatriz += tamanioBloque;
				//System.out.println(indiceInfMatriz);
				
			}
		}
		
		
		//-----> el Scatter divide todo lo que le mando por diferentes secciones y se lo asigna uno a uno a cada proceso
		MPI.COMM_WORLD.Scatter(bloqueEnvio, 0, 1, MPI.OBJECT, bloqueRecepcion, 0, 1, MPI.OBJECT, 0);
		
		
		/*boolean marca=true;
		//------> Solo verifica que se haya enviado todo en el Scatter
		if (idProceso==0 && marca) {
			marca= false;
			System.out.println("matrices enviadas a cada proceso 'SCATTER'");
			System.out.println("----------------------------------------------------");
		}*/

		
		
		
		matAux = new int[tamanioBloque][n];
		//--------> Verifico que si es el numero impar entonces va a haber uno que tenga una submatriz mas chiquita que los otros
		if (nroProceso == 3) {
			tamanioBloque = tamanioBloqueUltimo;
			matAux = new int[tamanioBloque][n];
		}

		
		
		matAux = (int[][]) bloqueRecepcion[0];  //------> inicializa la matriz con el bloque que va recibir
		sumaParcial[0] = 0;   //------> Se inicializa la suma parcial que es donde se va a devolver el resultado total al proceso 0

		
		for (int i = 0; i < tamanioBloque; i++) {	
			for (int j = 0; j < n; j++) {
				 System.out.print("["+matAux[i][j] + "]" ); 
				sumaParcial[0] = sumaParcial[0] + matAux[i][j];
			}
			
		}

		
		
		System.out.println("Resultado suma parcial hilo " + nroProceso + ": " + sumaParcial[0]);
		System.out.println("----------------------------------------------------");
				
		
		//-----> Le devuelve los resultados en la variable sumaTotal a el Proceso 0
		MPI.COMM_WORLD.Reduce(sumaParcial, 0, sumaTotal, 0, 1, MPI.INT, MPI.SUM, 0);		
		
		
		//----> Calcula el tiempo que tardo en hacerse el MPI solo si es el proceso Master
		if (nroProceso == 0) {
			tiempoFinal = MPI.Wtime(); //-----> marca de tiempo final para medir el tiempo
			tiempoTotal = tiempoFinal - tiempoInicial;
			
			System.out.println("-------------------------------------------------------");
			System.out.println("                 RESULTADOS FINALES                    ");
			System.out.println("----->  Suma total: " + sumaTotal[0]);  //-----> se referencia sumaTotal[0] porque lo que devuele del reduce es un vector!!!
			System.out.println("----->  Tiempo: +" + String.valueOf(tiempoTotal));
			System.out.println("-------------------------------------------------------");
			
		}
		
		
		//---> termina el MPI
		MPI.Finalize();
		
	}
	
	
}