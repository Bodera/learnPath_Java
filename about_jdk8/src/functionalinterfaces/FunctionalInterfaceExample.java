package functionalinterfaces;
public class FunctionalInterfaceExample {
    public static void main(String[] args) {
        MyBeautifulInterface myInterface = () -> System.out.println("Rebooting...");
        myInterface.reboot();
        myInterface.sayHello();
        MyBeautifulInterface.sayGoodbye();
        myInterface.saySalut();
        MyBeautifulInterface.sayAurevoir();
    }
}

@FunctionalInterface
interface MyBeautifulInterface {

    void reboot();

    // we can't have more than one method
    //void powerOff();

    // However, we can have many default and static methods we need
    default void sayHello() {
        System.out.println("Hello!");
    }

    default void saySalut() {
        System.out.println("Salut!");
    }

    static void sayGoodbye() {
        System.out.println("Goodbye!");
    }

    static void sayAurevoir() {
        System.out.println("Aurevoir!");
    }
}