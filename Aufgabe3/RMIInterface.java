package Aufgabe3;

/**
 * Author: Christoph Kozielski
 * Datei: RMIInterface.java
 * Datum: 02.02.2016
 */

import java.rmi.*;

public interface RMIInterface extends Remote {
    /**
     * Searches in the library for name and/or number. At least one must not be null
     *
     * @param name Name to search for. Empty String if none
     * @param number Number to search for. Empty String if none
     * @return the search result
     * @throws RemoteException
     */
    String[][] search(String name, String number) throws RemoteException;

    /**
     * Closes the RMI Server
     * @throws RemoteException
     */
    void close() throws RemoteException;
}
