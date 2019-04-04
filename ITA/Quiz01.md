1. É um método especial de uma classe Java que tem o mesmo nome da classe, cuja execução se dá imediatamente após a instanciação de um objeto da classe, com o objetivo de alocar memória e iniciar as suas variáveis de instância. Como é chamado esse método especial?
```
Construtor
```

### Explanation
Em Java, podemos definir um método que é executado inicialmente e automaticamente. Este método é chamado de construtor.

O construtor tem as seguintes características:

* Tem o mesmo nome da classe.
* É o primeiro método que é executado.
* Ele é executado automaticamente.
* Não pode retornar dados.
* Ele é executado apenas uma vez.
* Um construtor tem como objetivo inicializar atributos.

Quando um objeto é construído, é necessário inicializar suas variáveis ​​com valores coerentes. A solução em linguagens orientadas a objetos é usar os construtores. 

Leia mais em [Brainly.com.br](https://brainly.com.br/tarefa/14509658#readmore)

2. Uma classe que tem um método abstrato não precisa ser declarada como abstrata. Verdadeiro/V ou Falso/F?
```
Falso
```

### Explanation 
Classes declaradas abstratas não podem ser instanciadas diretamente, só pelas classes que a herdarem.
Métodos abstratos são métodos que ficam na classe mãe só podem ser acessados se forem implementados nas classes que herdaram a classe mãe. 
Portanto uma classe não pode ter um método abstrato sem ser declarada abstrata.

Leia mais em [Brainly.com.br](https://brainly.com.br/tarefa/14509734#readmore)

3. Uma classe abstrata pode não ter nenhum método abstrato. Verdadeiro/V ou Falso/F?
```
Verdadeiro
```

### Explanation
Uma classe abstrata não tem obrigação de ter métodos além do construtor. Ela pode ter ou não métodos abstratos.

4. O código abaixo compilará corretamente. Verdadeiro/V ou Falso/F?
```java
public abstract class X {...}
public class Teste{
   public static void main (String[] args) {
      X x = new X();
   }
}
```
```
Falso
```

### Explanation
Abstract classes cannot be instantiated, but they can be subclassed.

Read more at [Java documentation](https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html)

5. O código abaixo compilará corretamente. Verdadeiro/V ou Falso/F?
```java
class Teste{
   public void metodo (int i) {...}   
   protected void metodo (double x) {...}
}
```
```
Verdadeiro
```

### Explanation


6. O código abaixo compilará corretamente. Verdadeiro/V ou Falso/F?
```java
class Teste{
   public int metodo ( ) {...}   
   protected double metodo ( ) {...}
}
```
```
Falso
```

### Explanation
Num caso de overloading ou sobrecarga legítimo, um método pode ter o mesmo nome de outro, desde que os seus argumentos sejam diferentes, quanto à quantidade de argumentos ou quanto aos tipos dos argumentos se a quantidade for a mesma.
Neste último caso, a ordem dos argumentos também importa: (int, double) é diferente de (double, int).
Formalmente, dizemos que os métodos do exemplo têm o mesmo nome e mesma assinatura, o que é inaceitável em Java; seria aceitável se tivessem assinaturas diferentes quanto ao nome do método ou tipos e ordem de parâmetros!

7. É um mecanismo existente no paradigma orientado a objetos que permite a reutilização da estrutura e do comportamento de uma classe ao se definir novas classes; é conhecido também como relacionamento "é um"; a classe que herda o comportamento é chamada de subclasse e a que definiu o comportamento, superclasse. Qual é o nome desse mecanismo?
```
Herança
```

### Explanation
“É-UM”, “herda de” e “é um subtipo de” são todas expressões equivalentes.

8. Apresente como é em Java a assinatura do método correspondente à primeira mensagem que aparece no trecho do exemplo abaixo 
```java
Ponto ponto1 = new Ponto( );
Ponto ponto3 = new Ponto( );
if (ponto1.igual(ponto3))
      ponto3.mover(5,10);
```
```
Essa aqui eu não entendi.
```

### Explanation

9. Os dados e operações de um objeto que são visíveis externamente compreendem seu/sua ____________.
a. Interface
b. Sobreposição
c. Sobrecarga
d. Método
```
Interface
```

### Explanation


10. O princípio de ______________________ permite que objetos que pertencem a diferentes classes respondam de forma distinta a mensagens idênticas. 
```
Polimorfismo
``` 

### Explanation
O polimorfismo se caracteriza quando, para mensagens distintas, objetos diferentes responderem ou agirem de forma idêntica.

11. A principal diretriz convencionada para atributos de objetos é que todas as variáveis de instância devem ser declaradas____________.
a. Privadas
b. Públicas
c. Protegidas
```
Privadas
```

### Explanation
Assim o acesso a estes atributos são restritos às outras classes.
