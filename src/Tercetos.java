
public class Tercetos{
   
   
    public Tercetos(){
    	
    }
      
        
    public String EtiquetaSubprograma (String etiqueta){
        return "ETIQUETA_SUBPROGRAMA,"+etiqueta+",,\n";	// atentos q el main esta aqui dentro 
    }													// esta aqui dentro
    
    // iF
    public String InsertarEtiqueta(String etiqueta){

            return "ETIQUETA,"+etiqueta+",,\n";
    }
    
    // Comienzan las funciones en las que hay que llamar a la funcion "generar"

    public String operacionBinaria(String op1,String op2,String op_binaria,String resultado){
       
                return op_binaria +"," +op1 +","+ op2+ ","+resultado+"\n";
                        
    }
    
    public String operacionUnaria(String op1, String operador, String resultado){

                return operador+","+op1+","+"1"+","+resultado+"\n";
                
    }
    
    public String asignacion_valor(String op1, int op2){

    return "ASIGNACION,"+op1+","+op2+",\n";
    }
    
    public String asignacion_cadena(String op1, String op2){
    	/*
    	 * Asignamos a una etiqueta, op1, el valor de la cadena op2
    	 */
        return "ASIGNACION_CADENA,"+op1+","+op2+",\n";
    }
    
    public String asignacion(String op1, String op2){

    return "ASIGNA,"+op1+","+op2+",\n";
    }
    
    
    public String saltoIncondicional(String etiqueta){

                return "GOTO,"+etiqueta+",,\n";
    }
    
    public String saltoCondicional(String ident, String etiqueta){

                return "IF,"+ident+","+etiqueta+",\n";
    }
    
    public String saltoCondicionalFeliz(String ident, String etiqueta){

        return "IFP,"+ident+","+etiqueta+",\n";
}
    
    public String retorno(String nombre){

        return "RETURN,"+nombre+",,\n";
    }
    
    
    
    public String meteEnArray(String array,String nombre,String num){

        return "METE_EN_ARRAY,"+array+","+nombre+","+num+"\n";
    }

    public String sacaDeArray(String array,String nombre,String num){

        return "SACA_DE_ARRAY,"+array+","+nombre+","+num+"\n";
    }
    // mete en el terceto la llamada a una funcion

//    public String funcion_en_terceto (String nombre, int num_par, String parametros [], String etiqueta){
//
//        return "CALL,"+nombre + ", " + num_par+", "+parametros+", "+etiqueta+",\n";
//    }
    public String funcion_en_terceto (String nombre, int num_par){

        return "CALL,"+nombre + "," + num_par+",\n";
    }
    
    /*
     *  Avisa de que va a comenzar el apilado de parametros para llamar a una funcion
     */
    public String Init_parametros (){
        return "INIT_PARAM,,,\n";
    }
    
    // Avisa de que ya ha terminado el apilado de parametros para llamar a una funcion
    public String Fin_parametros (){
        return "FIN_PARAM,,,\n";
    }
    
    //Avisa de que tiene que apilar la direccion de retorno de una llamada a una funcion
    public String DirRetornoFuncion (String temporal)
    	{
    	return "DIR_RETORNO,"+ temporal + ",,\n";
    	}
    
    public String ApilarParam (String parametro)
    	{
    	return "APILAR_PARAM,"+parametro+",,\n";
    	}

    // Obtiene del terceto todos los parametros de la llamada a la funcion

    public String llamada_subprograma (String nombre){
                

                return "CALL,"+nombre+",,\n";
    }
    
     
    public String retorno_subprograma (){
        

        return "RET,,,\n";
    }
    
        
    public String putCadena(String op1){
     
        return "PUT_CADENA,"+op1+",,\n";
    }
    
    public String putEntero(String op1){
     
        return "PUT_ENTERO,"+op1+",,\n";
    }
    
    public String putSaltoLinea(){
     
        return "PUT_SALTO_LINEA,,,\n";
    }
    
     public String putBooleano(String op1){
     
        return "PUT_BOOLEANO,"+op1+",,\n";
    }
    
    public String readExpresion(String op1){
     
    return "PROMPT,"+op1+",,\n";}
    
    public String opRelacional(String op1, String op2, String temporal){
        return "MENOR,"+op1+","+op2+"," + temporal +"\n";    
    }
    
    public String opLogico(String op1, String op2, String temporal){
        return "AND,"+op1+","+op2+"," + temporal +"\n";    
    }
        
    public String devValor(String op1){
        return "DEVVALOR,"+op1+",,\n";
    }

    public String termina_Main () {
        return "TERMINA,,,\n";
    }
    
    public String da_valor_temp (String temporal, String valor){
        return "TEMP," + temporal + ","+ valor + ",\n";
    }
    
    public String halt (){
        return "HALT,,,\n";
    }
    
} 