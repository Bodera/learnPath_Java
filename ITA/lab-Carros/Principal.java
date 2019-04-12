public class Principal {
    
    public static void main(String[] args) {
            
            Carro carro1 = new Carro(); //método construtor que inicializa novos objetos.
            carro1.modelo = "Kadett GSi";
            carro1.potencia = 121;
            //carro1.velocidade = 0; //valores do tipo inteiro já são inicializados com 0 por default.
            
            carro1.acelerar();
            carro1.acelerar();
            carro1.acelerar();
            carro1.frear();
            
            carro1.imprimir();
            
            //Objetos são independentes, as alterações no atributo de um não irão afetar o estado do outro!
            Carro carro2 = new Carro();
            carro2.modelo = "Vectra";
            carro2.potencia = 138;
            //carro2.velocidade = 0; //valores do tipo inteiro já são inicializados com 0 por default.
            
            carro2.imprimir();
            
            /*
             //Objetos são independentes, as alterações no atributo de um não irão afetar o estado do outro!
            Carro carro3 = new Carro();
            carro3.modelo = "Vectra";
            carro3.potencia = 138;
            //carro3.velocidade = 0; //valores do tipo inteiro já são inicializados com 0 por default.
            
            carro3.imprimir(); 
            //Não importa o valor dos atributos. Os objetos carro2 e carro3 são coisas instâncias totalmente diferentes de uma mesma classe.
            */
            
    }
    
}
