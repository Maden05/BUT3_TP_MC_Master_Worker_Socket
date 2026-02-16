import java.io.*;
import java.net.*;

/** Master is a client. It makes requests to numWorkers. */
public class MasterSocket {
    static int maxServer = 12;
    static final int[] tab_port = {25545,25546,25547,25548,25549,25550,25551,25552,25553,25554,25555,25556};
    static String[] tab_total_workers = new String[maxServer];
    static final String ip = "127.0.0.1";
    static BufferedReader[] reader = new BufferedReader[maxServer];
    static PrintWriter[] writer = new PrintWriter[maxServer];
    static Socket[] sockets = new Socket[maxServer];

    public static void main(String[] args) throws Exception {

        // Paramètres Monte-Carlo
        int N_TOTAL = 16000000;        // Total de lancers sur tous les Workers
        int total = 0;                  // Total de points dans le quart de disque
        double pi;

        int numWorkers = maxServer;
        int totalCount;                 // Total de lancers par Worker

        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String s;

        System.out.println("#########################################");
        System.out.println("# Computation of PI by MC method        #");
        System.out.println("#########################################");

        System.out.println("\nHow many workers for computing PI (< maxServer): ");
        try {
            s = bufferRead.readLine();
            numWorkers = Integer.parseInt(s);
            System.out.println("Number of workers: " + numWorkers);
        } catch(IOException ioE){
            ioE.printStackTrace();
        }

        // Entrée des ports pour chaque Worker
        for (int i = 0; i < numWorkers; i++) {
            System.out.println("Enter worker" + i + " port: ");
            try {
                s = bufferRead.readLine();
                System.out.println("You select " + s);
            } catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        // Création des sockets pour chaque Worker
        for (int i = 0; i < numWorkers; i++) {
            sockets[i] = new Socket(ip, tab_port[i]);
            System.out.println("SOCKET = " + sockets[i]);

            reader[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            writer[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
        }

        totalCount = N_TOTAL / numWorkers;   // lancers par Worker
        String message_to_send = String.valueOf(totalCount);

        String message_repeat = "y";

        while (message_repeat.equals("y")) {

            total = 0;
            long startTime = System.nanoTime();

            // Envoyer le message à tous les Workers
            for (int i = 0; i < numWorkers; i++) {
                writer[i].println(message_to_send);
            }

            // Recevoir la réponse de chaque Worker
            for (int i = 0; i < numWorkers; i++) {
                tab_total_workers[i] = reader[i].readLine();
                System.out.println("Client sent: " + tab_total_workers[i]);
            }

            // Calcul de Pi
            for (int i = 0; i < numWorkers; i++) {
                total += Integer.parseInt(tab_total_workers[i]);
            }
            pi = 4.0 * total / totalCount / numWorkers;

            long stopTime = System.nanoTime();

            System.out.println("\nPi: " + pi);
            System.out.println("Error: " + (Math.abs((pi - Math.PI)) / Math.PI));
            System.out.println("Ntot: " + (totalCount * numWorkers));
            System.out.println("Available processors: " + numWorkers);
            System.out.println("Time Duration (ns): " + (stopTime - startTime) + "\n");

            // Écriture dans le CSV
            try (FileWriter fw = new FileWriter("scalabiliteForteSocket.csv", true)) {
                fw.write((totalCount * numWorkers) + "," + numWorkers + "," + (stopTime - startTime) + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("\nRepeat computation (y/N): ");
            try {
                message_repeat = bufferRead.readLine();
                System.out.println(message_repeat);
            } catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        // Fermeture des sockets
        for (int i = 0; i < numWorkers; i++) {
            System.out.println("END");
            writer[i].println("END");
            reader[i].close();
            writer[i].close();
            sockets[i].close();
        }
    }
}
