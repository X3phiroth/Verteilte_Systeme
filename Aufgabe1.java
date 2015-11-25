
/**
 * Dateiname:   Aufgabe1.java
 * Author:      Christoph Kozielski
 * Datum:       Dienstag, 19.10.2015
 * Zweck:       Nebenläufige Suche nach Name und Nummer in einem Verzeichnis/Telefonbuch
 */
public class Aufgabe1 {

    static String[] names;
    static String[] numbers;

    static String[] resultNames;
    static String[] resultNumbers;

    static String name;
    static String number;

    static String suche;

    /**
     * Aufgabe1 für den angedeuteten Abfrage-Server-Dienst
     * @param args
     */
    public static void main(String[] args) {
        initData();

        //Startet die Endloßchleife
        loop:
        while (true) {
            System.out.println("Wonach soll gesucht werden? (name, nummer, beides - exit für Verlassen): ");
            suche = System.console().readLine();

            switch (suche) {
                case "name":
                    startNameSearch();
                    System.out.println("\nErgebnisse: ");
                    break;
                case "nummer":
                    startNumberSearch();
                    System.out.println("\nErgebnisse: ");
                    break;
                case "beides":
                    startBothSearch();
                    if (resultNames[0] != null || resultNumbers[0] != null) {
                        System.out.println("\nErgebnisse: ");
                    }
                    break;
                case "exit":
                    System.out.println("Ciao!");
                    break loop;
                default:
                    System.out.println("!!! Geben Sie bitte auschließlich eines !!!\n!!! der geforderten Signalwörter ein    !!!");
                    continue;
            }

            for (String thisName : resultNames) {
                if (thisName != null) {
                    System.out.println(thisName);
                }
            }
            for (String thisNumber : resultNumbers) {
                if (thisNumber != null) {
                    System.out.println(thisNumber);
                }
            }
            System.out.println("");
            resultNames = new String[5];
            resultNumbers = new String[5];
        }
    }

    /**
     * Initialisiert das "Telefonbuch"
     */
    private static void initData() {
        names = new String[5];
        numbers = new String[5];

        names[0] = "Schmidt";
        numbers[0] = "016548";

        names[1] = "Müller";
        numbers[1] = "2566841";

        names[2] = "Van Beethoven";
        numbers[2] = "11880";

        names[3] = "Schmidt";
        numbers[3] = "3844216";

        names[4] = "Pooth";
        numbers[4] = "11880";

        resultNames = new String[5];
        resultNumbers = new String[5];
    }

    /**
     * Startet die Suche nach einem Namen
     */
    private static void startNameSearch() {
        System.out.println("Eingabe des Namens, nach dem gesucht werden soll: ");
        name = System.console().readLine();
        name = name.replace("\t", " ").trim();
        if (!name.isEmpty()) {
            try {
                NameThread nameSearch = new NameThread();
                nameSearch.start();
                nameSearch.join();
            } catch (InterruptedException e) {
                e.toString();
            }
        } else {
            System.out.println("Geben Sie einen validen Namen ein (keine leeren Zeichen- oder TAB-Folgen)!");
        }
    }

    /**
     * Aufgabe1 die Suche nach einer Nummer
     */
    private static void startNumberSearch() {
        System.out.println("Eingabe der Nummer, nach der gesucht werden soll: ");
        number = System.console().readLine();
        number = number.replace("\t", " ").trim();
        if (!number.isEmpty()) {
            try {
                NumberThread numberSearch = new NumberThread();
                numberSearch.start();
                numberSearch.join();
            } catch (InterruptedException e) {
                e.toString();
            }
        } else {
            System.out.println("Geben Sie eine valide Nummer ein (keine leeren Zeichen- oder TAB-Folgen)!");
        }
    }

    /**
     * Startet die Suchen nach Name und Nummer
     */
    private static void startBothSearch() {
        System.out.println("Eingabe des Namens, nach dem gesucht werden soll: ");
        name = System.console().readLine();
        name = name.replace("\t", " ").trim();
        System.out.println("Eingabe der Nummer, nach der gesucht werden soll: ");
        number = System.console().readLine();
        number = number.replace("\t", " ").trim();
        if (!name.isEmpty() && !number.isEmpty()) {
            try {
                NameThread nameSearch = new NameThread();
                NumberThread numberSearch = new NumberThread();

                nameSearch.start();
                numberSearch.start();

                nameSearch.join();
                numberSearch.join();
            } catch (InterruptedException e) {
                e.toString();
            }
        } else {
            System.out.println("Lassen Sie keine der Suchen leer!");
        }
    }

    /**
     * Klasse für den Thread, der die Namen durchsucht / vergleicht
     */
    static class NameThread extends Thread {

        @Override
        public void run() {
            int k = 0;
            for (int l = 0; l < names.length; l++) {
                if (names[l].equals(name)) {
                    resultNames[k] = names[l] + " - " + numbers[l];
                    k++;
                }
            }
            if (resultNames[0] == null) {
                resultNames[0] = "Die Suche nach \"" + name + "\" war erfolglos.";
            }
        }
    }

    /**
     * Klasse für den Thread, der die Nummern durchsucht / vergleicht
     */
    static class NumberThread extends Thread {

        @Override
        public void run() {
            int i = 0;
            for (int j = 0; j < numbers.length; j++) {
                if (numbers[j].equals(number)) {
                    resultNumbers[i] = names[j] + " - " + numbers[j];
                    i++;
                }
            }
            if (resultNumbers[0] == null) {
                resultNumbers[0] = "Die Suche nach \"" + number + "\" war erfolglos.";
            }
        }
    }
}
