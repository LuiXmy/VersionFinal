import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;


public class GenCodFinal {

	int dirGlobal = 65432;
	File wFile;
	BufferedWriter bw;
	String operacion,op1,op2,op3; // campos del terceto
	
	int num_param_actual = 0;
    int c_etiqueta;
	
	LinkedList<String> lista_data = new LinkedList<String>();
    int lista_ini=12000;	// comienzo en memoria de la lista_data
    int count_char=lista_ini;	// Numero de characters emitidos en lista data
    int true_false = lista_ini - 200;	// En esta direccion se guarda cadena "true" y "false" y valor 1 y 0 y mas constantes
    String nemonico = new String();	
	

	public GenCodFinal(LinkedList<tupla_Tercetos> listaTercetos, TablaSimbolos tabla,	String fichero) {

		wFile = new File(fichero);
		// preparamos el fichero que contendra el codigo objeto
		try {
			bw = new BufferedWriter(new FileWriter(fichero));
		} catch (IOException e) {
			System.err.println("Error fichero de salida para Codigo Objeto ("
					+ fichero + ")");
		}

		// inicializamos el codigo objeto y lo dejamos todo preparado para leer
		// los
		// tercetos
		try {
			bw.write("ORG 0\n");
			// Inicializamos la pila al maximo puesto que es decreciente
			// y la guardamos en el puntero de pila
			bw.write("MOVE #" + dirGlobal + ", .SP\n"); // ambito global
			bw.write("MOVE .SP, .IX ; .IX Puntero de marco de pila\n");
			
			 /* creamos el RA de la clase que contiene el metodo principal, dejando
	         * hueco para todos sus atributos, despues guardamos el IX.
	         */
	        bw.write ("ADD #-" + tabla.desplazamientoTabla() + ", .SP\n"); //sumamos desp_total de la tabla de simbolos padre al SP
	        bw.write("MOVE .A, .SP\n"); //actualizamos SP
			
	      /*  bw.write("PUSH .SR\n");        
	        bw.write("PUSH .IX\n");
	      */  
			System.out.println("----------------------------");
			System.out.println("Comienza a leer los tercetos");
			Iterator<tupla_Tercetos> it = listaTercetos.iterator();
	        while (it.hasNext()) {
	        	tupla_Tercetos v = it.next();
	        	System.out.println("Terceto: " + v.GetTerceto());
	            ProcesarTerceto(v, tabla);
	        }
	        System.out.println("Todos los tercetos leidos");
	     /*   bw.write("POP .IX ; Recuperamos el marco de pila\n");
	        bw.write("POP .SR\n");
	        bw.write("MOVE .IX, .SP\n");
	     */   
	        bw.write("HALT ; Se terminara la ejecucion\n");
	        /*
	         * Almacenamos constantes, sí o sí
	         */
	        bw.write("ORG "+true_false+"\n");
	        bw.write("cad_cierto: DATA \"true\"\n");
	        bw.write("cad_falso: DATA \"false\"\n");
	        bw.write("v_cierto: DATA 1\n");
	        bw.write("v_falso: DATA 0\n");
	        bw.write("salto_lin: DATA \"\\n\"\n");

	        /*
	         * Tenemos en "lista_data" las posibles cadenas que se guardan a partir de una dir de memoria 
	         */
	        if (!lista_data.isEmpty()) {
	        	Iterator<String> iterador = lista_data.iterator();
	        	bw.write("\nORG "+lista_ini+"\n");	// A partir de aqui las cadenas
	        	while (iterador.hasNext()) {
	        		bw.write(iterador.next());
	        	}
	        } // else No hay ninguna cadena en el codigo

	        // Importante! sino no se guarda nada en el fichero!
	        bw.close();
		} catch (IOException e) {
			System.err.println("Error: Iniciando generación de codigo final");
		}
	}
	
	
	
	private void ProcesarTerceto (tupla_Tercetos tupla_actual, TablaSimbolos tabla) {	
		// Obtenemos los dos valores de la tupla
		String terceto_actual= tupla_actual.GetTerceto();	// Almacenara el String emitido por el GCI
		TablaSimbolos ambitoterceto = tupla_actual.GetAmbitoActual();

		// Separamos los operando del terceto. operador, op1, op2...
		this.separar(terceto_actual);
				
		if (operacion.equals("ASIGNACION")) {				// caso de asignar un entero a algo
	    	EjecutarAsignacion(op1, op2, ambitoterceto);	// paso el destino(op1) y el valor(op2)
		} else if (operacion.equals("ETIQUETA_SUBPROGRAMA")) {
			ComienzoSubprograma(op1, ambitoterceto); // op1: etiqueta
		}  else if (operacion.equals("ASIGNACION_CADENA")) {	// ETI: data "HOLA"
			EjecutarAsignaCad(ambitoterceto);
		} else if (operacion.equals("ASIGNA")){				// asignamos a un temp el valor de otro tmp
			EjecutarAsigna(ambitoterceto);
		} else if (operacion.equals("METE_EN_ARRAY")) {		// Asignar valor en posicion del vector
			AsignaValorVector(ambitoterceto);				// pe: v[2]=23
		} else if (operacion.equals("SACA_DE_ARRAY")){		// pe: x = v[4]
			// TODO revisar-hacer
			ObtenerValorVector(ambitoterceto);
		} else if (operacion.equals("IF")) 	{		// If
			OpCondicional(ambitoterceto);
		}
		  else if (operacion.equals("IFP")) 	{		// If Positivo
				OpCondicionalPositivo(ambitoterceto);
		} else if (operacion.equals("ETIQUETA")) {	// Etiqueta
			EtiquetaIf();
		} else if (operacion.equals("GOTO")) {		// GOTO
			OpGoto();
		} else if (operacion.equals("INIT_PARAM")) {	// SP+desplz
			InitParam();
		} else if (operacion.equals("APILAR_PARAM")) {	// Apilar Parametros
			// TODO revisar-hacer
			PushParam(ambitoterceto);
		} else if (operacion.equals("FIN_PARAM")) {		// SP-desplz
			FinParam();
		} else if (operacion.equals("CALL")) {		// Llamada a Funcion!
			LlamadaProg(ambitoterceto);
		} else if (operacion.equals("RETURN")) {	// return Valor;
			// TODO hacer-comprobrar
			ReturnOp(ambitoterceto);
		} else if (operacion.equals("RET")) {		// Retornar de una Funcion
			// TODO	no comprobada
			RetornoProg(ambitoterceto);
		} else if (operacion.equals("DIR_RETORNO")) {	// Push DirRetorno donde dev el valor Retornado	
			PushDataRetorno(ambitoterceto);
		} else if (operacion.equals("+")) {		// Suma
			nemonico = "ADD";
			OpBinaria(ambitoterceto);
		} else if (operacion.equals("-")) {		// Resta
			nemonico = "SUB";
			OpBinaria(ambitoterceto);
		} else if (operacion.equals("AND")) {		// AND lógico
			nemonico = "AND";
			OpBinaria(ambitoterceto);
		} else if (operacion.equals("MENOR")) {		// OpRel.
			// Solo hemos hecho <!
			nemonico ="CMP";
			OpRelacional(ambitoterceto);
		} else if (operacion.equals("PROMPT")) {		// CIN
			nemonico = "ININT";
			GetEntero(ambitoterceto);
		} else if (operacion.equals("PUT_BOOLEANO")) {	// PRINT Boolean COUT
			nemonico="WRSTR";
			PutBool(ambitoterceto);
		} else if (operacion.equals("PUT_CADENA")) {	// PRINT CADENA	COUT
			nemonico="WRSTR";
			PutCadena(ambitoterceto);
		} else if (operacion.equals("PUT_ENTERO")) {	// PRINT ENTERO COUT
			nemonico="WRINT";
			PutEntero(ambitoterceto);
		} else if (operacion.equals("HALT")) {	 	// STOP
			try {
				bw.write("HALT ; Se terminara la ejecucion\n");;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (operacion.equals("PUT_SALTO_LINEA")) {// PRINT SALTO_LINEA
			// No es una instruccion al uso. Solo en cada cout se emite esto
			try { bw.write("WRSTR /salto_lin\n"); }	// etiqueta ya guardada! 
			catch (IOException e) 
				{ System.err.println("Error: SaltoLinea"); }
		// } else if () {PUT_BOOLEANO
		} else {
			System.err.println("Operacion Terceto no contemplado->"+tupla_actual.GetTerceto());
		}
	}

	
	
	//***********************************************************************************************
	
	
	//******************** OPERADORES ******************************************
	
	
	/*
	 * OpCondicional
	 * Condicional IF. Si no se cumple op1 saltamos a op2
	 * Usamos las etiquetas de v_cierto y v_falso declaradas en memoria
	 */
	private void OpCondicional(TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;
			
			if (ambito_terceto.existeClave(op1)) {	// op1 local
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				bw.write("CMP #-"+Despla1+"[.IX], /v_cierto \n");
				bw.write("BNZ /"+op2+"\n");	// salto si el resultado no es cierto
			} else if (!ambito_terceto.existeClave(op1)) {	// op1 no local	
				// obtenemos el desplazamiento del simbolo en la tabla padre
				if (ambito_terceto.padre.existeClave(op1)){
					Despla1= ambito_terceto.padre.getDesplazamiento(op1);
					bw.write("CMP #-"+Despla1+"[.IY], /v_cierto \n");
					bw.write("BNZ /"+op2+"\n");	// salto si el resultado no es cierto
				} else {
					System.err.println("Error: OpCondicional. Identificador no existe.");
				}
			} else {
				System.err.println("Error: OpCondicional. Caso no contemplado.");
			}
		} catch (Exception e) {
			System.err.println("Error: Ejecutar Operacion If.");
		}
	}
	
	
	/*
	 * OpCondicional
	 * Condicional IF. Si se cumple op1 saltamos a op2
	 * Usamos las etiquetas de v_cierto y v_falso declaradas en memoria
	 */
	private void OpCondicionalPositivo(TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;
			
			if (ambito_terceto.existeClave(op1)) {	// op1 local
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				bw.write("CMP #-"+Despla1+"[.IX], /v_cierto \n");
				bw.write("BZ /"+op2+"\n");	// salto si el resultado es cierto
			} else if (!ambito_terceto.existeClave(op1)) {	// op1 no local	
				// obtenemos el desplazamiento del simbolo en la tabla padre
				if (ambito_terceto.padre.existeClave(op1)){
					Despla1= ambito_terceto.padre.getDesplazamiento(op1);
					bw.write("CMP #-"+Despla1+"[.IY], /v_cierto \n");
					bw.write("BZ /"+op2+"\n");	// salto si el resultado es cierto
				} else {
					System.err.println("Error: OpCondicional. Identificador no existe.");
				}
			} else {
				System.err.println("Error: OpCondicional. Caso no contemplado.");
			}
		} catch (Exception e) {
			System.err.println("Error: Ejecutar Operacion If.");
		}
	}
	
	
	/*
	 * OpGoto
	 * Hacemos un salto incondicional a dicha etiqueta
	 */
	private void OpGoto () {
		try {
			bw.write("br /"+op1+" ;Etiqueta Goto\n");
		} catch (Exception e) {
			System.err.println("Error: Ejecutar Insertar Etiqua Goto.");
		}
	}

	
	/*
	 * EtiquetaIf
	 * Simplemente escritibremos en el fichero el nombre de la etiqueta pasada como 1º argumento, op1
	 */
	private void EtiquetaIf () {
		try {
			bw.write(op1+": NOP;Etiqueta IFs\n");
//			bw.write("NOP\n");
		} catch (Exception e) {
			System.err.println("Error: Ejecutar Insertar Etiqua IF.");
		}
	}
	
	
	/*
	 * Operacion Binaria
	 * OpMUL(Operancion, op1, op2, resultado, ambitoterceto); a temporal!
	 * Correccion, en caso de ser una operacion normal se cumple lo de arribe,
	 * en caso de ser op= (Operacion con asignacion) el simbolo resultado puede no ser local
	 */
	private void OpBinaria (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0, Despla2=0, Despla3=0;

			// Recuerda q "nemonico" fue ya asignado en la llamada a esta funcion
			if (ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) {			// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// operando3
				Despla3 = ambito_terceto.getDesplazamiento(op3);
				bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IX]\n");
				bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
			} else if (!ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) { 	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
				bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IX]\n");
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
					bw.write("MOVE .A, #-"+Despla3+"[.IY]\n");
				}
			} else if (ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) { 				//op2 No local
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
				bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IY]\n");
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
					bw.write("MOVE .A, #-"+Despla3+"[.IY]\n");
				}
			} else if (!ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) { 	//NADA local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
				bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IY]\n");
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
					bw.write("MOVE .A, #-"+Despla3+"[.IY]\n");
				}
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar OpBinaria.");
	    }
	}
	
	
	/*
	 * OpRelacional
	 * Realizaremos la operacion < 
	 * op1 operador, op2, op1, primer operando, op2 segundo operando2
	 */
	private void OpRelacional (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0, Despla2=0, Despla3=0;
			System.out.println("Primer operador: "+ op1);
			System.out.println("Segundo operador: "+ op2);
			System.out.println("Resultado: "+ op3);
			// Nemonico en todos los casos: CMP Mirar SR el bit S Signo
			if (ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) {			// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
				}
				// Comparo el contenido de memoria de los dos operandos.
				bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IX]\n");
				// Es op1 < op2?
				bw.write("BP $3\n");	// TODO Solo si esta activo el bit P (Positivo)  => OJO!!! Tiene que saltar al MOVE .A ...
				bw.write("MOVE #1, #-" + Despla3 + "[.IX]\n"); // Verdad
				bw.write("BN $3\n");    // Saltas la siguiente instruccion
				bw.write("MOVE #0,#-" + Despla3 + "[.IX]\n"); // Falso
			} else if (!ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) { 	//op1 No local
				// Busco el operando 1.
				// Dejará en IY el marco de pila para acceder al simbolo op.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
				}
				// Comparo el contenido de memoria de los dos operandos.
				bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IX]\n");
				// Es op1 < op2?
				bw.write("BP $3\n");	// Solo si esta activo el bit P (Positivo)  => OJO!!! Tiene que saltar al MOVE .A ...
				bw.write("MOVE #1, #-" + Despla3 + "[.IX]\n"); // Verdad
				bw.write("BN $3\n");    // Saltas la siguiente instruccion /////////////OJO he cambiado el salto a 3
				bw.write("MOVE #0,#-" + Despla3 + "[.IX]\n"); // Falso
			} else if (ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) { 	//op2 No local
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
				}
				// Comparo el contenido de memoria de los dos operandos.
				bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IY]\n");
				// Es op1 < op2?
				bw.write("BP $3\n");	// Solo si esta activo el bit P (Positivo)  => OJO!!! Tiene que saltar al MOVE .A ...
				bw.write("MOVE #1, #-" + Despla3 + "[.IX]\n"); // Verdad
				bw.write("BN $3\n");    // Saltas la siguiente instruccion
				bw.write("MOVE #0,#-" + Despla3 + "[.IX]\n"); // Falso
			} else if (!ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) { 	//NADA local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);		// op3 No local
				}
				// Comparo el contenido de memoria de los dos operandos.
				bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IY]\n");
				// Es op1 < op2?
				bw.write("BP $3\n");	// Solo si esta activo el bit P (Positivo)  => OJO!!! Tiene que saltar al MOVE .A ...
				bw.write("MOVE #1, #-" + Despla3 + "[.IX]\n"); // Verdad
				bw.write("BN $3\n");    // Saltas la siguiente instruccion
				bw.write("MOVE #0,#-" + Despla3 + "[.IX]\n"); // Falso
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}		
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar OpRelacional.");
		}
	}

	
	

	//************************* ASIGNACIONES **********************************
	
	
	/*
	 * Ejecutar Asignacion es para casos donde el valor a asignar sea un ENTERO!
	 * Luego guardo a partir del IX el valor de dicho elemento
	 * Siempre es un valor a un temporal.
	 */
	private void EjecutarAsignacion(String op1, String op2, TablaSimbolos ambito_terceto)	{
		try {
			bw.write("MOVE #"+op2+",#-" + ambito_terceto.getDesplazamiento(op1) + "[.IX]\n");
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar Asignacion.");		
	    }
	}
	
	
	/* 
	 * Asignar temporal cadena
	 * 1- Anadimos a una cola de cadenas otro dato que sera guardado a partir de una direccion de mem. accesible
	 * por la etiqueta dada. pe: temporal20: DATA "HOLA"
	 * 2- Apilamos la direccion a partir de la cual empieza la cadena.
	 */
	private void EjecutarAsignaCad (TablaSimbolos ambito_terceto) {
		try {
			// 1- Anadimos a la lista de DATA esta etiqueta con su valor
			lista_data.add(op1 +": DATA "+ "\""+ op2 + "\"" + "\n");
			// Elimino las comillas que envuelven al string
			op2=op2.substring(1, op2.length()-1);
			// 2- Guardo la direccion a la cadena en el marco de pila actual
			bw.write("MOVE #"+ count_char +",#-" + ambito_terceto.getDesplazamiento(op1) + "[.IX]\n");
			// Cuento el numero de elem del string para mover el desplazamiento
		    // Texto que vamos a buscar
		    String sTextoBuscado = "\\n";	// solo ocupa un espacio pero son 2 char
		    // Contador de ocurrencias 
		    int contador = 0;	// Numero de veces que aparece la cadena
		    while (op2.indexOf(sTextoBuscado) > -1) {
		      op2 = op2.substring(op2.indexOf(sTextoBuscado)+sTextoBuscado.length(),op2.length());
		      contador++;
		    }
			// Ajustamos le desplazamiento teniendo en cuenta todo
		    if ((op2.length()==0) && (contador!=0)) {		// caso "\n"
				count_char= count_char + contador + 1;
		    } else {
				count_char= count_char + op2.length() + contador + 1;	
		    }
			// prueba impresion
			//bw.write("MOVE #-" + simbolo_op1.GetDesplazamiento() + "[.IX], .IY\n");
			//bw.write("WRSTR [.IY]\n");
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar AsignaCadena.");		
	    }
	}
	
	
	/*
	 * Asignamos el valor de op2 a op1 
	 * op1 y op2 pueden ser o no variables locales ¿Vectores?
	 * TODO la asignacion de un vector entero a otro estaría incluido aqui.
	 */
	private void EjecutarAsigna (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0, Despla2=0;
			int tamanio=1;

			if (ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) {	// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				tamanio = ambito_terceto.getTamano(op2);
				// Caso todo en LOCAL - MOVE #-op2.desp[.IX], #-op1.desp[.IX]
				CopiaBloqMem(".IX", Despla2, ".IX", Despla1, tamanio);
			} else if (!ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) {	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				tamanio = ambito_terceto.getTamano(op2);
				// Pongo el valor local en el hueco ajeno
				CopiaBloqMem(".IX", Despla2, ".IY", Despla1, tamanio);
			} else if (ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) { //op2 No local	
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando2
				// Dejará en IY el marco de pila para acceder al simbolo op.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				tamanio = ambito_terceto.padre.getTamano(op2);
				// Pongo el valor ajeno en el hueco local
				CopiaBloqMem(".IY", Despla2, ".IX", Despla1, tamanio);
			} else if (!ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op2)) {	// NADA LOCAL!
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				tamanio = ambito_terceto.padre.getTamano(op2);
				// Pongo el valor ajeno en el hueco ajeno
				CopiaBloqMem(".IY", Despla2, ".IY", Despla1, tamanio);
			} else {
				System.err.println("Ejecutar Asigna. Caso no contemplado");
			}

		} catch (Exception e) {
	        System.err.println("Error: Ejecutar Asigna.");		
	    }
	}
	
	
	
	/*
	 * AsignaValorVector
	 * Asignamos un valor a una posicion (segun el indice) del vector.
	 * uso de .R9 y .R8
	 */
	private void AsignaValorVector (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0, Despla2=0, Despla3=0;

			// Tenemos tres simbolos a dos posibilidades cada uno de estar o no en el ambito local -> 2 * 2 * 2 = 8 posibilidades
			// op1 Vector, op2 Valor, op3 indice
			if (ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2)) {	// todo local!
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
				} else {
					// Dejará en IY el marco de pila para acceder al simbolo op1.
					bw.write("MOVE #"+dirGlobal+",.IY\n");
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);	// op3 No local
					bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
				}
				// BUscamos el valor del indice y asignamos el valor a dicha posicion
				bw.write("ADD #"+ambito_terceto.getDesplazamiento(op1)+", .R9\n");	// .A=deplazamiento resp IX del elem vector
				bw.write("SUB .IX, .A\n");
				bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
				bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
			} else if (!ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op2))	{	// op1 No local
				// No cambia nada, en este caso solo el simbolo vector esta fuera
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando2
				Despla2 = ambito_terceto.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);	    // op3 No local
					bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
				}
				// BUscamos el valor del indice y asignamos el valor a dicha posicion
				bw.write("ADD #"+ambito_terceto.padre.getDesplazamiento(op1)+", .R9\n");	// .A=deplazamiento resp IX del elem vector
				bw.write("SUB .IY, .A\n");
				bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
				bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
			} else if (ambito_terceto.existeClave(op1) && (!ambito_terceto.existeClave(op2)))	{	// op2 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);	    // op3 No local
					bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
				}
				bw.write("ADD #"+ambito_terceto.getDesplazamiento(op1)+", .R9\n");	// .A=desplazamiento resp IX del elem vector
				bw.write("SUB .IX, .A\n");
				bw.write("MOVE .A, .R8\n");	// .R8 = Desplzamiento total hasta elemento del vector
				bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
			} else if (!ambito_terceto.existeClave(op1) && (!ambito_terceto.existeClave(op2)))	{	//op1 y op2 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando2
				Despla2 = ambito_terceto.padre.getDesplazamiento(op2);
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando3
				if (ambito_terceto.existeClave(op3)){							// op3 local
					Despla3 = ambito_terceto.getDesplazamiento(op3);
					bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
				} else {
					Despla3 = ambito_terceto.padre.getDesplazamiento(op3);	    // op3 No local
					bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
				}
				bw.write("ADD #"+Despla1+", .R9\n");	// desplazamiento del vector mas indice en .A
				bw.write("SUB .IY, .A\n");	
				bw.write("MOVE .A, .R8\n");	// .R8 direccion del elemento del vector
				bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
				
			} else {
				System.err.println("Ejecutar Asigna. Caso no contemplado");
			}		
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar AsignaValorVector.");
		}
	}
	
	
	
	/*
	 * ObtenerValorVector
	 * Obtenemos el valor de la posicion (op3) del vector (op1) y lo guardamos en un resultado (op2)
	 * uso .R9 
	 */
	private void ObtenerValorVector (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0, Despla3=0, Despla2=0;

			if (ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op3)) {	// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando3
				Despla3 = ambito_terceto.getDesplazamiento(op3);
				
				// Obtengo el desplzamiento total hasta el elemento ->.A
				bw.write("ADD #-"+Despla3+"[.IX], #"+Despla1+"\n");
				bw.write("SUB .IX, .A\n");	// Tengo en .A la direccion al elemento del vector
				// operando2
				if (ambito_terceto.existeClave(op2)){							// op2 local
					Despla2 = ambito_terceto.getDesplazamiento(op2);
					bw.write("MOVE [.A], #-"+Despla2+"[.IX]\n");
				} else {
					// Dejará en IY el marco de pila para acceder al simbolo op1.
					bw.write("MOVE #"+dirGlobal+",.IY\n");
					Despla2 = ambito_terceto.padre.getDesplazamiento(op2);	// op2 No local
					bw.write("MOVE [.A], #-"+Despla2+"[.IY]\n");
				}
			} else if (!ambito_terceto.existeClave(op1) && ambito_terceto.existeClave(op3)) {	//op1 no local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando3
				Despla3 = ambito_terceto.getDesplazamiento(op3);
				// Obtengo el desplzamiento total hasta el elemento ->.A
				bw.write("ADD #-"+Despla3+"[.IX], #"+Despla1+"\n");
				bw.write("SUB .IY, .A\n");	// Tengo en .A la direccion al elemento del vector(respecto de IY)
				if (ambito_terceto.existeClave(op2)){							// op2 local
					Despla2 = ambito_terceto.getDesplazamiento(op2);
					bw.write("MOVE [.A], #-"+Despla2+"[.IX]\n");
				} else {
					Despla2 = ambito_terceto.padre.getDesplazamiento(op2);	// op2 No local
					bw.write("MOVE [.A], #-"+Despla2+"[.IY]\n");
				}
			} else if (ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op3)) {	// op3 no local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// operando3
				Despla3 = ambito_terceto.padre.getDesplazamiento(op3);
				// Obtengo el desplzamiento total hasta el elemento ->.A
				bw.write("ADD #-"+Despla3+"[.IY], #"+Despla1+"\n");
				bw.write("SUB .IX, .A\n");	// Tengo en .A la direccion al elemento del vector
				if (ambito_terceto.existeClave(op2)){							// op2 local
					Despla2 = ambito_terceto.getDesplazamiento(op2);
					bw.write("MOVE [.A], #-"+Despla2+"[.IX]\n");
				} else {
					Despla2 = ambito_terceto.padre.getDesplazamiento(op2);	// op2 No local
					bw.write("MOVE [.A], #-"+Despla2+"[.IY]\n");
				}
			} else if (!ambito_terceto.existeClave(op1) && !ambito_terceto.existeClave(op3)) {	// op1 y op3 no locales
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// operando3
				Despla3 = ambito_terceto.padre.getDesplazamiento(op3);
				// Obtengo el desplzamiento total hasta el elemento ->.A
				bw.write("ADD #-"+Despla3+"[.IY], #"+Despla1+"\n");
				bw.write("SUB .IY, .A\n");	// Tengo en .A la direccion al elemento del vector
				if (ambito_terceto.existeClave(op2)){							// op2 local
					Despla2 = ambito_terceto.getDesplazamiento(op2);
					bw.write("MOVE [.A], #-"+Despla2+"[.IX]\n");
				} else {
					Despla2 = ambito_terceto.padre.getDesplazamiento(op2);	// op2 No local
					bw.write("MOVE [.A], #-"+Despla2+"[.IY]\n");
				}			
			} else {
				System.err.println("Error: ObtenerValorVector. Caso no contemplado.");
			}
		} catch (Exception e) {
			System.err.println("Error: Ejecutar ObtenerValorVector.");
		}
	}
	
	
	
	//******************* LLAMADAS Y RETORNOS DE FUNCIONES ********************************
	
	
	/*
	 *	Crear el nuevo marco de pila, añade la etiqueta al codigo ensamblador 
	 */
	private void ComienzoSubprograma (String subprograma, TablaSimbolos ambito_terceto) {
		try {
			// Recuperamos el desplazamiento para le Marco de pila
			int despl_local=ambito_terceto.desplazamientoTabla();
			// Escribimos la etiqueta
			bw.write(subprograma.toLowerCase() +":\n");		// tiene q ser en minusculas!!
			bw.write("MOVE .SP, .IX\n");					// Base del marco de pila
			bw.write("ADD #-" + despl_local + ", .SP\n");	// Techo del Marco de pila
			bw.write("MOVE .A, .SP\n");
		} catch (Exception e) {
			System.err.println("Error: Comienzo Subprograma.");
		}
	}
	
	
	/*
	 * LlamadaProg
	 * Realizamos la llamada a una funcion. Hay q tener mucho cuidado con lo que apilamos. Luego hay 
	 * q desapilar lo mismo
	 */
	private void LlamadaProg (TablaSimbolos ambito_terceto) {
		try {
			// Apilamos todo lo necesario para la vuelta
			bw.write("PUSH .SR\n");
			bw.write("PUSH .IX\n");
			// Salto a etiqueta
			bw.write("CALL /"+op1+"; SALTO A PROGRAMA\n");
			// Desapilamos lo mismo que apilamos
			bw.write("POP .IX\n");
			bw.write("POP .SR\n");
			// Hay otro valor en la pila. PushDirRetorno lo metio!
			bw.write("POP .R0; Sacando el DIR VALOR retorno\n"); // siempre se apila
		} catch (Exception e) {
			System.err.println("Error: Ejecutar Llamada a Programa.");
		}
	}

	
	/*
	 * RetornoProg
	 * Volveremos a la funcion llamante, dejando igual q cuando nos lo dieron
	 */
	private void RetornoProg (TablaSimbolos ambito_terceto) {
		try {
			// Dejamos el .SP igual que cuando nos lo dieron
			bw.write("MOVE .IX, .SP\n");
			// Ahora debe estar en la cima de la pila la direccion de retorno
			bw.write("RET\n");
		} catch (Exception e) {
			System.err.println("Error: Ejecutar RetornoPrograma.");
		}
	}

	
	
	/*
	 * InitParam
	 * Movera el SP tantas posiciones como sean necesarias para poder ir colocando los parametros-argumentos
	 * en el ambito de la funcion a la que se va a llamar. 
	 * Nota: Para saber cuantas posiciones has de desplazar el SP puedes mirar LlamadaProg y PushDIrRetorno
	 * Usamos R2
	 */
	private void InitParam () {
		try {
			// Movemos el SP tantas posiciones como sean necesarias.
			bw.write("MOVE .SP, .R2\n");
			bw.write("SUB .SP, #4\n");	//	Atento a los elem q apilas antes de CALL
			bw.write("MOVE .A, .SP\n");
			// Ahora tendran que venir PARAM para apilar
		} catch (Exception e) {
			System.err.println("Error: Ejecutar InitParam.");
		}
	}

	
	
	/*
	 * PushParam
	 * Apilamos a partir de SP, que se movio en INIT_PARAM, los argumentos que tendra la funcion llamada.
	 * Cuando la funcion llamada pase a ejecutar tendra en su marco de pila todos loa valores ya colocados
	 * NO PODEMOS TOCAR R2 !!!
	 */
	private void PushParam (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;
			int tamanio, total;

			if (ambito_terceto.existeClave(op1)) {	// op1 Local
				// obtenemos el desplazamiento de op1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				// TODO Asumimos que todos los tipos de datos son de 2 Bytes excepto vecotores, cadenas se pasan por referencia.
				// La maquina tiene direccionamiento a nivel de 2 Bytes
				tamanio = ambito_terceto.getTamano(op1);
				total = Despla1 + tamanio;
				for (int i=Despla1; i<total; i++) {
					bw.write("PUSH #-"+i+"[.IX]\n");	//apilo valores
				}
			} else {
				System.err.println("Op "+nemonico+". El parametro no esta en la tabla de sibolos local");
			}
		} catch (Exception e) {
			System.err.println("Error: Ejecutar PushParam.");
		}
	}
	
	
	/*
	 * FinParam
	 * Movera el SP tantas posiciones hacia abajo como haya movido la funcion InitParam. 
	 * Nota: Para saber cuantas posiciones has de desplazar el SP puedes mirar LlamadaProg y PushDIrRetorno. y InitParam
	 * Usamos R2: Contenia el antiguo valor de SP, metido ahi por InitParam
	 */
	private void FinParam () {
		try {
			// Movemos el SP tantas posiciones como sean necesarias.
			bw.write("MOVE .R2, .SP\n");
		} catch (Exception e) {
			System.err.println("Error: Ejecutar InitParam.");
		}	
	}
	
	
	/*
	 * PushDirRetorno
	 * Apilamos la direccion donde dejaremos el valor de retorno. Esta direccion sera la 
	 * direccion ABSOLUTA.
	 * Nota: En caso NO devolver nada, void, la funcion se apilara con una direccion IX
	 * SIEMPRE SE LLAMA A ESTA FUNCION HAYA O NO VALOR DEVUELTO
	 */
	private void PushDataRetorno (TablaSimbolos ambito_terceto) {
		try {
			// Resto a IX el desplazamiento para llegar al temporal  NOTA: Realmente guarda el valor de retorno???????
			bw.write("SUB .IX,#"+ambito_terceto.getDesplazamiento(op1)+"\n");
			// Apilo dicha direccion en la cima
			bw.write("PUSH .A; Apilando donde se guardara el retorno funcion\n");
		} catch (Exception e) {
			System.err.println("Error: Ejecutar PushDirRetorno.");
		}
	}

	
	
	/*
	 * ReturnOp
	 * Guardaremos el valor del simbolo pasado como argumento, op1, en la direccion apilada antes de llamar
	 * a la funcion. Para saber mas mirar PushDirRetorno
	 * Es decir, al acabar una funcion colocamos el valor de retorno a partir de la direccion especificada
	 * usamos R9
	 * RETURN
	 */
	private void ReturnOp (TablaSimbolos ambito_terceto)  {
		try {
			int Despla1=0;
			int tamanio=1;

			if (ambito_terceto.existeClave(op1)) {			// op1 Local
				// Movemos la direccion de Retorno a un Registro
				bw.write("MOVE [.IX], .IY; ReturnFuncion\n");	// IY tiene la dir donde se guardara el valorRetorno
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				tamanio = ambito_terceto.getTamano(op1);
				CopiaBloqMem(".IX", Despla1, ".IY", 0, tamanio);
			}  else if (!ambito_terceto.existeClave(op1)) {	// op1 no local
				// TODO NO FUNCIONA
				// Para que sirve esta rama? Siempre hay que poner la dir de retorno en el registro de activacion actual.
				// Tenemos en R9 la direccion a partir de la cual debemos dejar el valor de retorno
				// Dejará en IY el marco de pila para acceder al simbolo op.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				
				// obtenemos el desplazamiento del simbolo introducido en dicho ambito
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				bw.write("SUB .IY, #"+Despla1+"\n");
				bw.write("MOVE .A, .IY\n");
				tamanio = ambito_terceto.padre.getTamano(op1);
				// Movemos la direccion de Retorno a un Registro
				bw.write("MOVE #4[.IX], .IY; ReturnFuncion\n");	// IY tiene la dir donde se guardara el valorRetorno
			} else {
				System.err.println("Error: ReturnOp. Caso no contemplado.");
			}

		} catch (Exception e) {
			System.err.println("Error: Ejecutar ReturnOp.");
		}
	}
	
	
	//**************** ENTRADA / SALIDA *****************************
	
	
	/*
	 * GetEntero
	 * Captura por consola una ristra de caracteres que luego convertira a entero y colocara en op1
	 */
	private void GetEntero (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;
			
			if (ambito_terceto.existeClave(op1)) {			// todo local!
				System.out.println("Estas aki para recoger un valor en local: "+op1);
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				bw.write(nemonico+" #-"+Despla1 + "[.IX]\n");
			} else if (!ambito_terceto.existeClave(op1)) { 	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				Despla1= ambito_terceto.padre.getDesplazamiento(op1);
				bw.write(nemonico + " #-"+Despla1+"[.IY]\n");	
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar PutEntero.");
		}
	}
	
	
	/*
	 * PutEntero
	 * Imprime por pantalla un valor entero
	 */
	private void PutEntero (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;

			
			if (ambito_terceto.existeClave(op1)) {			// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				bw.write(nemonico + "#-"+Despla1+"[.IX]\n");
			} else if (!ambito_terceto.existeClave(op1)) { 	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				bw.write(nemonico + "#-"+Despla1+"[.IY]\n");		
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar PutEntero.");
		}
	}
	
	
	/*
	 * PutBool
	 * Imprime por pantalla la secuencia de String: true o false. Dependiendo del valor op1
	 */
	private void PutBool (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;

			if (ambito_terceto.existeClave(op1)) {			// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);	
				bw.write("CMP #-"+Despla1+"[.IX], /v_cierto\n");
				bw.write("BZ $4\n");	// Es cierto? Sí -> salto!
				bw.write(nemonico + " /cad_falso\n");	// imprime-> "false"
				bw.write("BR $2\n");
				bw.write(nemonico + " /cad_cierto\n");	// imprime-> "cierto"
			} else if (!ambito_terceto.existeClave(op1)) { 	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op1.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);
				// Imprimimos lacadena q representa a dicho valor
				bw.write("CMP #-" + Despla1 + "[.IY], /v_cierto\n");
				bw.write("BZ $4\n");	// Es cierto? Sí -> salto!
				bw.write(nemonico + " /cad_falso\n");	// imprime-> "false"
				bw.write("BR $2\n");
				bw.write(nemonico + " /cad_cierto\n");	// imprime-> "cierto"
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar PutBool.");
		}
	}
	
	
	
	/*
	 * PutCadena
	 * Imprime por pantalla el valor de una cadena que previamente se almaceno en un espacio de memoria
	 * y del q se sabe la direccion de comienzo, almacenada en el ambito (local-padre...)
	 * Se usa .R9
	 */
	private void PutCadena (TablaSimbolos ambito_terceto) {
		try {
			int Despla1=0;

			if (ambito_terceto.existeClave(op1)) {			// todo local!
				// operando1
				Despla1 = ambito_terceto.getDesplazamiento(op1);
				bw.write("MOVE #-"+Despla1+"[.IX],.R9\n");
				bw.write(nemonico + " [.R9]\n");	// imprime a partir de la etiqueta
			} else if (!ambito_terceto.existeClave(op1)) { 	//op1 No local
				// Dejará en IY el marco de pila para acceder al simbolo op.
				bw.write("MOVE #"+dirGlobal+",.IY\n");
				// operando1
				Despla1 = ambito_terceto.padre.getDesplazamiento(op1);		
				bw.write("MOVE #-"+Despla1+"[.IY],.R9\n");
				bw.write(nemonico + " [.R9]\n");	// imprime a partir de la etiqueta
			} else {
				System.err.println("Op "+nemonico+". Caso no contemplado");			
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar PutCadena.");
		}
	}
	
	
	
	// **************** FUNCIONES AUXILIARES *********************************

	
	
	/*
	 * Operacion q dado un terceto-> ASIGNACION, temp0, 10-> separa cada uno en un operando global
	 */
	private void separar(String linea)	{
	    int u= linea.indexOf(",");
	    this.operacion=linea.substring(0,u); //cogemos la operación
	    linea=linea.substring(u+1);
	    
	    u= linea.indexOf(",");
	    op1=linea.substring(0,u);	// Tenemos op1
	    linea=linea.substring(u+1);

	    // Problemas con cadenas
	    if (linea.contains("\"")) {	// Terceto con cadena de texto
	    	//System.err.println("Es algo con cadenas!"+linea);
	    	//aklinea=linea.substring(u+1);
	    	u= linea.indexOf("\",")+1;
	    	//System.err.println("Es algo con cadenas:"+linea.substring(0,u));
		    op2=linea.substring(0,u);
	    } else {
		    u= linea.indexOf(",");
		    op2=linea.substring(0,u);
	    }
	    linea=linea.substring(u+1);

	    op3=linea.substring(0,linea.indexOf("\n"));
	}
	
	
	
	/*
	 * Asigna un bloque de memoria a otro
	 * Copiamos a partir de la direccion Base con desplazamiento base en la direccion edstino con desplzamiento destino
	 * tantas posiciones como tamanio diga
	 * Si llega con R9 el desplazamiento tiene que haberse aplicado antes, es decir, sera cero!
	 */
	private void CopiaBloqMem (String dirBase, int DesplBase, String dirDest, int DesplDest, int tamanio) {
		try {
			int despl1=DesplBase, despl2=DesplDest;
			for (int i=0; i<tamanio;i++) {
				if (dirDest.equals(".R9")) {
					bw.write("MOVE #-"+despl1+"["+dirBase+"], ["+dirDest+"]; Moviendo bloque\n");
					//decremento el valor de .R9
					bw.write("SUB .R9, #1\n");
					bw.write("MOVE .A, .R9\n");
				} else if (dirBase.equals(".R9")) {
					bw.write("MOVE ["+dirBase+"], ["+dirDest+"]; Moviendo bloque\n");
					//decremento el valor de .R9
					bw.write("SUB .R9, #1\n");
					bw.write("MOVE .A, .R9\n");
				} else {
					bw.write("MOVE #-"+despl1+"["+dirBase+"], #-"+despl2+"["+dirDest+"]; Moviendo bloque\n");
				}
				despl1++;
				despl2++;
			}
			if (tamanio==0) {
				System.err.println("ERROR en CopiaBloqMem: Tamanio == 0");
			}
		} catch (Exception e) {
	        System.err.println("Error: Ejecutar CopiaBloqMem.");
		}
	}
}
