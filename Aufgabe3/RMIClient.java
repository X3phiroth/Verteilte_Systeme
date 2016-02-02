package Aufgabe3;

/**
 * Author: Christoph Kozielski
 * Datei: RMIClient.java
 * Datum: 02.02.2016
 */

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.StringTokenizer;

public class RMIClient {

    private static RMIInterface server = null;
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

    public RMIClient() {}

    public static void main(String[] args) throws Exception {

        if (args.length == 1 && Integer.parseInt(args[0]) >= 60 && Integer.parseInt(args[0]) <= 80) {
            host = InetAddress.getLocalHost().getHostName();
            port = 9800 + Integer.parseInt(args[0]);

            System.out.println("Connect to RMI server @ " + host);
            server = (RMIInterface) Naming.lookup("rmi://" + host + "/SearchService");
            System.out.println("Establishing port " + port);
            ss = new ServerSocket(port);

            while (true) {
                System.out.println("Waiting @ port " + port);
                cs = ss.accept();

                // read Request
                is = cs.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                zeile = br.readLine();
                System.out.println("\nControl output (Request): " + zeile);

                // ignore favicon requests
                if (zeile.startsWith("GET /favicon")) {
                    System.out.println("Favicon-Request");
                    br.close();
                    continue;
                }

                //  work with  request
                if (zeile.equals("GET / HTTP/1.1")) {
                    returnForm();
                } else {
                    if (zeile.contains("D=Server+beenden")) {
                        try {
                            server.close();
                        } catch (Exception ignored) {}
                        System.out.println("Shutting webserver down...");
                        returnBye();
                        break;
                    }
                    cutRequestAndSend();
                }
            }
        } else {
            throw new Exception("As argument enter one number between 60 and 80 (recommended: your local hostname number)!");
        }
    }

    /**
     * trimming request, sending to server
     * and saving the answer
     */
    private static void cutRequestAndSend() throws Exception {
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

        System.out.println("Control output (Name): " + name);
        System.out.println("Control output (Number): " + number + "\n");

        String[][] answer = new String[2][10];

        String result = "";

        os = cs.getOutputStream();
        pw = new PrintWriter(os);

        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: text/html");
        pw.println();

        if (name.length() > 0 || number.length() > 0) {
            System.out.println("Sending to RMI-Server...");
            try {
                answer = server.search(name, number);
            } catch (RemoteException ignored) {}

            System.out.println("prepare results...");
            if (name.length() > 0) {
                if (answer[0][0] == null) {
                    result += "<p>Die Suche nach dem Namen \"" + name + "\" war erfolglos.</p>";
                } else {
                    result += "<table border=1><tr><th colspan=2>Suche nach Name</tr><tr><th>Name<th>Nummer</tr>";
                    for (int i = 0; i < answer[0].length; i += 2) {
                        if (answer[0][i] != null) {
                            result += "<tr><td>" + answer[0][i] + "<td>" + answer[0][i + 1] + "</tr>";
                        }
                    }
                    result += "</table><br>";
                }
            }
            if (number.length() > 0) {
                if (answer[1][0] == null) {
                    result += "<p>Die Suche nach der Nummer \"" + number + "\" war erfolglos.</p>";
                } else {
                    result += "<table border=1><tr><th colspan=2>Suche nach Nummer</tr><tr><th>Name<th>Nummer</tr>";
                    for (int j = 0; j < answer[1].length; j += 2) {
                        if (answer[1][j] != null) {
                            result += "<tr><td>" + answer[1][j] + "<td>" + answer[1][j + 1] + "</tr>";
                        }
                    }
                    result += "</table>";
                }
            }
        } else {
            result += "<p>Es dürfen nicht beide Felder (Name & Nummer) leer sein oder nur Leerzeichen enthalten!</p>";
        }

        System.out.println("");
        pw.println(toASCII(result));
        pw.println(toASCII("<br>"
                + "<form action\"http://" + host + ":" + port + "\">"
                + "<button>Zurück</button>"
                + "</form>"));

        System.out.println("Sending answer to browser...");
        pw.println();
        pw.flush();
        pw.close();
        br.close();
        System.out.println("Done...");
    }

    /**
     * Liefert die Startseite zurück
     */
    private static void returnForm() throws Exception {
        System.out.println("Returning home page");
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
     * Sendet Seite zum Abschied
     */
    private static void returnBye() throws Exception {
        System.out.println("Returning goodbye page");
        os = cs.getOutputStream();
        pw = new PrintWriter(os);

        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: text/html");
        pw.println();

        pw.println("<html>");
        pw.println("<body>");
        pw.println("<h1 align=center>Auf Wiedersehen!</h2>");
        pw.println("</body>");
        pw.println("</html>");

        pw.println();
        pw.flush();
        pw.close();
        br.close();
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
     * Wandelt UTF-8 in ASCII
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
}