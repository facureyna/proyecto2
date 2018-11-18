package prueba;


import mpi.MPI;

public class MPIEnvio {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int me,size;
		args = MPI.Init(args);
		me = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();
		
		int data[]=new int[2];
		String texto = "";
		if(me==0)
		{
			texto = "hola proceso dos";
			data[0]=100;
			data[1]=200;
			MPI.COMM_WORLD.Send(data,0,2,MPI.INT,1,10);
			System.out.println("Proceso "+me+" envía el número "+data[0]+ " y " + data[1] + " al Proceso 1");
			for(int i=2;i<10;i++)
			{
				MPI.COMM_WORLD.Send(texto.toCharArray(), 0, texto.length(), MPI.CHAR, i, 10);
				System.out.println("Proceso "+me+" envía el texto " + texto + " al Proceso 2");
			}
		}
		else if(me==1)
		{
		MPI.COMM_WORLD.Recv(data,0,2,MPI.INT,0,10);
		System.out.println("Proceso "+me+" recibe número "+data[0]+ " y " + data[1] + " del Proceso 0");
		}
		else
		{
			char[] recibido=new char[16];
			MPI.COMM_WORLD.Recv(recibido,0,16,MPI.CHAR,0,10);
			System.out.println("Proceso "+me+" recibe texto "+ new String(recibido) + " del Proceso 0");
		}
		
		MPI.Finalize();
	}

}
