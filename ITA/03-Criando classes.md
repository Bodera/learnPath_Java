Hoje vamos ver como implementar na prática esses conceitos aí de classes, aprender a criar e instanciar classes, colocar atributo, método, etc. 

Aqui no Java quase tudo é uma classe. Basicamente quase tudo o que você for criar Java é classe e a gente faz isso usando essa diretriz aqui `Public` e `Class` mais o nome da classe que a gente quer.

Como um sistema pode ter muitas classes, elas costumam ser organizadas em pacotes, como se fosse diretório para a gente estar colocando as classes. Fica desse jeito assim:
```java
package org.veiculos;

public class Carro {

}
```

Lembrando que toda a vez que a gente precisar usar uma classe, certo? A gente tem que importar ou a classe ou o pacote que ela está beleza?

Então por exemplo, se eu quero usar a classe Math, para poder usar as operações aí matemáticas, eu posso usar aí o Import java lang Math. Então no caso aqui, por exemplo, da minha, da classe que eu dei o exemplo, a classe Carro, eu poderia, se eu quisesse usar essa classe fora dali, eu poderia dar Import org.veículos.* ou org.veículos.carro para importar somente aquela classe. Certo? 

Bom, a informação que a classe precisa de ter, ela é armazenada ali atributos, então eu faço da seguinte forma, (você que já provavelmente tem experiência alguma outra linguagem, é como se eu estivesse declarando variáveis dentro da minha classe):
```java
package org.veiculos;

public class Carro {
    int potencia;
    float velocidade;
}
```

Então estou dizendo que a classe `Carro`, ela tem a informação potência e ela tem a informação velocidade. Olhando com mais detalhe aqui para o atributo, todo o atributo ele precisa ter tipo, então no caso aqui ele é inteiro, certo? 

E ele pode ser do tipo primitivo ou de uma classe. Pegando aqui os outros exemplos, a potência ali é inteiro, a velocidade seria ali do tipo Float, o ponto flutuante. E se a gente quiser criar `Carro`? Então para criar `Carro` a gente usa o operador __new__.

A sintaxe básica do Java para criar uma instância de classe ou um objeto é apresentada a seguir:
```java
Carro fusca = new Carro();
```
