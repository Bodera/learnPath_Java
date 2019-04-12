Nessa aula iremos aplicar os conhecimentos até então aprendidos, então vamos codar!
## Linha de comando básica em Java
```bash
javac Classe.java #compila uma classe
java Classe.class #executa uma aplicação
javadoc #gera a documentação do código
java jar #compila uma aplicação em .jar
```

## Monte seu ambiente
```bash
mkdir lab-Carros
cd lab-Carrlos
touch Carro.java
touch Principal.java
```

## Classe Carro
```java
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
```

## Classe Principal (aplicação)
```java
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
```
