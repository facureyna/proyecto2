package prueba;

import java.util.Random;

import mpi.MPI;

public class EjercicioMatricesComentado {

	public static void main(String[] args) {
		
		

		//----------> variables 
		long startTime = 0; //= System.currentTimeMillis();   //---> el tiempo inicial 
		int nroProceso,n,cantProcesos;
		Random rand = new Random();
		n = 6360 ;    //------> el numero de filas o columnas que tendra la matriz, sera NxN , TIENE QUE SER MULTIPLO DE 2
		int[][] datos = new int[n][n];   //---> creo la matriz datos
		
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
			System.out.println("----------------------------------------------------");
			System.out.println("                       COMIENZO                     ");
			System.out.println(" * Proceso Master     => "+ nroProceso);
			System.out.println(" * Numero de procesos => " + cantProcesos);
			System.out.println("----------------------------------------------------");   
			
			//-------> inicializo la matriz con todos los valores
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					datos[i][j] = rand.nextInt(10);
				}
			}
			
			
			//---> inicializo el contador para los procesos
			int contProcesos = 0;
			 startTime = System.currentTimeMillis();   //---> el tiempo inicial 
			
			while (indiceFinal <= n) {

				int[][] subMat = new int[tamanioBloqueNormal][n];
				int contador = 0;   //---> se usa para avanzar de fila
				for (int i = indiceInicial; i < indiceFinal; i++) { // Controla los indices para cada bloque
					for (int j = 0; j < n; j++) {
						subMat[contador][j] = datos[i][j];    //----> la columna en el for si cambia pero la fila no
					}
					contador++;
				}
				
				arrayEnvio[contProcesos] = (Object) subMat;   //---> se envia como Object la submatriz porque sino no se puede enviar!! 
				int ultimoProceso = cantProcesos - 1; //---> obtengo el nro del ultimo proceso
				contProcesos++;  //---->  Incrementa el numero del procesador

				if (contProcesos != ultimoProceso) {
					//--->  no es el ultimo se asigna normal
					indiceFinal += tamanioBloqueNormal;
				} else {
					//---> es el ultimo proceso no se le asigna normal
					indiceFinal = n;
				}

				indiceInicial += tamanioBloqueNormal;
			
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
		for (int i = 0; i < tamanioBloqueNormal; i++) {
			for (int j = 0; j < n; j++) {
				sumatoriaProcesoActual[0] = sumatoriaProcesoActual[0] + (matAux[i][j] % 10);  //----> calculo su mod
			}
		}
		
		//-----> Le devuelve los resultados en la variable sumaTotal a el Proceso 0
		MPI.COMM_WORLD.Reduce(sumatoriaProcesoActual,0, sumaTotal,0,1,MPI.INT,MPI.SUM,0);		
		
		//----> Calcula el tiempo que tardo en hacerse el MPI solo si es el proceso Master
		if (nroProceso == 0) {
			long endTime = System.currentTimeMillis();  //-----> marca de tiempo final para medir el tiempo
			double tiempoTotal = (double)(endTime - startTime)/ 1000;   //---> paso de milisegundos a segundos la resta
			
			System.out.println("-------------------------------------------------------");
			System.out.println("                 RESULTADOS FINALES                    ");
			System.out.println("----->  Suma total: " + sumaTotal[0]);  //-----> se referencia sumaTotal[0] porque lo que devuele del reduce es un vector!!!
			System.out.println("----->  Tiempo:  "+ String.valueOf(tiempoTotal) );
			System.out.println("-------------------------------------------------------");
			
		}
			
		//---> termina el MPI
		MPI.Finalize();
		
		
	}

}
