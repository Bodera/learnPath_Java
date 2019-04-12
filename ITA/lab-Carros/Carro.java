public class Carro {
    
    //declarando os atributos da classe
    int potencia;
    int velocidade;
    String modelo;
    
    //declarando os métodos da classe
    void acelerar() {
        velocidade += potencia; 
    }
    
    void frear() {
        velocidade /= 2;
    }
    
    //
    int getVelocidade() {
        return velocidade;
    }
    
    void imprimir() {
        System.out.println("O carro "+modelo+" está a velocidade de "+getVelocidade()+" Km/h.");
    }
}
