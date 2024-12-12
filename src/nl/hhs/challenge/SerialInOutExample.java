/*  Dit voorbeeld: Gert den Neijsel 20231130 (origineel 20201216) (in puur sequentiële Arduino stijl)
    Eenvoudig verzenden en ontvangen van data via seriële poort.
    Zie bron: https://github.com/RishiGupta12/SerialPundit voor meer gevanceerde voorbeelden
    Geen threads - geen timers - geen events (alleen voor de gui).
    Dat betekent dat het programma stilstaat als je een stukje code toevoegd waar het programma op moet wachten.
    Is te testen in combinatie met Arduino sketch "Microbit_SerieelInvoerUitvoer.ino"

    Voer na installatie de volgende commando's uit in MySQL server/workbench:
       CREATE DATABASE vb1;
       CREATE TABLE vb1.tbl1(tijdstip TEXT, temperatuur FLOAT);
       CREATE USER microbit IDENTIFIED BY 'geheim';
       GRANT INSERT, UPDATE, SELECT, DELETE ON vb1.* TO 'microbit';
    Nadat dit programma data ingevoerd heeft in de database, dan kun je dit opvragen maken met dit commando:
       SELECT * FROM vb1.tbl1;
 */

package nl.hhs.challenge;

import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import java.sql.*;
import java.awt.*; // alleen voor het geluidje
import java.text.SimpleDateFormat; // omzetten voor datum en tijd naar iets leesbaar
import java.util.Date; // voor bepalen datum en tijd
import javax.swing.*; // voor de GUI
//import java.time.Clock; // voor de millis()

public class SerialInOutExample extends JFrame {
    private JPanel mainPanel;
    private JTextField inkomend;
    private JTextField uitgaand;
    private JButton verstuurButton;
    private String teVerzenden;

    public static void main(String[] args) {
        test();
        JFrame frame = new SerialInOutExample("SerialInOutExample");
        frame.setVisible(true);
    }

    public static void test () {


        Connection connection = null;
        try {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/vb1",
                    "root", "0020Ferron0500");

            // mydb is database
            // mydbuser is name of database
            // mydbuser is password of database

            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                    "select * from tbl1");
            Float code;
            String title;
            while (resultSet.next()) {
                code = resultSet.getFloat("temperatuur");
//                title = resultSet.getString("tijdstip").trim();
                System.out.println("Code : " + code);
//                        + " Title : " + title);
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (Exception exception) {
            System.out.println(exception);
        }
    }
    public SerialInOutExample(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        verstuurButton.addActionListener(e -> {
            teVerzenden = uitgaand.getText();
            uitgaand.setText("");
        });
        // Stukje code om elke 2 seconden iets via seriële poort te verzenden (om te testen ofzo)
//        Clock clock = Clock.systemDefaultZone();
//        long millis = 0;
//        long vorigeMillis = 0;
//        int interval = 2000; // interval van verzenden

        // Begin van het "hoofdprogramma"
        InsertIntoSQL database = new InsertIntoSQL();   //Deze regel uitcommenten als SQL nog niet werkt.
        String port = "";

        try {
            SerialComManager scm = new SerialComManager();

            // Blok hieronder: automatisch de poort met de Microbit kiezen (werkt alleen voor Microbit).
            String[] poorten = scm.listAvailableComPorts();
            for (String poort : poorten) {
                port = poort;
                System.out.println("Poort " + port + " gekozen..."); // beschikbare poorten afdrukken
            }
            if (port.isEmpty()) {
                System.out.print("Geen Microbit gevonden!");
                System.exit(1); // Programma afbreken
            }

            // COM poort kun je ook hard invullen, zoek via Arduino of Device Manager uit welke COM poort je gebruikt:
            // long handle = scm.openComPort("COM3", true, true, true);

            long handle = scm.openComPort("COM8", true, true, true);
            scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 9600);
            scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
            scm.writeString(handle, "test", 0);
            this.setVisible(true); // de gui

            while (true) { // gewoon altijd doorgaan, vergelijkbaar met de Arduino loop()
                this.mainPanel.updateUI();

                // tijdstip = nu, dit moment.
                String tijdstip = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                tijdstip = tijdstip.replaceAll("[\\n\\r]", ""); // tijdstip om af te drukken, handig...

                // Stukje code om elke 2 seconden iets via seriële poort te verzenden (om te testen ofzo)
//                millis = clock.millis(); // tijdafhandeling op dezelfde manier als op Arduino/Microbit
//                if (millis > vorigeMillis + interval) {
//                    String dataVerzenden = "\n";
//                    scm.writeString(handle, dataVerzenden, 0);
////                    System.out.println(tijdstip + " Data verzonden: " + dataVerzenden);
//                    vorigeMillis = milli s;
//                }

                // Data verzenden via serieel
                if (teVerzenden != null) {
//                    scm.writeString(handle, teVerzenden, 0);
                    System.out.println(tijdstip + " Data verzonden: " + teVerzenden);
                    teVerzenden = null;
                }

                // Data ontvangen via serieel
                String dataOntvangen = scm.readString(handle);
                if (dataOntvangen != null) { // Er is data ontvangen
                    // verwijder alle newlines '\n' en carriage_returns '\r':
                    dataOntvangen = dataOntvangen.replaceAll("\\R.*", "");
                    System.out.println(tijdstip + " Ontvangen data: " + dataOntvangen);

                    this.inkomend.setText("Ontvangen op " + tijdstip + ": " + dataOntvangen);

                    // String naar float omzetten
                    Float temperatuur = Float.parseFloat(dataOntvangen);

                    // afronden op 1 cijfer achter de komma
//                    temperatuur = (float) (Math.round(temperatuur * 10.0) / 10.0);

                    //System.out.println("Float: " + temperatuur); // Kun je mee testen of er correct verstuurd wordt.
//                    database.insert(tijdstip, temperatuur);  //Deze regel uitcommenten als SQL nog niet werkt.

//                    if (dataOntvangen.contains("1")) { // Piepje als er een 1 gelezen wordt vanaf de seriële poort
//                        System.out.println("\"1\" ontvangen, dus: Windows default beep");
//                        Toolkit.getDefaultToolkit().beep(); // Piep


                    String url = "jdbc:mysql://localhost:3306/vb1";
                    String user = "root";
                    String password = "0020Ferron0500";
                    try {
                        Connection conn = DriverManager.getConnection(url, user, password);
                        Statement mystmt = conn.createStatement();
                        mystmt.executeUpdate("insert into tbl1" + "(temperatuur)" + "values(" + dataOntvangen + ")");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
//                    }
                }
            }


        } catch (Exception e) { // Stukje foutafhandeling, wordt als het goed is nooit gebruikt
            System.out.print("\033[1;93m\033[41m"); // Dikke gele tekst in rode achtergrond (ANSI colors Java)
            System.out.print("Ai, er zit een fout in je programma. Kijk eerst naar de onderste rode foutmeldingen en werk omhoog:");
            System.out.println("\033[0m"); // Tekstkleuren weer resetten naar standaard.
            e.printStackTrace(); // Dit drukt de foutmeldingen af.
            System.exit(2); // Programma afbreken
        }
    }
}

