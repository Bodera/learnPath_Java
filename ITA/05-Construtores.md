A aula de hoje tem por objetivo fazer com que você aprenda o conceito de construtores e como criá-los em classes Java.
Você já aprendeu a usar o construtor default do Java para criar seus objetos sem parâmetros, onde todos os atributos recebem falso, 0 ou nulo quando inicializados desta forma.
```java
//Instanciando um objeto sem passar nenhum parâmetro no processo.
Carro meuCarro = new Carro();
```
### O que é um construtor?
Construtor é um método especial usado para criarmos objetos das classes. Usando esse recurso, podemos parametrizar o objeto criado e inicializar variáveis. Calma, vamos ilustrar isso com um exemplo:
```java
public class Carro {
    int potencia;
    float velocidade;
    
    //método construtor sempre deve ter o mesmo nome da Classe.
    Carro(int potencia) { //método construtor não precisa definir o retorno.
        
        //a palavra reservada this é usada para referenciar elementos da classe (métodos e atributos).
        this.potencia = potencia; 
        velocidade = 0;
    }
}
```

Lembre-se que toda classe tem pelo menos um construtor, mesmo quando não definimos nenhum explicitamente.
Muito bem, agora que você já sabe como criar seus próprios métodos construtores, você já pode passar parâmetros na criação dos seus objetos. Nossa aplicação teria a assinatura do objeto `meuCarro` da seguinte forma:

```java
Carro novoCarro = new Carro(10); //Objeto newCarro recebe 10 no atributo int potencia.
```

Apesar de toda classe possuir um construtor, não há limitações para o número de construtores que podemos ter. É possível ter vários construtores com tipos de parâmetros diferentes (assinaturas). Vamos ao exemplo:
```java
public class Carro {
    Carro (int potencia) {...}
    Carro (String modelo) {...}
    Carro (int potencia, String modelo) {...}
    
    //Carro (int potencia, int velocidade) {...} //errado
    /*
    o que eu não posso ter é por exemplo, 2 construtores, que recebe o inteiro, por exemplo,
    com a velocidade inicial e outro que recebe o inteiro com a potência, 
    isso porque eu sempre preciso ter tipos diferentes quando defino mais de um construtor.
    */
}
```
Nossa aula terminou.
