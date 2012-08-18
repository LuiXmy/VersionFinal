import java.util.Vector;

class Symbol {//Esto se guardará en el campo Value de la tabla de momento tira con enteros
	
	//Tamaño de los datos que se sumara al desplazamiento
	static final int entero=4;
	static final int car=1;
	static final int bool=4;

	//Atributos
    public String key;
    public int numerolinea; // creo que no sirve para nada
    public int tipo;     // final static int ID = 24;	final static int ENTERO = 27;	final static int CADENA = 28;
    public int desplazamiento=0;//direccion relativa del identificador
    public int numeroelementos;		// Tamaño del vector
    // sólo para funciones
    public int numArgumentos = 0; // este atributo es redundante pero ya veremos
    public Vector<Symbol> vArgumentos; // el contenido del vector hay que revisarlo, se puede buscar cualquier otra solucion para saber los argumentos que se esperan
    								  // de hecho el nombre de los argumentos creo que no sirven para nada, con que se pueda diferenciar entre los argumentos que son vectores y el resto valdría¿?
    public String tiporetorno="NULL";
    
    public Symbol(String key, int linea) {
    	super();
		this.key = key;
		this.numerolinea = linea;
    }
    
    public Symbol(String key, int linea, int tipo) {
    	super();
    	this.key = key;
    	this.numerolinea = linea;
    	this.tipo = tipo;
    }
    
    public Symbol(String key, int linea, int tipo, int tamano) {
    	super();
    	this.key = key;
    	this.numerolinea = linea;
    	this.tipo = tipo;
     }
    
    public Symbol(String key, int numerolinea, int tipo, int tamano, int numeroelementos) {
		super();
		this.key = key;
		this.numerolinea = numerolinea;
		this.tipo = tipo;
		this.numeroelementos = numeroelementos;
	}

	public Symbol(String key, int tipo, int numArgumentos, Vector<Symbol> v, int tamano, String tiporetorno) {//Constructor para funciones
    	super();
    	this.key = key;
    	this.tipo = tipo;
    	this.numArgumentos = numArgumentos;
    	vArgumentos = v;
    	this.tiporetorno=tiporetorno;
    }
	
	
	public int gDesplazamiento(){
    	return this.desplazamiento;
    }
    
    public int gTamano(){
    	if (this.tipo == 100){
    		return this.numeroelementos;
    	} else {
    		return 1; // Todas los demás tipos tienen valor 1 (2 Bytes)
    	}
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Symbol [key=" + key + ", numerolinea=" + numerolinea
				+ ", tipo=" + tipo + ", desplazamiento=" + desplazamiento
				+ ", numeroelementos=" + numeroelementos + ", numArgumentos="
				+ numArgumentos + ", vArgumentos=" + vArgumentos
				+ ", tiporetorno=" + tiporetorno + "]\n";
	}
 
}