package client;

import client.Client;

import java.util.Scanner;

public class Repl {
    final private Client client;
    public Repl(String url){
        client = new Client(url);
    }

    public void run() {
        System.out.println("Welcome to chess. Type 'help' to start.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
