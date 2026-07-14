import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class GUI {

    public static void MainMenu(JPanel topPanel, JPanel bottomPanel, JFrame frame, HashMap<String, Main.Track> Tracks){
        try{
            frame.remove(topPanel);
            frame.remove(bottomPanel);} catch (Exception ignored) {}
        topPanel.removeAll();
        bottomPanel.removeAll();

        JButton AddTracksMenuButton = new JButton("Add Tracks");

        AddTracksMenuButton.addActionListener(e -> {
            GUI.AddTracksMenu(topPanel, bottomPanel, frame, Tracks);

        });


        JButton OptimalButton = new JButton("Optimal");

        OptimalButton.addActionListener(e -> {
            GUI.OptMenu(topPanel, bottomPanel, frame, Tracks);

        });

        JButton DeleteTracksMenuButton = new JButton("Delete Tracks");

        DeleteTracksMenuButton.addActionListener(e -> {
            GUI.DeleteTracksMenu(topPanel, bottomPanel, frame, Tracks);

        });

        bottomPanel.add(OptimalButton);

        bottomPanel.add(AddTracksMenuButton);

        bottomPanel.add(DeleteTracksMenuButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }

    public static void OptMenu(JPanel topPanel, JPanel bottomPanel, JFrame frame, HashMap<String, Main.Track> Tracks){
        try{
            frame.remove(topPanel);
            frame.remove(bottomPanel);} catch (Exception ignored) {}
        topPanel.removeAll();
        bottomPanel.removeAll();


        ArrayList<String> trackNames = new ArrayList<>(Tracks.keySet());

        JComboBox<String> TrackDropdown =
                new JComboBox<>(trackNames.toArray(new String[0]));
        topPanel.add(TrackDropdown);


        JTextField lapCount = new JTextField(4);
        topPanel.add(lapCount);

        JPanel MidPanel = new JPanel();


        JButton OptimalButton = new JButton("Optimal");
        OptimalButton.addActionListener(e -> {
            // 1. Immediately update UI to show "Loading" before the thread starts
            try {
                MidPanel.removeAll();
            } catch(Exception ignored){}

            JLabel statusLabel = new JLabel("(Loading...)");
            MidPanel.add(statusLabel);
            frame.add(MidPanel, BorderLayout.CENTER); // Ensure layout manager places it correctly

            frame.revalidate();
            frame.repaint();

            // 2. Start background processing thread
            Thread thread = new Thread(() -> {
                String currentTrack = (String) TrackDropdown.getSelectedItem();
                int laps = Integer.parseInt(lapCount.getText());

                // This long calculation runs in the background
                ArrayList<Main.StintInfo> Strat = Main.Optimal(4, Tracks.get(currentTrack), laps);

                String output = "";
                for (Main.StintInfo stint : Strat){
                    output += stint.getLaps() + " laps on " + stint.getCompound() + "       ";
                }

                // Final string effectively replacing 'output' variable
                final String finalOutput = output;

                // 3. Push GUI updates back onto the Swing Event Dispatch Thread (EDT)
                SwingUtilities.invokeLater(() -> {
                    try {
                        MidPanel.removeAll();
                    } catch(Exception ignored){}

                    MidPanel.add(new JLabel(finalOutput));

                    frame.revalidate();
                    frame.repaint();
                });
            });
            thread.start();
        });
        bottomPanel.add(OptimalButton);


        JButton OptimalButton2 = new JButton("Optimal2");
        OptimalButton2.addActionListener(e -> {

            String currentTrack = (String) TrackDropdown.getSelectedItem();
            int laps = Integer.parseInt(lapCount.getText());
            ArrayList<ArrayList<Main.StintInfo>> Strats = Main.OptimalList(4, Tracks.get(currentTrack), laps);
            for (ArrayList<Main.StintInfo> Strat:Strats){
                for (Main.StintInfo Stint:Strat){
                    System.out.println(Stint.getLaps());
                    System.out.println(Stint.getCompound());
                }
                System.out.println("\n");
            }



        });
        //bottomPanel.add(OptimalButton2);




        JButton Back = new JButton("Back");

        Back.addActionListener(e -> {

            try{
                MidPanel.removeAll();
            } catch(Exception ignored){}
            try{
                frame.remove(MidPanel);
            } catch(Exception ignored){}


            MainMenu(topPanel, bottomPanel, frame, Tracks);



        });
        bottomPanel.add(Back);



        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }

    public static void AddTracksMenu(JPanel topPanel, JPanel bottomPanel, JFrame frame, HashMap<String, Main.Track> Tracks){

        topPanel.removeAll();
        bottomPanel.removeAll();
        frame.getContentPane().removeAll();

        ArrayList<JPanel> MIDs = new ArrayList<>();
        for (int i=0; i<8; i++){
            MIDs.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
        }




        ArrayList<String> Compounds = new ArrayList<>();
        Compounds.add("soft");
        Compounds.add("medium");
        Compounds.add("hard");

        int lapCount = 25;
        HashMap<String, ArrayList<JTextField>> LapEntries = new HashMap<>();

        for (String compound : Compounds){
            ArrayList<JTextField> fields = new ArrayList<>();
            for (int i = 0; i < lapCount; i++){
                fields.add(new JTextField(2));
            }
            LapEntries.put(compound, fields);
        }


        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(new JLabel("soft Laptimes: "));
        for (int i = 0; i < lapCount; i++){
            topPanel.add(LapEntries.get("soft").get(i));
        }
        MIDs.get(0).add(new JLabel("medium Laptimes: "));
        for (int i = 0; i < lapCount; i++){
            MIDs.get(0).add(LapEntries.get("medium").get(i));
        }

        MIDs.get(1).add(new JLabel("hard Laptimes: "));

        for (int i = 0; i < lapCount; i++){
            MIDs.get(1).add(LapEntries.get("hard").get(i));
        }

        HashMap<String, Component> otherAttributes = new HashMap<>();

        otherAttributes.put("PitLoss", new JLabel("PitLoss: "));
        otherAttributes.put("PitLosse", new JTextField(10));
        otherAttributes.put("StartLoss", new JLabel("StartLoss: "));
        otherAttributes.put("StartLosse", new JTextField(10));
        otherAttributes.put("Name", new JLabel("Name: "));
        otherAttributes.put("Namee", new JTextField(10));
        otherAttributes.put("FuelLoss", new JLabel("FuelLoss: "));
        otherAttributes.put("FuelLosse", new JTextField(10));
        otherAttributes.put("Laps", new JLabel("Laps: "));
        otherAttributes.put("Lapse", new JTextField(10));

        MIDs.get(2).add(otherAttributes.get("PitLoss"));
        MIDs.get(2).add(otherAttributes.get("PitLosse"));
        MIDs.get(3).add(otherAttributes.get("StartLoss"));
        MIDs.get(3).add(otherAttributes.get("StartLosse"));
        MIDs.get(4).add(otherAttributes.get("Name"));
        MIDs.get(4).add(otherAttributes.get("Namee"));
        MIDs.get(5).add(otherAttributes.get("FuelLoss"));
        MIDs.get(5).add(otherAttributes.get("FuelLosse"));
        MIDs.get(6).add(otherAttributes.get("Laps"));
        MIDs.get(6).add(otherAttributes.get("Lapse"));



        JPanel rowsContainer = new JPanel(new GridLayout(8, 1));
        for (int i=0; i<8; i++){
            rowsContainer.add(MIDs.get(i));
        }






        JButton Submit = new JButton("Submit");
        Submit.addActionListener(e -> {
            // FIX 2: Read data from JTextFields IMMEDIATELY on the EDT main thread
            HashMap<String, ArrayList<Double>> LapTimes = new HashMap<>();
            try {
                for (String compound : Compounds) {
                    LapTimes.put(compound, new ArrayList<>());
                    for (int i = 0; i < lapCount; i++) {
                        LapTimes.get(compound).add(Double.parseDouble(LapEntries.get(compound).get(i).getText()));
                    }
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid lap time input: " + nfe);
                return; // Stop execution if inputs aren't valid numbers
            }

            final String pitLossText = ((JTextField) otherAttributes.get("PitLosse")).getText();
            final String startLossText = ((JTextField) otherAttributes.get("StartLosse")).getText();
            final String nameText = ((JTextField) otherAttributes.get("Namee")).getText();
            final String fuelLossText = ((JTextField) otherAttributes.get("FuelLosse")).getText();
            final String lapseText = ((JTextField) otherAttributes.get("Lapse")).getText();

            // FIX 3: Update local panel safely without hijacking the main JFrame layout
            try {
                MIDs.get(7).removeAll();
            } catch(Exception ignored){}

            JLabel statusLabel = new JLabel("(Loading...)");
            MIDs.get(7).add(statusLabel);

            // Trigger visual redraw instantly
            frame.revalidate();
            frame.repaint();

            // Safe to process slow database/heavy computing on background thread now
            Thread thread = new Thread(() -> {
                try {
                    Main.InsertIntoDataBase(
                            Double.parseDouble(pitLossText),
                            Double.parseDouble(startLossText),
                            nameText,
                            Double.parseDouble(fuelLossText),
                            Integer.parseInt(lapseText),
                            LapTimes
                    );
                } catch(Exception f) {
                    System.out.println("DB Insertion Failed: " + f);
                }

                HashMap<String, Main.Track> TracksNew = new HashMap<>();
                try {
                    Main.InsertAllFromDB(TracksNew);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                // FIX 1: Safely push UI instantiation/refresh back to the Swing EDT
                SwingUtilities.invokeLater(() -> {
                    AddTracksMenu(topPanel, bottomPanel, frame, TracksNew);
                });
            });

            thread.start();
        });


        bottomPanel.add(Submit);


        JPanel centerWrapper = new JPanel(new BorderLayout());

        centerWrapper.add(rowsContainer, BorderLayout.NORTH);

        JButton Back = new JButton("Back");
        Back.addActionListener(e -> {
            frame.remove(centerWrapper);
            try {
                Main.InsertAllFromDB(Tracks);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            MainMenu(topPanel, bottomPanel, frame, Tracks);
        });

        bottomPanel.add(Back);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerWrapper, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // 6. Refresh the UI to reflect changes
        frame.revalidate();
        frame.repaint();
    }

    public static void DeleteTracksMenu(JPanel topPanel, JPanel bottomPanel, JFrame frame, HashMap<String, Main.Track> Tracks){
        try {
            frame.remove(topPanel);
            frame.remove(bottomPanel);
        } catch (Exception ignored) {}

        topPanel.removeAll();
        bottomPanel.removeAll();

        HashMap<String, Main.Track> TracksNew = new HashMap<>();


        ArrayList<String> trackNames = new ArrayList<>(Tracks.keySet());

        JComboBox<String> TrackDropdown =
                new JComboBox<>(trackNames.toArray(new String[0]));
        topPanel.add(TrackDropdown);

        JButton DeleteTrackButton = new JButton("Delete");
        DeleteTrackButton.addActionListener(e -> {
            try {
                Main.DeleteTrack((String) TrackDropdown.getSelectedItem());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            try {
                Main.InsertAllFromDB(TracksNew);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            DeleteTracksMenu(topPanel, bottomPanel, frame, TracksNew);
        });
        bottomPanel.add(DeleteTrackButton);

        JButton Back = new JButton("Back");

        try{
            Tracks = TracksNew;
        } catch (Exception ignored) {}

        HashMap<String, Main.Track> finalTracks = Tracks;
        Back.addActionListener(e -> {
            try {
                Main.InsertAllFromDB(finalTracks);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }


            MainMenu(topPanel, bottomPanel, frame, finalTracks);

        });
        bottomPanel.add(Back);




        frame.add(topPanel, BorderLayout.CENTER); // Changed to CENTER so it stretches nicely to contain fields
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }

    public static void GUIInit() throws SQLException {
        //Creates and populated the HashMap for all the Tracks
        HashMap<String, Main.Track> Tracks = new HashMap<>();
        Main.InsertAllFromDB(Tracks);


        JFrame frame = new JFrame("Stratify");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1280, 720));
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottomPanel.setBackground(new Color(240, 240, 240));


        MainMenu(topPanel, bottomPanel, frame, Tracks);

        frame.setVisible(true);
    }

}
