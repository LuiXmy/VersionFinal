
public class Expresion {
	String _str = "";
	public String etqVerdad = "";
	public String etqFalso = "";
	public int _tipo = 0; // tipo 1 = operacion aritmetica
						 // tipo 2 = operacion relacional/logica
	public int _linea;
	public Token t;
	
	public Expresion(String str, int tipo){
		_str = str;
		_tipo = tipo;
	}
	public Expresion(String str, int tipo, int linea){
		_str = str;
		_tipo = tipo;
		_linea=linea;
	}
	public Expresion(String etqVerdad, String etqFalso){
		this.etqVerdad = etqVerdad;
		this.etqFalso = etqFalso;
		_tipo = 2;
	}

	@Override
	public String toString() {
		String str = ("Str: "+ _str + "; etqVerdad: "+ etqVerdad + "; etqFalso: "+ this.etqFalso + "; tipo: "+ _tipo);
		return str;
	}
	

}
