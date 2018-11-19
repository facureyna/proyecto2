package prueba;

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

		int tamanioBloqueNormal = (int) Math.round((double) n / (double) cantProcesos);  //---> round redondea el numero integer mas cercano
		int tamanioUltimoBloque = n - (tamanioBloqueNormal * (cantProcesos-1));   //---> cuenta para calcular el tama�o de el ultimo bloque
		
		//--->  Estas variables  tienen que ser arrays de una posicion
		Object arrayEnvio[] = new Object[cantProcesos];   //---> esta seria la inicializacion del bloque que se envia desde Po(la submatriz)
		Object arrayReceptor[] = new Object[1];  //----> inicializacion del bloque que se va a recibir por los Pn procesos
		int sumatoriaProcesoActual[] = new int[1];  //---> se inicializa la variable que va a contener la suma parcial de los elementos de la matriz 
		int sumaTotal[] = new int[1];  //----> se inicializa ls variable que va a contener la suma total de todos los bloques
		int indiceInicial = 0;   //---> arranca de cero y termina en donde termina el bloque de datos normal 
		int indiceFinal = tamanioBloqueNormal;
		
		
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
				}
				System.out.println();
			}
			
			System.out.println("----------------------------------------------------");
			System.out.println("Tama�o bloque para procesos hasta (n-1): " + tamanioBloqueNormal);
			
			if ((cantProcesos % 2 != 0) || (n % 2 != 0)) {
				System.out.println("Tama�o bloque para el ultimo proceso: " + tamanioUltimoBloque);
				System.out.println("----------------------------------------------------");
			}
			
			//---> inicializo el contador para los procesos
			int contProcesos = 0;
			
			while (indiceFinal <= n) {

				int[][] subMat = new int[tamanioBloqueNormal][n];
				int contador = 0;   //---> se usa para avanzar de posicione en el bloque
				System.out.println(" ");
				System.out.println("          Submatriz para el proceso =>  "+ contProcesos); 

				for (int i = indiceInicial; i < indiceFinal; i++) { // Controla los indices para cada bloque
					System.out.print("    fila "+i +"  => ");
					for (int j = 0; j < n; j++) {
						subMat[contador][j] = datos[i][j];
						System.out.print("["+subMat[contador][j]+"]");
					}

					contador++;
					System.out.println();
				}
				
				System.out.println(".:::  contador de procesadores : "+ contProcesos + "  :::.");
				arrayEnvio[contProcesos] = (Object) subMat;   //---> se envia como Object la submatriz porque sino no se puede enviar!! 
				int ultimoProceso = cantProcesos - 1; //---> obtengo el nro del ultimo proceso
				
				
				if (contProcesos == 0) {
					System.out.println(" - - - - -> Indice hasta donde tomo la submatriz => " + indiceInicial);
					System.out.println(" - - - - -> Indice desde donde tomo la submatriz => " + indiceFinal);
					System.out.println(".:::  contador de procesadores : "+ contProcesos + "  :::.");
				}
				
				contProcesos++;  //---->  Incrementa el numero del procesador
				System.out.println(".:::  contador de procesadores : "+ contProcesos + "  :::.");
				
				if (contProcesos != ultimoProceso) {
					//--->  no es el ultimo se asigna normal
					indiceFinal += tamanioBloqueNormal;
					System.out.println("tama�o de bloque => " + tamanioBloqueNormal);
					System.out.println(" no es el ultimo "+ (indiceInicial + tamanioBloqueNormal) +"  "+indiceFinal);
					//contProcesadores++;  //---->  Incrementa el numero del procesador
					//System.out.println(".:::  contador de procesadores : "+ contProcesadores + "  :::.");
					
				} else {
					//---> es el ultimo proceso no se le asigna normal , se asigna la ultima submattriz
					System.out.println("tama�o de bloque => "+ tamanioUltimoBloque);
					System.out.println("es el ultimo "+ indiceInicial +"  "+ indiceFinal);
					indiceFinal = n;	
					//contProcesadores++;  //---->  Incrementa el numero del procesador
					//System.out.println(".:::  contador de procesadores : "+ contProcesadores + "  :::.");
				}

				
				indiceInicial += tamanioBloqueNormal;
				System.out.println(" - - - - -> Indice hasta donde tomo la submatriz => " + indiceInicial);
				System.out.println(" - - - - -> Indice desde donde tomo la submatriz => " + indiceFinal);
				//System.out.println("es el ultimo indice de la matriz wachoo -> "+indiceInfMatriz);
			
			}
		}
		
		
		//-----> el Scatter divide todo lo que le mando por diferentes secciones y se lo asigna uno a uno a cada proceso
		MPI.COMM_WORLD.Scatter(arrayEnvio, 0, 1, MPI.OBJECT, arrayReceptor, 0, 1, MPI.OBJECT, 0);
		
	
		int [][] matAux = new int[tamanioBloqueNormal][n];    //---> una matriz auxiliar
		
		//--------> Verifico que si es el numero impar entonces va a haber uno que tenga una submatriz mas chiquita que los otros
		if (nroProceso == 3) {
			tamanioBloqueNormal = tamanioUltimoBloque;
			matAux = new int[tamanioBloqueNormal][n];
		}

		
		
		matAux = (int[][]) arrayReceptor[0];  //------> inicializa la matriz con el bloque que va recibir
		sumatoriaProcesoActual[0] = 0;   //------> Se inicializa la suma parcial que es donde se va a devolver el resultado total al proceso 0
		int resultado = 0;
		System.out.println(" ");
		System.out.println("          Submatriz para el proceso =>  "+ nroProceso);
		for (int i = 0; i < tamanioBloqueNormal; i++) {
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