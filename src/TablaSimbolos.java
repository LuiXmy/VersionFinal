import java.util.HashMap;
/**
 * @author Luis Miguel
 *
 */

public class TablaSimbolos{
	
	HashMap<String, Symbol> tabla;
	TablaSimbolos padre;
	private int desplazamiento;  // Tamaño de la tabla
		
	public TablaSimbolos() {
		tabla=new HashMap<String, Symbol>();
		padre = null;
	}
	
	public TablaSimbolos(TablaSimbolos padre) {
		tabla = new HashMap<String, Symbol>();
		this.padre = padre;
	}
	
	void insertarTS(String nombre, Symbol simbolo){
		if (existeClave(nombre))
		{
			error(nombre);
		}else{
			tabla.put(nombre, simbolo);
			actualizarTamano(simbolo);
		}
	}
	
	
	boolean buscar(String clave){
		if (tabla.containsKey(clave))
			return true;	
		else
			return false;
	}
	
	int tipo(String clave){
		Symbol res;
		if (tabla.containsKey(clave)){
			res=tabla.get(clave);
			return res.tipo;
		}else{
			return -1;
		}
	}
	
	void cambiarTipo(String clave, int tipo){
		Symbol simbolo;
		simbolo=tabla.get(clave);
		simbolo.tipo=tipo;
		tabla.put(clave, simbolo);
	}
	
	boolean existeClave(String nombre){
		return tabla.containsKey(nombre);
	}
	
	void imprimir(){
		System.out.println("Elementos de la Tabla: "+tabla.toString());
	}
	
	Symbol obtenerSimbolo(String nombre){
		Symbol res;
		res=tabla.get(nombre);
		return res;
	}
	void error(String nombre){//¿Para la ejecucion?
		throw new Error("Error. El identificador"+nombre +" ya existe");
	}
	
	
	//Tamano de la tabla
	public int desplazamientoTabla(){
		return this.desplazamiento;
	}
	
	public int getDesplazamiento(String simbolo){
		return tabla.get(simbolo).gDesplazamiento();
	}
	
	//Necesito actualizar el tamaño cada vez que inserto algo
	public void actualizarTamano(Symbol simbolo){
		if (simbolo.tipo == 100){
			simbolo.desplazamiento = desplazamiento;
			desplazamiento += simbolo.numeroelementos; 
		} else {
			simbolo.desplazamiento = desplazamiento;
			desplazamiento += 1; 
		}
		
	}
	
	
	public int getTamano(String clave){
		return tabla.get(clave).gTamano();
	}
	////////////////////////////PRUEBA////////////////////////////////////////////////////////////////////////
	/*public static void main(String[]args){
		TablaSimbolos tablapadre = new TablaSimbolos();
		TablaSimbolos activa = tablapadre;
		
		activa = tablapadre;
		Symbol simbolo=new Symbol("hola",3);
		tablapadre.insertarTS("hola", simbolo);
		tablapadre.insertarTS("hola23", simbolo);
		tablapadre.imprimir();
		
		TablaSimbolos tabla = new TablaSimbolos(activa); 
		//Esto es para ver si funciona bien el paso del puntero de la tabla
		activa = tabla;
		activa.insertarTS("padre", simbolo);
		activa.insertarTS("padre2", simbolo);
		activa.imprimir();
		System.out.println(tabla.padre.buscar(3));
		
		TablaSimbolos tablanieta = new TablaSimbolos(activa);
		activa = tablanieta;
		activa.insertarTS("nieta23", simbolo);
		activa.insertarTS("nieta", simbolo);
		activa.imprimir();
		System.out.println(activa.padre.padre.buscar(5));
	}*/
}
