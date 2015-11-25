// Datei: Aufgabe2.java
// Autor: Christoph Kozielski
// Datum: 24. November 2015, 17:30 Uhr

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

class Aufgabe2 {

    private static ServerSocket ss = null;
    private static Socket cs = null;
    private static InputStream is = null;
    private static InputStreamReader isr = null;
    private static BufferedReader br = null;
    private static OutputStream os = null;
    private static PrintWriter pw = null;
    private static String zeile = null;
    private static String host = null;
    private static int port = 0;

    private static String name;
    private static String number;

    private static String[] names;
    private static String[] numbers;

    private static String[] resultNames;
    private static String[] resultNumbers;

    /**
     * 
     * @param args Should get exactly one number (between 60 and 80)
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 1) {

            initData();

            // Serverstart
            host = InetAddress.getLocalHost().getHostName();
            port = 9800 + Integer.parseInt(args[0]);
            System.out.println("Server startet auf " + host + " an Port " + port);

            // ServerSocket einrichten und auf Requests warten.
            ss = new ServerSocket(port);

            while (true) {
                System.out.println("Warte im accept()");
                cs = ss.accept();

                // Request lesen
                is = cs.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                zeile = br.readLine();
                System.out.println("\nKontrollausgabe (Request): " + zeile);

                // Favicon-Requests ignorieren
                if (zeile.startsWith("GET /favicon")) {
                    System.out.println("Favicon-Request");
                    br.close();
                    continue;
                }

                // Den Request bearbeiten
                if (zeile.equals("GET / HTTP/1.1")) {
                    returnForm();
                } else {
                    if (zeile.contains("D=Server+beenden")) {
                        System.out.println("Schließe Server...");
                        break;
                    }
                    searchPhoneBook();
                }
            }
        } else {
            throw new Exception("As argument enter one number between 60 and 80 (recommended: your local hostname number)!");
        }
    } // end of main

    /**
     * Liefert die Startseite zurück
     */
    private static void returnForm() throws Exception {
        System.out.println("Gebe Seite aus");
        os = cs.getOutputStream();
        pw = new PrintWriter(os);

        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: text/html");
        pw.println();

        pw.println("<html>");
        pw.println("<body>");
        pw.println("<h2 align=center>Telefonverzeichnis</h2>");
        pw.println(toASCII("<h3>Sie können nach Name oder nach Telefonnummer oder nach beiden (nebenläufig) suchen.</h3>"));
        pw.println("<form method=get action=\"http://" + host + ":" + port + "\">");
        pw.println("<table>");
        pw.println("<tr> <td valign=top>Name:  </td> <td><input name=A></td> <td></td> </tr>");
        pw.println("<tr> <td valign=top>Nummer:</td> <td><input name=B></td> <td></td> </tr>");
        pw.println("<tr> <td valign=top><input type=submit name=C value=Suchen></td>");
        pw.println("<td><input type=reset></td>");
        pw.println("<td><input type=submit name=D value=\"Server beenden\"></td> </tr>");
        pw.println("</table>");
        pw.println("</form>");
        pw.println("</body>");
        pw.println("</html>");

        pw.println();
        pw.flush();
        pw.close();
        br.close();
    }

    /**
     * Durchsucht das Telefonbuch und macht die Ausgabe (Konsole und Browser)
     */
    private static void searchPhoneBook() throws Exception {
        StringTokenizer st = new StringTokenizer(zeile);
        st.nextToken();
        String req = st.nextToken();
        st = new StringTokenizer(req, "&");
        StringTokenizer st1 = new StringTokenizer(st.nextToken(), "=");
        st1.nextToken();
        StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
        st2.nextToken();

        if (st1.hasMoreTokens()) {
            name = toUTF_8(st1.nextToken().replace("+", " ").trim());
        } else {
            name = "";
        }
        if (st2.hasMoreTokens()) {
            number = toUTF_8(st2.nextToken().replace("+", " ").trim());
        } else {
            number = "";
        }

        String result = "";

        System.out.println("Kontrollausgabe (Name): " + name);
        System.out.println("Kontrollausgabe (Nummer): " + number + "\n");

        if (name.length() > 0 && number.length() > 0) {
            System.out.println("Starte Suche nach Name und Nummer...");
            startBothSearch();
        } else if (name.length() > 0) {
            System.out.println("Starte Suche nach Name...");
            startNameSearch();
        } else if (number.length() > 0) {
            System.out.println("Starte Suche nach Nummer...");
            startNumberSearch();
        } else {
            System.out.println("Fehler: Name und Nummer dürfen nicht beide leer sein!");
            result += "<p> Fehler:  Name und Nummer dürfen nicht beide leer sein!</p>";
        }

        os = cs.getOutputStream();
        pw = new PrintWriter(os);

        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: text/html");
        pw.println();

        System.out.println("Prepariere Ergenisse...");
        if (name.length() > 0) {
            if (resultNames[0] == null) {
                result += "<p>Die Suche nach dem Namen \"" + name + "\" war erfolglos.</p>";
            } else {
                result += "<table border=1><tr><th colspan=2>Suche nach Name</tr><tr><th>Name<th>Nummer</tr>";
                for (int i = 0; i < resultNames.length; i += 2) {
                    if (resultNames[i] != null) {
                        result += "<tr><td>" + resultNames[i] + "<td>" + resultNames[i + 1] + "</tr>";
                    }
                }
                result += "</table><br>";
            }
        }
        if (number.length() > 0) {
            if (resultNumbers[0] == null) {
                result += "<p>Die Suche nach der Nummer \"" + number + "\" war erfolglos.</p>";
            } else {
                result += "<table border=1><tr><th colspan=2>Suche nach Nummer</tr><tr><th>Name<th>Nummer</tr>";
                for (int j = 0; j < resultNumbers.length; j += 2) {
                    if (resultNumbers[j] != null) {
                        result += "<tr><td>" + resultNumbers[j] + "<td>" + resultNumbers[j + 1] + "</tr>";
                    }
                }
                result += "</table>";
            }
        }

        System.out.println("");
        resultNames = new String[10];
        resultNumbers = new String[10];

        pw.println(toASCII(result));
        pw.println(toASCII("<br>"
                + "<form action\"http://" + host + ":" + port + "\">"
                + "<button>Zurück</button>"
                + "</form>"));

        System.out.println("Sende Antwort...");
        pw.println();
        pw.flush();
        pw.close();
        br.close();
        System.out.println("Fertig...");
    }

    /**
     * Startet die Suche nach einem Namen
     */
    private static void startNameSearch() {
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
     * Start die Suche nach einer Nummer
     */
    private static void startNumberSearch() {
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
     * Wandelt ASCII in UTF-8
     */
    private static String toUTF_8(String string) {
        return string.replace("%C4", "Ä")
                .replace("%D6", "Ö")
                .replace("%DC", "Ü")
                .replace("%E4", "ä")
                .replace("%F6", "ö")
                .replace("%FC", "ü")
                .replace("%DF", "ß");
    }

    /**
     * UTF-8 in ASCII
     */
    private static String toASCII(String string) {
        return string.replace("Ä", "&Auml")
                .replace("Ö", "&Ouml")
                .replace("Ü", "&Uuml")
                .replace("ä", "&auml")
                .replace("ö", "&ouml")
                .replace("ü", "&uuml")
                .replace("ß", "&szlig");
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

        resultNames = new String[10];
        resultNumbers = new String[10];
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
                    resultNames[k] = names[l];
                    k++;
                    resultNames[k] = numbers[l];
                    k++;
                }
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
                    resultNumbers[i] = names[j];
                    i++;
                    resultNumbers[i] = numbers[j];
                    i++;
                }
            }
        }
    }
} // end of class

