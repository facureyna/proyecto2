package prueba;

import java.util.Random;

public class EjercicioMatricesNoParalelo {

	
	public static void main(String[] args) {
		
		
		long startTime = System.currentTimeMillis();  //---> tomo el timpo desde donde arranca
		Random rand = new Random();
		int n = 567 ;    //------> el numero de filas o columnas que tendra la matriz, sera NxN
		int[][] datos = new int[n][n];   //---> creo la matriz datos
		int sumaTotal = 0;
		
		//System.out.println("-----------------------------------------------------");
		//System.out.println("             GENERO MATRIZ CON DATOS                 ");
		for (int i = 0; i < n; i++) {
			//System.out.print("    fila "+ i + "  =>   ");
			for (int j = 0; j < n; j++) {
				datos[i][j] = rand.nextInt(10);
				//System.out.print("["+datos[i][j]+"] ");
			}
			//System.out.println();
		}
		
		
		for (int i = 0; i < datos.length; i++) {
			for (int j = 0; j < datos.length; j++) {
				sumaTotal =  sumaTotal + (datos[i][j] % 10);
			}
		}
		
		
		long endTime = System.currentTimeMillis();  //-----> marca de tiempo final para medir el tiempo
		double tiempoTotal = (double)(endTime - startTime)/ 1000;   //---> paso de milisegundos a segundos la resta
		
		System.out.println("-------------------------------------------------------");
		System.out.println("                 RESULTADOS FINALES                    ");
		System.out.println("----->  Suma total: " + sumaTotal); 
		System.out.println("----->  Tiempo:  "+ String.valueOf(tiempoTotal) );
		System.out.println("-------------------------------------------------------");
		
	}
	
	
}
