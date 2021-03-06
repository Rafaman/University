package poo.polinomio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.*;

public class Applicazione {
    public static void main(String[] args) {
        /*
        * Scelta del metodo con cui si vuole procedere
        * */
        System.out.println("Come vuoi procedere?\nC. Riga di comando\nG. Grafico\nQ. Quit");
        Scanner sc = new Scanner(System.in);
        while(true) {
            String scelta = sc.nextLine().toUpperCase();
            try {
                switch (scelta) {
                    case "C":
                        commandLine();
                        break;
                    case "G":
                        GUI.creaGUI();
                        break;
                    case "Q":
                        System.exit(0);
                    default:
                        throw new IllegalArgumentException();
                }
                break;
            }catch(IllegalArgumentException iae){
                System.out.println("Opzione errata!");
            }
        }
        sc.close();
    }
    private static void commandLine(){
        String espressione2 = "", espressione1;
        Polinomio polinomio, polinomio2;
        int tipo;

        Scanner sc = new Scanner(System.in);
        /*
        * Scelta tipo di polinomio
        * */
        while(true){
            System.out.println("Tipo polinomio:\n1. PolinomioLL\n2. PolinomioSet\n3. PolinomioList\n4. PolinomioMap");
            try {
                tipo = sc.nextInt();
                if(tipo != 1 && tipo != 2 && tipo != 3 && tipo != 4) throw new IllegalArgumentException();
                break;
            }catch(Exception e){
                System.out.println("Scelta errata!");
            }
        }
        sc.nextLine();
        /*
        * Inserimento polinomi da tastiera e controllo validità
        * */
        while(true){
            try {
                System.out.println("Inserisci due polinomi (Q per terminare): ");
                espressione1 = sc.nextLine();
                if(espressione1.equals("Q") || espressione1.equals("q")) break;
                valutaEspressione(espressione1);
                espressione2 = sc.nextLine();
                if(espressione2.equals("Q") || espressione2.equals("q")) break;
                valutaEspressione(espressione2);
                break;
            } catch(IllegalArgumentException iae) {
                System.out.println("Polinomio errato!");
            }
        }
        sc.close();
        /*
         * Riconoscimento dei polinomi
         * */
        polinomio = riconosciPolinomio(espressione1, tipo);
        polinomio2 = riconosciPolinomio(espressione2, tipo);
        /*
         * Stampa operazioni polinomi
         * */
        System.out.println("Polinomi: " + polinomio + ", " + polinomio2);
        System.out.println("Addizione: " + polinomio.add(polinomio2));
        System.out.println("Moltiplicazione: " + polinomio.mul(polinomio2));
        System.out.println("Derivate: " + polinomio.derivata() + ", " + polinomio2.derivata());
    }
    public static Polinomio riconosciPolinomio(String s, int t){
        Polinomio polinomio;
        int coefficiente = 0, grado = 0;
        boolean segno = false, esponente = false;
        /*
        * Controllo scelta tipo polinomio dell' utente
        * */
        if(t == 1)
            polinomio = new PolinomioLL();
        else if(t == 2)
            polinomio = new PolinomioSet();
        else if(t == 3)
            polinomio = new PolinomioList();
        else polinomio = new PolinomioMap();
        /*
        * Inizio del riconoscimento dei monomi
        * */
        StringTokenizer st = new StringTokenizer(s, "+-x^", true);
        while(st.hasMoreTokens()){
            String corrente = st.nextToken();
            if (corrente.equals("-")) segno = true;
            else if (corrente.equals("^")) esponente = true;
            if (Character.isDigit(corrente.charAt(0)))
                if (segno) {
                    coefficiente = Integer.parseInt("-" + corrente);
                    segno = false;
                } else if (esponente) {
                    grado = Integer.parseInt(corrente);
                    polinomio.add(new Monomio(coefficiente, grado));
                    coefficiente = 0; grado = 0;
                    esponente = false;
                } else coefficiente = Integer.parseInt(corrente);
            else if (corrente.equals("x")){
                grado = 1;
                if(segno && coefficiente == 0) coefficiente = -1;
                else if(coefficiente == 0) coefficiente = 1;
            }
            else if (!esponente){
                polinomio.add(new Monomio(coefficiente, grado));
                coefficiente = 0; grado = 0;
            }
        }
        polinomio.add(new Monomio(coefficiente, grado));
        return polinomio;
    }
    public static void valutaEspressione(String s){
        /*
        * Match tra regex e la stringa inserita dall' utente
        * */
        if(s.equals("")) throw new IllegalArgumentException();
        else if(s.contains("++") || s.contains("--")) throw new IllegalArgumentException();
        String polinomioControllo = "[\\-?[[0-9]+]s[[x]^[0-9]]*]*";
        if(!Pattern.matches(polinomioControllo, s)) throw new IllegalArgumentException();
    }
}

class GUI{
    public static void creaGUI(){
        /*
        * Creazione GUI
        * */
        EventQueue.invokeLater(() -> {
            JFrame frame = new Finestra();
            frame.setVisible(true);
        });
    }
    static class Finestra extends JFrame{
        private ArrayList<Polinomio> polinomi = new ArrayList<>(), derivate = new ArrayList<>();
        private ArrayList<JCheckBox> checkPolinomi = new ArrayList<>(), ris = new ArrayList<>();
        private JPanel panelPolinomi, panelRisultati;
        private JCheckBox addizione, moltiplicazione, derivata;
        private JMenuItem salva, salvaConNome, apri, esci, aggiungiPolinomio, modificaPolinomio, rimuoviPolinomio, polinomioLL, polinomioSet, polinomioList, polinomioMap, help, about;
        private JButton start;
        private File fileSalvataggio = null;
        private int tipoPolinomio;

        private Finestra() {
            int WIDTH = 500, HEIGHT = 350;
            /* Imposto l'aspetto della finestra secondo quello del sistema operativo */
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored){}
            setTitle("Polinomio"); /* Titolo finestra */
            setSize(WIDTH, HEIGHT); /* Dimensioni finestra */
            /*
            * Location al centro
            * */
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dimension.width - WIDTH) / 2;
            int y = (dimension.height - HEIGHT) / 2;
            setLocation(x, y);
            /*
            * Creazione Listener
            * */
            ListenerFileMenu listenerFileMenu = new ListenerFileMenu();
            ListenerCommandMenu listenerCommandMenu = new ListenerCommandMenu();
            ListenerTipologieMenu listenerTipologieMenu = new ListenerTipologieMenu();
            ListenerHelpMenu listenerHelpMenu = new ListenerHelpMenu();
            ListenerStartButton listenerStartButton = new ListenerStartButton();
            /*
            * Inserimento componenti aggiuntivi:
            * Menù "File" e relative voci
            * */
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar( menuBar );
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            apri = new JMenuItem("Apri");
            apri.addActionListener(listenerFileMenu);
            fileMenu.add(apri);
            salva = new JMenuItem("Salva");
            salva.addActionListener(listenerFileMenu);
            fileMenu.add(salva);
            salvaConNome = new JMenuItem("Salva con nome");
            salvaConNome.addActionListener(listenerFileMenu);
            fileMenu.add(salvaConNome);
            esci = new JMenuItem("Esci");
            esci.addActionListener(listenerFileMenu);
            fileMenu.add(esci);
            /*
            * Menù "Comandi" e relative voci
            * */
            JMenu commandMenu = new JMenu("Comandi");
            menuBar.add(commandMenu);
            JMenu tipologie = new JMenu("Tipo polinomio");
            commandMenu.add(tipologie);
            polinomioLL = new JMenuItem("PolinomioLL");
            polinomioLL.addActionListener(listenerTipologieMenu);
            polinomioSet = new JMenuItem("PolinomioSet");
            polinomioSet.addActionListener(listenerTipologieMenu);
            polinomioList = new JMenuItem("PolinomioList");
            polinomioList.addActionListener(listenerTipologieMenu);
            polinomioMap = new JMenuItem("PolinomioMap");
            polinomioMap.addActionListener(listenerTipologieMenu);
            tipologie.add(polinomioLL);
            tipologie.add(polinomioSet);
            tipologie.add(polinomioList);
            tipologie.add(polinomioMap);
            commandMenu.addSeparator();
            aggiungiPolinomio = new JMenuItem("Aggiungi polinomio");
            aggiungiPolinomio.addActionListener(listenerCommandMenu);
            commandMenu.add(aggiungiPolinomio);
            modificaPolinomio = new JMenuItem("Modifica polinomio");
            modificaPolinomio.addActionListener(listenerCommandMenu);
            commandMenu.add(modificaPolinomio);
            rimuoviPolinomio = new JMenuItem("Rimuovi polinomio");
            rimuoviPolinomio.addActionListener(listenerCommandMenu);
            commandMenu.add(rimuoviPolinomio);
            /*
            * Menù "Help" e relative voci
            * */
            JMenu helpMenu = new JMenu("Help");
            menuBar.add(helpMenu);
            help = new JMenuItem("Help");
            help.addActionListener(listenerHelpMenu);
            about = new JMenuItem("About");
            about.addActionListener(listenerHelpMenu);
            helpMenu.add(help);
            helpMenu.add(about);
            /*
            * Pannelli e componenti aggiuntivi:
            * Pannello generale
            * */
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(2, 2));
            panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            /*
            * Pannello che contiene i polinomi
            * */
            panelPolinomi = new JPanel();
            panelPolinomi.setLayout(new FlowLayout(FlowLayout.LEFT));
            /*
            * Pannello delle operazioni
            * */
            JPanel panelOperazioni = new JPanel();
            panelOperazioni.setLayout(new GridLayout(4, 1));
            panelOperazioni.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            /*
            * Pannello risultati
            * */
            panelRisultati = new JPanel();
            panelRisultati.setLayout(new FlowLayout(FlowLayout.LEFT));
            /*
            * Creazione etichette
            * */
            JLabel label = new JLabel("Polinomi", JLabel.LEFT);
            label.setFont(new Font("", Font.PLAIN, 14));
            JLabel label2 = new JLabel("Operazioni");
            label2.setFont(new Font("", Font.PLAIN, 14));
            JLabel label3 = new JLabel("Risultati");
            label3.setFont(new Font("", Font.PLAIN, 14));
            /*
            * Aggiunta etichette ai rispettivi pannelli
            * */
            panelPolinomi.add(label);
            panelOperazioni.add(label2);
            panelRisultati.add(label3);
            /*
            * Creazione voci operazioni permesse e aggiunta al loro pannello
            * */
            addizione = new JCheckBox("Addizione");
            moltiplicazione = new JCheckBox("Moltiplicazione");
            derivata = new JCheckBox("Derivata");
            panelOperazioni.add(addizione);
            panelOperazioni.add(moltiplicazione);
            panelOperazioni.add(derivata);
            /*
            * Creazione pannello-bottone avviamento programma
            * */
            JPanel panelStart = new JPanel();
            panelStart.setLayout(new FlowLayout(FlowLayout.RIGHT));
            panelStart.setBorder(BorderFactory.createEmptyBorder(105, 0, 5, 5));
            panelStart.setSize(WIDTH, 20);
            start = new JButton("Start");
            start.addActionListener(listenerStartButton);
            panelStart.add(start, BorderLayout.EAST);
            /*
            * Aggiunta al pannello generale di tutti i componenti
            * */
            panel.add(panelPolinomi);
            panel.add(panelOperazioni);
            panel.add(panelRisultati);
            panel.add(panelStart, BorderLayout.WEST);
            add(panel, BorderLayout.NORTH);
            /*
             * Metodo start() blocca tutte le azioni non consentite se non si seleziona il tipo di polinomio
             * */
            start();
            /*
            * Chiusura
            * */
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if(consensoUscita()) System.exit(0);
                }
            });
        }
        private class ListenerFileMenu implements ActionListener{
            /*
            * Listener per menù "File" della GUI, nel quale sono presenti "Apri, Salva, Salva con nome, esci"
            * */
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                * Utente seleziona esci
                * */
                if(e.getSource() == esci){
                    if(consensoUscita()) System.exit(0);
                }
                /*
                 * Utente seleziona salva
                 * */
                else if(e.getSource() == salva){
                    JFileChooser fileChooser = new JFileChooser();
                    try{
                        if( fileSalvataggio != null ){
                            int ans = JOptionPane.showConfirmDialog(null,"Sovrascrivere " + fileSalvataggio.getAbsolutePath() + " ?");
                            if( ans == 0)
                                save( fileSalvataggio.getAbsolutePath() );
                            else
                                JOptionPane.showMessageDialog(null,"Nessun salvataggio!");
                            return;
                        }
                        if( fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION )
                            fileSalvataggio = fileChooser.getSelectedFile();
                        if( fileSalvataggio != null ){
                            save( fileSalvataggio.getAbsolutePath() );
                            setTitle(getTitle() + " ~ " + fileSalvataggio.getAbsolutePath());
                        }
                        else
                            JOptionPane.showMessageDialog(null,"Nessun salvataggio!");
                    }catch( Exception exc ){
                        exc.printStackTrace();
                    }
                }
                /*
                 * Utente seleziona salva con nome
                 * */
                else if(e.getSource() == salvaConNome){
                    JFileChooser chooser=new JFileChooser();
                    try{
                        if( chooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION )
                            fileSalvataggio = chooser.getSelectedFile();
                        if( fileSalvataggio != null ){
                            save( fileSalvataggio.getAbsolutePath() );
                            setTitle(getTitle() + " ~ " + fileSalvataggio.getAbsolutePath());
                        }
                        else
                            JOptionPane.showMessageDialog(null,"Nessun salvataggio!");
                    }catch( Exception exc ){
                        exc.printStackTrace();
                    }
                }
                /*
                 * Utente seleziona apri
                 * */
                else if(e.getSource() == apri){
                    JFileChooser chooser = new JFileChooser();
                    try {
                        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                            if(!chooser.getSelectedFile().exists())
                                JOptionPane.showMessageDialog(null, "File inesistente!", "Error", JOptionPane.ERROR_MESSAGE);
                            else{
                                fileSalvataggio = chooser.getSelectedFile();
                                try{
                                    load(fileSalvataggio.getAbsolutePath());
                                    setTitle(getTitle() + " ~ " + fileSalvataggio.getAbsolutePath());
                                } catch (IOException ioe){
                                    JOptionPane.showMessageDialog(null, "Impossibile aprire. File malformato!", "Errore", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        else
                            JOptionPane.showMessageDialog(null,"Nessuna apertura!");
                    } catch(Exception exc){
                        exc.printStackTrace();
                    }
                }
            }
        }
        private class ListenerCommandMenu implements ActionListener {
            /*
             * Listener menù "Comandi" della GUI, nel quale troviamo il menù "Tipologie" e i comandi "Aggiungi polinomio, Modifica polinomio, Rimuovi polinomio"
             * */
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                 * Utente seleziona aggiungi polinomio
                 * */
                if(e.getSource() == aggiungiPolinomio){
                    String polinomio = JOptionPane.showInputDialog(null, "Inserisci polinomio:", "Aggiungi polinomio", JOptionPane.PLAIN_MESSAGE);
                    try {
                        Applicazione.valutaEspressione(polinomio);
                        polinomi.add(Applicazione.riconosciPolinomio(polinomio, tipoPolinomio));
                        checkPolinomi.add(new JCheckBox(polinomio));
                        panelPolinomi.add(checkPolinomi.get(checkPolinomi.size() - 1));
                    } catch(IllegalArgumentException iae) {
                        JOptionPane.showMessageDialog(null, "Polinomio errato!", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                    Finestra.this.validate();
                }
                /*
                 * Utente seleziona modifica polinomio
                 * */
                else if(e.getSource() == modificaPolinomio) {
                    String newPolinomio = null;
                    if(countSelected(checkPolinomi) > 1) {
                        JOptionPane.showMessageDialog(null, "Troppi polinomi selezionati!", "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    for(JCheckBox cb:checkPolinomi)
                        if(cb.isSelected()) {
                            try{
                                newPolinomio = JOptionPane.showInputDialog(null, "Polinomio modificato:", "Modifica polinomio", JOptionPane.PLAIN_MESSAGE);
                                Applicazione.valutaEspressione(newPolinomio);
                                Polinomio polinomioCorrente = Applicazione.riconosciPolinomio(newPolinomio, tipoPolinomio);
                                int index = polinomi.indexOf(polinomi.get(checkPolinomi.indexOf(cb)));
                                polinomi.add(index, polinomioCorrente);
                            } catch (IllegalArgumentException iae){
                                JOptionPane.showMessageDialog(null, "Polinomio errato!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            cb.setText(newPolinomio);
                            cb.setSelected(false);
                        }
                }
                /*
                 * Utente seleziona rimuovi polinomio
                 * */
                else if(e.getSource() == rimuoviPolinomio){
                    if(countSelected(checkPolinomi) == 0) {
                        JOptionPane.showMessageDialog(null, "Nessun polinomio selezionato!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Iterator<JCheckBox> iterator = checkPolinomi.iterator();
                    while (iterator.hasNext()) {
                        JCheckBox corrente = iterator.next();
                        if (corrente.isSelected()) {
                            polinomi.remove(checkPolinomi.indexOf(corrente));
                            panelPolinomi.remove(corrente);
                            iterator.remove();
                        }
                    }
                    Finestra.this.repaint();
                    Finestra.this.validate();
                    JOptionPane.showMessageDialog(null, "Polinomi rimossi corretatmente!", "Completato", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        private class ListenerTipologieMenu implements ActionListener{
            /*
             * Listener per il sottomenù "Tipologie" presente in "Comandi" per la scelta del tipo di polinomio
             * */
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == polinomioLL) {
                    tipoPolinomio = 1;
                    setTitle("PolinomioLL");
                }
                else if(e.getSource() == polinomioSet) {
                    tipoPolinomio = 2;
                    setTitle("PolinomioSet");
                }
                else if(e.getSource() == polinomioList) {
                    tipoPolinomio = 3;
                    setTitle("PolinomioList");
                }
                else if(e.getSource() == polinomioMap) {
                    tipoPolinomio = 4;
                    setTitle("PolinomioMap");
                }
                postScelta();
            }
        }
        private class ListenerHelpMenu implements ActionListener{
            /*
            * Listener menù "Help"
            * */
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == help)
                    JOptionPane.showMessageDialog(null, "Per iniziare:\n1. Recarsi nel menu' \"Comandi\" e scegliere la tipologia di polinomio da utilizzare\n" +
                            "2. Inserire nel programma i polinomi tramite l'apposita voce nel menu' \"Comandi\" o tramite l'apertura di un qualsiasi file\n" +
                            "3. Spuntare le caselle dei polinomi su cui effettuare le operazioni, scelte anch'esse nel medesimo modo\n" +
                            "4. Cliccare sul bottone \"Start\"\n\n\nNota: i polinomi inseriti NON devono contenere caratteri diversi da cifre, \"x\" e \"-, +, ^\"", "Help", JOptionPane.INFORMATION_MESSAGE);
                else if(e.getSource() == about){
                    JOptionPane.showMessageDialog(null, "Applicazione per lavorare con i polinomi", "About", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        private class ListenerStartButton implements ActionListener{
            /*
             * Listener bottone "Start" per lìavvio delle operazioni scelte
             * */
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!addizione.isSelected() && !moltiplicazione.isSelected() && !derivata.isSelected())
                    JOptionPane.showMessageDialog(null, "Nessuna operazione selezionata", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    String ris = "";
                    if(polinomi.size() == 0) {
                        JOptionPane.showMessageDialog(null, "Nessun polinomio inserito", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if(countSelected(checkPolinomi) == 0){
                        JOptionPane.showMessageDialog(null, "Nessun polinomio selezionato", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if(addizione.isSelected()) ris += actionOperation(addizione);
                    if(moltiplicazione.isSelected()) ris += actionOperation(moltiplicazione);
                    if(derivata.isSelected()) ris += actionOperation(derivata);
                    JOptionPane.showMessageDialog(null, ris, "Risultati", JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
        private String actionOperation(JCheckBox cb) {
            String risultati = "";
            Polinomio risultato = new PolinomioLL();
            if(countSelected(checkPolinomi) < 2 && !derivata.isSelected()) {
                JOptionPane.showMessageDialog(null, "Hai selezionato meno di 2 polinomi", "Error", JOptionPane.ERROR_MESSAGE);
                if(addizione.isSelected()) addizione.setSelected(false);
                else if(moltiplicazione.isSelected()) moltiplicazione.setSelected(false);
                else derivata.setSelected(false);
                return null;
            } else {
                if(cb == addizione){
                    for(int i = 0; i < checkPolinomi.size(); i++)
                        if(checkPolinomi.get(i).isSelected())
                            risultato = risultato.add(polinomi.get(i));
                    ris.add(new JCheckBox(risultato.toString()));
                    panelRisultati.add(ris.get(ris.size() - 1));
                    risultati += "Addizione: " + risultato.toString() + "\n";
                    addizione.setSelected(false);
                } else if(cb == moltiplicazione){
                    risultato.add(new Monomio(1, 0));
                    for(int i = 0; i < checkPolinomi.size(); i++)
                        if(checkPolinomi.get(i).isSelected())
                            risultato = risultato.mul(polinomi.get(i));
                    ris.add(new JCheckBox(risultato.toString()));
                    panelRisultati.add(ris.get(ris.size() - 1));
                    risultati += "Moltiplicazione: " + risultato.toString() + "\n";
                    moltiplicazione.setSelected(false);
                }
            }
            if (cb == derivata){
                risultati += "Derivata:\n";
                for (int i = 0; i < checkPolinomi.size(); i++) {
                    if (checkPolinomi.get(i).isSelected()){
                        risultato = polinomi.get(i);
                        derivate.add(risultato.derivata());
                        risultati += derivate.get(derivate.size() - 1) + "\n";
                    }
                    ris.add(new JCheckBox(derivate.get(derivate.size() - 1).toString()));
                    panelRisultati.add(ris.get(ris.size() - 1));
                }
                derivata.setSelected(false);
            }
            Finestra.this.validate();
            return risultati;
        }
        private static boolean consensoUscita(){
            /*
             * Popup per richiesta del consenso di uscita dal programma
             * */
            int option = JOptionPane.showConfirmDialog(
                    null, "Uscendo si perderanno tutti i dati!\n\nContinuare?", "Esci",
                    JOptionPane.YES_NO_OPTION);
            return option == JOptionPane.YES_OPTION;
        }
        private void save(String s){
            /*
             * Salvataggio su file
             * */
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(s));
                for (JCheckBox cb : checkPolinomi)
                    if (cb.isSelected())
                        pw.println(cb.getText());
                pw.close();
            }catch (IOException ioe){
                JOptionPane.showMessageDialog(null, "Errore lettura file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        private void load(String s) throws IOException{
            /*
             * Lettura polinomi da file
             * */
            String polinomiErrati = "", polinomioCorrente = "";
            BufferedReader br = new BufferedReader(new FileReader(s));
            while(br.ready()){
                try{
                    polinomioCorrente = br.readLine();
                    Applicazione.valutaEspressione(polinomioCorrente);
                    polinomi.add(Applicazione.riconosciPolinomio(polinomioCorrente, tipoPolinomio));
                    checkPolinomi.add(new JCheckBox(polinomioCorrente));
                    for(JCheckBox cb: checkPolinomi)
                        panelPolinomi.add(cb);
                    Finestra.this.repaint();
                    Finestra.this.validate();
                }catch(IllegalArgumentException iae){
                    polinomiErrati = polinomiErrati + polinomioCorrente + "\n";
                }catch(Exception e){
                    JOptionPane.showMessageDialog(null, "Errore lettura file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            if(!polinomiErrati.equals(""))
                JOptionPane.showMessageDialog(null, "I seguenti polinomi sono errati:\n" + polinomiErrati, "Error", JOptionPane.ERROR_MESSAGE);
            br.close();
        }
        private void start(){
            /*
            * Il metodo disabilita alcune funzioni dell'applicazione fin quando l'utente non sceglie il tipo di polinomio con cui vuole procedere
            * */
            salva.setEnabled(false);
            salvaConNome.setEnabled(false);
            apri.setEnabled(false);
            aggiungiPolinomio.setEnabled(false);
            modificaPolinomio.setEnabled(false);
            rimuoviPolinomio.setEnabled(false);
            addizione.setEnabled(false);
            moltiplicazione.setEnabled(false);
            derivata.setEnabled(false);
            start.setEnabled(false);
        }
        private void postScelta(){
            /*
            * Il metodo riabilita le funzioni bloccate dal metodo start()
            * */
            if(tipoPolinomio != 0) {
                salva.setEnabled(true);
                salvaConNome.setEnabled(true);
                apri.setEnabled(true);
                aggiungiPolinomio.setEnabled(true);
                modificaPolinomio.setEnabled(true);
                rimuoviPolinomio.setEnabled(true);
                addizione.setEnabled(true);
                moltiplicazione.setEnabled(true);
                derivata.setEnabled(true);
                start.setEnabled(true);
            }
        }
        private static int countSelected(ArrayList<JCheckBox> al){
            /*
            * E' un metodo di supporto per effettuare controlli sulle scelte dell'utente
            * */
            int count = 0;
            for(JCheckBox cb:al)
                if(cb.isSelected()) count++;
            return count;
        }
    }
}