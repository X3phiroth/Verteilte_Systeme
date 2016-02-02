package Aufgabe3;
/**
 * Author: Christoph Kozielski
 * Datei: RMIServer.java
 * Datum: 02.02.2016
 */

import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class RMIServer extends UnicastRemoteObject implements RMIInterface {

    private static String host;

    private static String name;
    private static String number;

    private static String names[];
    private static String numbers[];

    private static String results[][];

    public RMIServer() throws RemoteException {}

    public static void main(String[] args) throws Exception {
        initData();
        host = InetAddress.getLocalHost().getHostName();
        LocateRegistry.createRegistry(1099);     // Port 1099
        RMIServer zeit = new RMIServer();

        Naming.rebind("rmi://" + host + "/SearchService", zeit);
        System.out.println("Server wartet auf RMIs");
    }

    @Override
    public String[][] search(String name, String number) throws RemoteException {
        RMIServer.name = name;
        RMIServer.number = number;
        System.out.println("Got:: Name: " + name + "; Number: " + number);
        results = new String[2][10];
        if (name.length() > 0 && number.length() > 0) {
            System.out.println("Starting search for name and number...");
            startBothSearch();
        } else if (name.length() > 0) {
            System.out.println("starting search for name...");
            startNameSearch();
        } else if (number.length() > 0) {
            System.out.println("starting search for number...");
            startNumberSearch();
        } else {
            System.out.println("Error: Name and number, one must not be empty!");
            results = null;
        }

        return results;
    }

    @Override
    public void close() throws RemoteException {
        System.out.println("Shutting down RMI Server");
        System.exit(1);
    }

    /**
     * Starts to search for name
     */
    private void startNameSearch() {
        if (!name.isEmpty()) {
            try {
                NameThread nameSearch = new NameThread();
                nameSearch.start();
                nameSearch.join();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        } else {
            System.out.println("input a valid name (no empty string)!");
        }
    }

    /**
     * Starts to search for number
     */
    private void startNumberSearch() {
        if (!number.isEmpty()) {
            try {
                NumberThread numberSearch = new NumberThread();
                numberSearch.start();
                numberSearch.join();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        } else {
            System.out.println("input a valid number (no empty string)!");
        }
    }

    /**
     * Starts to search for both, name and number
     */
    private void startBothSearch() {
        if (!name.isEmpty() && !number.isEmpty()) {
            try {
                NameThread nameSearch = new NameThread();
                NumberThread numberSearch = new NumberThread();

                nameSearch.start();
                numberSearch.start();

                nameSearch.join();
                numberSearch.join();
            } catch (InterruptedException e) {
                e.getMessage();
            }
        } else {
            System.out.println("Name AND number must not be empty!");
        }
    }

    /**
     * Initializes the phone book
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

        results = new String[2][10];
    }

    /**
     * Thread class for name search
     */
    static class NameThread extends Thread {

        @Override
        public void run() {
            int k = 0;
            for (int l = 0; l < names.length; l++) {
                if (names[l].equals(name)) {
                    results[0][k++] = names[l];
                    results[0][k++] = numbers[l];
                }
            }
        }
    }

    /**
     *Thread class for number search
     */
    static class NumberThread extends Thread {

        @Override
        public void run() {
            int i = 0;
            for (int j = 0; j < numbers.length; j++) {
                if (numbers[j].equals(number)) {
                    results[1][i++] = names[j];
                    results[1][i++] = numbers[j];
                }
            }
        }
    }
}