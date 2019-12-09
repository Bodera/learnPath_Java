// TODO package _1045; //javac -d _1045 _1045.java
import java.util.Scanner; 

/**
* URI problem 1045
* The challenge is at: https://www.urionlinejudge.com.br/judge/en/problems/view/1045
* author: Bodera
*/

public class _1045
{
    public static void main(String[] args)
    {
        Scanner scan = new Scanner(System.in);
        
        double[] sides = new double[3];
        int j=1;
        double maior=0;
        
        //storing values in a array
        for(int i = 0; i < 3; i++)
        {
            double side_value = scan.nextDouble();
            sides[i] = side_value;
        }

        //sorting the array
        for(int i = 0; i < 3; i++)
        {
            for(int k = j; k < 3; k++)
            {
                if(sides[i] < sides[k])
                {
                    maior = sides[k];
                    sides[k] = sides[i];
                    sides[i] = maior;
                }
            }
            j++;
        }

        //now we can proceed working w/ variables
        double side_A = sides[0];
        double side_B = sides[1];
        double side_C = sides[2];

        Triangulo tri_1 = new Triangulo(side_A, side_B, side_C);
        tri_1.Validator();
    }
}

class Triangulo
{
    // class attributes
    private double side_A;
    private double side_B;
    private double side_C;

    // class constructor
    public Triangulo(double side_A, double side_B, double side_C)
    {
        this.side_A = side_A;
        this.side_B = side_B;
        this.side_C = side_C;
    }

    //class getters methods
    public double getSide_A() 
    {
        return side_A;
    }
    public double getSide_B() 
    {
        return side_B;
    }
    public double getSide_C() 
    {
        return side_C;
    }

    //class setters methods
    public void setSide_A(double side_A)
    {
        this.side_A = side_A;
    }
    public void setSide_B(double side_B)
    {
        this.side_B = side_B;
    }
    public void setSide_C(double side_C)
    {
        this.side_C = side_C;
    }

    // class auxiliar method.
    public double simpleSquare(double number)
    {
        double sqr = number * number;
        return sqr;
    }

    // class main method.
    public void Validator()
    {
        if(side_A >= (side_B + side_C))
        {
            System.out.println("NAO FORMA TRIANGULO");
        }
        else if(simpleSquare(side_A) == (simpleSquare(side_B) + simpleSquare(side_C)))
        {
            System.out.println("TRIANGULO RETANGULO");
        }
        else if(simpleSquare(side_A) > (simpleSquare(side_B) + simpleSquare(side_C)))
        {
            System.out.println("TRIANGULO OBTUSANGULO");
        }
        else if(simpleSquare(side_A) < (simpleSquare(side_B) + simpleSquare(side_C)))
        {
            System.out.println("TRIANGULO ACUTANGULO");
        }

        if(side_A == side_B && side_B == side_C)
        {
            System.out.println("TRIANGULO EQUILATERO");
        }
        else if(side_A == side_B || side_B == side_C || side_A == side_C)
        {
            System.out.println("TRIANGULO ISOSCELES");
        }
    }
}