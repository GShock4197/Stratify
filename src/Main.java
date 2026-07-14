import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.sql.*;
import java.util.List;

public class Main{

    public static double[] regression(double[] x, double[] y, int n) {
        double sumX = 0.0, sumY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
        }
        double xBar = sumX / n;
        double yBar = sumY / n;

        double varianceX = 0.0;
        double covarianceXY = 0.0;
        for (int i = 0; i < n; i++) {
            varianceX += (x[i] - xBar) * (x[i] - xBar);
            covarianceXY += (x[i] - xBar) * (y[i] - yBar);}
        double slope = covarianceXY / varianceX;
        double intercept = yBar - slope * xBar;
        return new double[]{slope, intercept};}

    public static String ConvertToStandardTime(double time){
        int minutes = (int) time/60;
        double seconds = time%60.0;
        return minutes + ":" + Math.round(seconds * 1000.0)/1000.0;}

    public static class DbUtils{
        public static Connection connect(String dbPath) throws SQLException {
            String url = "jdbc:sqlite:" + dbPath;

            // By using "DriverManager" here, we break the infinite loop.
            // We are calling Java's built-in tool, NOT this method again.
            Connection myConn = DriverManager.getConnection(url);

            if (myConn == null) {
                System.out.println("Could not establish connection.");
            }

            return myConn;}


        // A single, reusable method for ALL select queries
        public static List<HashMap<String, Object>> executeQuery(Connection conn, String sql, Object... params) {
            List<HashMap<String, Object>> rows = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 1. Dynamically bind whatever parameters were passed in
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]); // JDBC is 1-indexed
                }

                // 2. Execute and automatically read the schema metadata
                try (ResultSet rs = pstmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // 3. Loop through rows and dynamically map columns to values
                    while (rs.next()) {
                        HashMap<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object columnValue = rs.getObject(i);
                            row.put(columnName, columnValue);
                        }
                        rows.add(row);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Query execution failed: " + e.getMessage());
            }
            return rows;
        }
        public static void executeInsert(Connection conn, String sql, Object... params) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 1. Dynamically bind whatever parameters were passed in
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }

                // 2. Use executeUpdate() for INSERT/UPDATE/DELETE
                int rowsAffected = pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Insert execution failed: " + e.getMessage());
            }
        }





        public static void AddTrackFromDB(HashMap<String, Track> Tracks, String TrackName) throws SQLException {
            Connection conn = DbUtils.connect("Track.db");
            double PitLoss1 = GetAllTrackAttributes.GetPitLoss(TrackName, conn);
            double StartLoss1 = GetAllTrackAttributes.GetStartLoss(TrackName, conn);
            double FuelLoss1 = GetAllTrackAttributes.GetFuelLoss(TrackName, conn);
            int Laps1 = GetAllTrackAttributes.GetLaps(TrackName, conn);
            Tracks.put(TrackName, new Track(PitLoss1, StartLoss1, TrackName, FuelLoss1, Laps1));
            conn.close();
        }


    }

    public static class StintInfo{
        int Laps;
        String Compound;
        public StintInfo(int Laps, String Compound){this.Laps = Laps; this.Compound = Compound;}
        public int getLaps(){return this.Laps;}
        public String getCompound(){return this.Compound;}
    }

    public static class GetAllTrackAttributes{
        public static double GetPitLoss(String TrackID, Connection conn) throws SQLException {
            List<HashMap<String, Object>> PitLoss = DbUtils.executeQuery(conn, "SELECT PitLoss FROM Tracks WHERE TrackID = ?", TrackID);
            return (double) PitLoss.get(0).get("pitloss");
        }
        public static double GetStartLoss(String TrackID, Connection conn) throws SQLException {
            List<HashMap<String, Object>> StartLoss = DbUtils.executeQuery(conn, "SELECT startloss FROM Tracks WHERE TrackID = ?", TrackID);
            return (double) StartLoss.get(0).get("startloss");
        }
        public static double GetFuelLoss(String TrackID, Connection conn) throws SQLException {
            List<HashMap<String, Object>> StartLoss = DbUtils.executeQuery(conn, "SELECT fuelloss FROM Tracks WHERE TrackID = ?", TrackID);
            return (double) StartLoss.get(0).get("fuelloss");
        }
        public static int GetLaps(String TrackID, Connection conn) throws SQLException {
            List<HashMap<String, Object>> Laps = DbUtils.executeQuery(conn, "SELECT laps FROM Tracks WHERE TrackID = ?", TrackID);
            return (int) Laps.get(0).get("laps");
        }
    }

    public static class Track{

        double PitLoss;
        double StartLoss;
        String Name;
        double FuelLoss;
        int Laps;
        HashMap<String, double[]> CompoundData;

        public Track(double PitLoss, double StartLoss, String Name, double FuelLoss, int Laps){
            this.PitLoss = PitLoss;
            this.StartLoss = StartLoss;
            this.Name = Name;
            this.FuelLoss = FuelLoss;
            this.Laps = Laps;
            this.CompoundData = new HashMap<>();
            this.CacheCompoundData();
        }

        private void CacheCompoundData(){
            ArrayList<String> Compounds = new ArrayList<>();
            Compounds.add("soft");
            Compounds.add("medium");
            Compounds.add("hard");

            for (String Compound : Compounds){
                try (Connection conn = DbUtils.connect("Track.db")) {
                    if (conn == null) return;

                    String sql1 = "SELECT laptime FROM Laps WHERE TrackID = ? AND compound = ?";
                    List<HashMap<String, Object>> Laps = DbUtils.executeQuery(conn, sql1, this.Name, Compound);

                    // Construct the Y dataset (laptimes)
                    double[] y = Laps.stream()
                            .mapToDouble(lap -> (Double) lap.get("laptime"))
                            .toArray();
                    int n = y.length;

                    if (n == 0) continue; // Skip if no data found

                    // Construct the X dataset (0 to n-1) automatically without extra loops or boxing
                    double[] x = new double[n];
                    for (int i = 0; i < n; i++) {
                        x[i] = i;
                    }

                    // FIX: Compute both slope [0] and intercept [1] in ONE single pass
                    double[] regressionValues = regression(x, y, n);
                    this.CompoundData.put(Compound, regressionValues);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        public double Stint(int Laps, String Compound){
            double d = this.CompoundData.get(Compound)[0];
            double a = this.CompoundData.get(Compound)[1];
            return ((double) Laps /2)*(2*a + (Laps-1)*d);
        }

        public double Race(ArrayList<StintInfo> StintList){
            int count = 0;
            double time = 0;
            for (StintInfo Stints:StintList){
                if (count!=0){time+=this.PitLoss;}
                time+=Stint(Stints.getLaps(), Stints.getCompound());
                count+=1;
            }
            return time;
        }

        public int getLaps(){
            return this.Laps;
        }

    }

    public static void InsertAllFromDB(HashMap<String, Track> Tracks) throws SQLException {
        List<HashMap<String, Object>> TrackList= DbUtils.executeQuery(DbUtils.connect("Track.db"), "SELECT DISTINCT TrackID FROM Tracks");

        for (HashMap<String, Object> TrackInstance:TrackList){DbUtils.AddTrackFromDB(Tracks, (String) TrackInstance.get("TrackID"));}
    }

    public static class StrategyGenerator {

        public static void findStints(int lapsLeft, int stintsLeft, ArrayList<Integer> currentStrat, ArrayList<ArrayList<Integer>> combinations) {
            if (stintsLeft == 1) {
                if (lapsLeft >= 2) {
                    ArrayList<Integer> fullStrat = new ArrayList<>(currentStrat);
                    fullStrat.add(lapsLeft);
                    combinations.add(fullStrat);
                }
                return;
            }

            int maxLength = lapsLeft - ((stintsLeft - 1) * 2);

            for (int stintLength = 2; stintLength <= maxLength; stintLength++) {

                ArrayList<Integer> nextStrat = new ArrayList<>(currentStrat);
                nextStrat.add(stintLength);
                findStints(lapsLeft - stintLength, stintsLeft - 1, nextStrat, combinations);
            }
        }

        public static ArrayList<ArrayList<Integer>> generateStintCombinations(int numStints, int laps) {
            ArrayList<ArrayList<Integer>> combinations = new ArrayList<>();
            findStints(laps, numStints, new ArrayList<>(), combinations);
            return combinations;
        }

        public static void FindCompounds(int StintsLeft, ArrayList<String> CurrentCombination, ArrayList<ArrayList<String>> Result){
            ArrayList<String> Compounds = new ArrayList<>();
            Compounds.add("soft");
            Compounds.add("medium");
            Compounds.add("hard");

            if (StintsLeft==0){
                Result.add(new ArrayList<>(CurrentCombination));
                return;
            }

            for (String Compound:Compounds){
                CurrentCombination.add(Compound);
                FindCompounds(StintsLeft-1, CurrentCombination, Result);

                CurrentCombination.remove(CurrentCombination.size()-1);
            }

        }

        public static ArrayList<ArrayList<String>> GenerateCompounds(int NumStints){
            ArrayList<ArrayList<String>> Result = new ArrayList<>();
            FindCompounds(NumStints, new ArrayList<>(), Result);
            Result.removeIf(combination -> new LinkedHashSet<>(combination).size() <= 1);
            return Result;
        }
    }

    public static ArrayList<StintInfo> Optimal(int MaxStints, Track TrackInstance, int Laps){
        ArrayList<ArrayList<StintInfo>> ListOfStintList = new ArrayList<>();

        for (int i = 2; i <= MaxStints; i++){
            ArrayList<ArrayList<Integer>> StintDistList = StrategyGenerator.generateStintCombinations(i, Laps);
            ArrayList<ArrayList<String>> CompoundList = StrategyGenerator.GenerateCompounds(i);

            for (ArrayList<String> CompoundDistro : CompoundList){
                for (ArrayList<Integer> LapsDistro : StintDistList){
                    ArrayList<StintInfo> StintInstance = new ArrayList<>();
                    for (int j = 0; j < i; j++){
                        StintInstance.add(new StintInfo(LapsDistro.get(j), CompoundDistro.get(j)));
                    }
                    ListOfStintList.add(StintInstance);
                }
            }
        }

        if (ListOfStintList.isEmpty()) {
            return new ArrayList<>(); // Edge-case protection
        }

        // FIX: Track the current best strategy and its score explicitly
        ArrayList<StintInfo> currentOPT = ListOfStintList.get(0);
        double bestTime = TrackInstance.Race(currentOPT); // Calculate ONCE upfront

        for (ArrayList<StintInfo> Challenger : ListOfStintList) {
            double challengerTime = TrackInstance.Race(Challenger); // Calculate ONCE per iteration

            if (challengerTime < bestTime) {
                bestTime = challengerTime;
                currentOPT = Challenger;
            }
        }
        return currentOPT;
    }

    public static ArrayList<ArrayList<StintInfo>> OptimalList(int MaxStints, Track TrackInstance, int Laps){
        ArrayList<ArrayList<StintInfo>> ListOfStintList = new ArrayList<>();

        for (int i = 2; i <= MaxStints; i++){
            ArrayList<ArrayList<Integer>> StintDistList = StrategyGenerator.generateStintCombinations(i, Laps);
            ArrayList<ArrayList<String>> CompoundList = StrategyGenerator.GenerateCompounds(i);

            for (ArrayList<String> CompoundDistro : CompoundList){
                for (ArrayList<Integer> LapsDistro : StintDistList){
                    ArrayList<StintInfo> StintInstance = new ArrayList<>();
                    for (int j = 0; j < i; j++){
                        StintInstance.add(new StintInfo(LapsDistro.get(j), CompoundDistro.get(j)));
                    }
                    ListOfStintList.add(StintInstance);
                }
            }
        }

        if (ListOfStintList.isEmpty()) {
            return new ArrayList<>(); // Edge-case protection
        }

        // FIX: Track the current best strategy and its score explicitly
        ArrayList<StintInfo> currentOPT = ListOfStintList.get(0);
        double bestTime = TrackInstance.Race(currentOPT); // Calculate ONCE upfront
        ArrayList<ArrayList<StintInfo>> listOfOtps = new ArrayList<>();
        for (ArrayList<StintInfo> Challenger : ListOfStintList) {
            double challengerTime = TrackInstance.Race(Challenger); // Calculate ONCE per iteration

            if (challengerTime < bestTime) {
                bestTime = challengerTime;
                listOfOtps.add(Challenger);
            }
        }
        ArrayList<ArrayList<StintInfo>> finalList = new ArrayList<>();
        int lenOpt = listOfOtps.toArray().length;
        for (int i=1; i<22; i++){
            finalList.add(listOfOtps.get(lenOpt-i));
        }

        return finalList;
    }

    public static void InsertIntoDataBase(double PitLoss, double StartLoss, String Name, double FuelLoss, int Laps, HashMap<String, ArrayList<Double>> LapTimes) throws SQLException {

        Connection ConnectionInstance = DbUtils.connect("Track.db");

        DbUtils.executeInsert(ConnectionInstance, "INSERT INTO Tracks(TrackID, pitloss, laps, startloss, fuelloss) VALUES(?, ?, ?, ?, ?)", Name, PitLoss, Laps, StartLoss, FuelLoss);
        ArrayList<String> Compounds = new ArrayList<>();
        Compounds.add("soft");
        Compounds.add("medium");
        Compounds.add("hard");

        for (String compound:Compounds){
            int count = 0;
            for (double laptime:LapTimes.get(compound)){
                count+=1;
                DbUtils.executeInsert(ConnectionInstance, "INSERT INTO Laps(TrackID, lapnumber, laptime, compound) VALUES(?, ?, ?, ?)", Name, count, laptime, compound);
            }}

        ConnectionInstance.close();

    }

    public static void DeleteTrack(String TrackID) throws SQLException {
        DbUtils.executeInsert(DbUtils.connect("Track.db"), "DELETE FROM Tracks WHERE TrackID = ?", TrackID);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GUI.GUIInit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}