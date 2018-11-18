package prueba;

import javax.xml.crypto.Data;

import mpi.MPI;

public class Ejercicio2 {

	
	//----> ATENCION !!!!
	//----> Se tiene que probar al menos con 2 procesadores
	
	public static void main(String[] args) {
		
		
		int me,size;
		args = MPI.Init(args);      //---> lo inicializa
		me = MPI.COMM_WORLD.Rank();   //--->  este es el id del proceso actual
		size = MPI.COMM_WORLD.Size();   //---> la cantidad de procesos totales
				
		
		String texto = "";   //---> inicializo la variable texto
		Boolean marca = false;
		
		
		if(me==0)
		{
			texto = "hola proceso!";
			
			for(int i=1;i<10;i++)
			{
				MPI.COMM_WORLD.Send(texto.toCharArray(), 0, texto.length(), MPI.CHAR, i, 10);
				System.out.println("Proceso "+me+" envía el texto: '" + texto + "' al Proceso 2");
			}
			
			
		}
		else if(me != 0)
		{
			char[] recibido= new char[13];
			MPI.COMM_WORLD.Recv(recibido,0,13,MPI.CHAR,0,10);
			System.out.println("Proceso "+me+" recibe texto: '"+ new String(recibido) + "' del Proceso 0");
		}
			
				
		MPI.Finalize();

	}

}
