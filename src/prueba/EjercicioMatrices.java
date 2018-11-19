package prueba;

import java.awt.Dialog.ModalExclusionType;
import java.util.Random;
import mpi.MPI;


public class EjercicioMatrices {
	
	
	
	public static void main(String[] args) {
		
		
		//----------> variables 
		int nroProceso,n,cantProcesos;
		double tiempoInicial,tiempoFinal,tiempoTotal;
		Random rand = new Random();
		n = 7 ;    //------> el numero de filas o columnas que tendra la matriz, sera NxN , TIENE QUE SER MULTIPLO DE 2
		int[][] datos = new int[n][n];   //---> creo la matriz datos
		
		tiempoInicial = MPI.Wtime();   //---> el tiempo inicial donde arranca el MPI
		args = MPI.Init(args);
		nroProceso = MPI.COMM_WORLD.Rank();   //---> el numero del proceso actual
		cantProcesos = MPI.COMM_WORLD.Size();   //---> el total de procesos que hay en total

		int tamanioBloque = (int) Math.round((double) n / (double) cantProcesos);  //---> round redondea el numero integer mas cercano
		int tamanioBloqueUltimo = n - (tamanioBloque * (cantProcesos-1));   //---> cuenta para calcular el tamaño de el ultimo bloque
		int indiceInfMatriz = 0;
		int indiceSupMatriz = tamanioBloque;
		
		//--->  Estas variables  tienen que ser arrays de una posicion
		Object arrayEnvio[] = new Object[cantProcesos];   //---> esta seria la inicializacion del bloque que se envia desde Po(la submatriz)
		Object arrayReceptor[] = new Object[1];  //----> inicializacion del bloque que se va a recibir por los Pn procesos
		int sumatoriaProcesoActual[] = new int[1];  //---> se inicializa la variable que va a contener la suma parcial de los elementos de la matriz 
		int sumaTotal[] = new int[1];  //----> se inicializa ls variable que va a contener la suma total de todos los bloques

		
		
		//---> Si es el proceso 0 entonces inicializa la matriz y reparte en submatrices
		if (nroProceso == 0) {
			System.out.println(" "); System.out.println(" ");
			System.out.println("----------------------------------------------------");
			System.out.println("                       COMIENZO                     ");
			System.out.println(" * Proceso Master     => "+ nroProceso);
			System.out.println(" * Numero de procesos => " + cantProcesos);
			System.out.println("----------------------------------------------------");
			
			//-------> inicializo la matriz con todos los valores
			System.out.println("-----------------------------------------------------");
			System.out.println("             GENERO MATRIZ CON DATOS                 ");
			for (int i = 0; i < n; i++) {
				System.out.print("    fila "+ i + "  =>   ");
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
			
			if ((cantProcesos % 2 == 0) && (n % 2 == 0)) {
				System.out.println("Tamaño bloque para el ultimo proceso: " + tamanioBloqueUltimo);
				System.out.println("----------------------------------------------------");
			}
			
			
			int contProcesadores = 0;
			
			while (indiceSupMatriz <= n) {

				int[][] subMat = new int[tamanioBloque][n];
				int contador = 0;   //---> se usa para avanzar de posicione en el bloque
				System.out.println(" ");
				System.out.println("          Submatriz para el proceso =>  "+ contProcesadores); 

				for (int i = indiceInfMatriz; i < indiceSupMatriz; i++) { // Controla los indices para cada bloque
					System.out.print("    fila "+i +"  => ");
					for (int j = 0; j < n; j++) {
						subMat[contador][j] = datos[i][j];
						System.out.print("["+subMat[contador][j]+"]");
					}

					contador++;
					System.out.println();
				}
				
				arrayEnvio[contProcesadores] = (Object) subMat;   //---> se envia como Object la submatriz porque sino no se puede enviar!! 
				int ultimoProceso = cantProcesos - 1; //---> obtengo el nro del ultimo proceso
				contProcesadores++;  //---->  Incrementa el numero del procesador
				
				if (contProcesadores != ultimoProceso) {
					//--->  si no es el ultimo procesador
					indiceSupMatriz += tamanioBloque;
					System.out.println(" - - - - -> " + indiceSupMatriz);
				} else {
					//--->  si es el ultimo procesador
					indiceSupMatriz = n;
					System.out.println(" - - - - -> " + indiceSupMatriz);
				}

				indiceInfMatriz += tamanioBloque;
				System.out.println(" - - - - -> " + indiceInfMatriz);
				//System.out.println("es el ultimo indice de la matriz wachoo -> "+indiceInfMatriz);
				
			}
		}
		
		
		//-----> el Scatter divide todo lo que le mando por diferentes secciones y se lo asigna uno a uno a cada proceso
		MPI.COMM_WORLD.Scatter(arrayEnvio, 0, 1, MPI.OBJECT, arrayReceptor, 0, 1, MPI.OBJECT, 0);
		
		int[][] matAux;   //---> una matriz auxiliar
		matAux = new int[tamanioBloque][n];
		
		//--------> Verifico que si es el numero impar entonces va a haber uno que tenga una submatriz mas chiquita que los otros
		if (nroProceso == 3) {
			tamanioBloque = tamanioBloqueUltimo;
			matAux = new int[tamanioBloque][n];
		}

		
		
		matAux = (int[][]) arrayReceptor[0];  //------> inicializa la matriz con el bloque que va recibir
		sumatoriaProcesoActual[0] = 0;   //------> Se inicializa la suma parcial que es donde se va a devolver el resultado total al proceso 0
		int resultado = 0;
		System.out.println(" ");
		System.out.println("          Submatriz para el proceso =>  "+ nroProceso);
		for (int i = 0; i < tamanioBloque; i++) {
			System.out.print("    fila "+i +"  => ");
			for (int j = 0; j < n; j++) {
				System.out.print("["+matAux[i][j] + "]" );
				resultado = matAux[i][j] % 10;  //----> calculo su mod
				sumatoriaProcesoActual[0] = sumatoriaProcesoActual[0] + resultado;
			}
			System.out.println();
		}

		
		
		System.out.println("  *   Sumatoria mod 10 de valores de proceso " + nroProceso + "   =>   " + sumatoriaProcesoActual[0]);
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println(" ");
				
		
		//-----> Le devuelve los resultados en la variable sumaTotal a el Proceso 0
		MPI.COMM_WORLD.Reduce(sumatoriaProcesoActual,0, sumaTotal,0,1,MPI.INT,MPI.SUM,0);		
		
		
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