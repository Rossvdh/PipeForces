/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pipes;

import java.io.*;
import java.sql.*; // for when using SQL
import javax.swing.*; //for pop-ups (error messages)
import java.awt.Color;// for when using colours

/**
 *
 * @author Ross
 */
public class Driver {

    //global database connection variable
    private static Connection dbcon = null;

    //**************************************************************************
    //Constructor method
    public Driver() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException c) {
            errorMessageCritical("Unable to load the driver . . . ! Click OK to terminate program.");
        }

        // connect to database
        try {
            String filename = "PipeData.mdb";
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
            database += filename.trim() + ";DriverID=22;READONLY=false}";
            dbcon = DriverManager.getConnection(database, "", "");
        } catch (Exception e) {
            errorMessageCritical("Unable to connect to the database . . . ! Click OK to terminate program!" + e);
        }

    }

    //**************************************************************************
    //Method to disconnect from database
    public void disconnectDB() {
        try {
            dbcon.close();
        } catch (Exception e) {
            errorMessageNormal("Unable to close connection to the database – please contact the programmer!");
        }
    }

    //**************************************************************************
    // Critical error message (Pop-up window) - program must close
    public void errorMessageCritical(String boodskap) {
        Object[] options = {"OK"};
        int s = JOptionPane.showOptionDialog(null, boodskap, "C R I T I C A L   E R R O R ! ",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);
        System.exit(0);
    }

    //**************************************************************************
    // Normal error message (Pop-up window)
    public void errorMessageNormal(String boodskap) {
        Object[] options = {"OK"};
        int s = JOptionPane.showOptionDialog(null, boodskap, " E R R O R  ! ",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);
    }

    //**************************************************************************
    // Normal information message (Pop-up window)
    public void infoMessageNormal(String boodskap) {
        Object[] options = {"OK"};
        int s = JOptionPane.showOptionDialog(null, boodskap, " I N F O R M A T I O N   M E S S A G E ",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
    }

    //Convert Celsius to Farenhiet
    public double CtoF(double cel) {
        double fah = (cel * 1.8) + 32;

        return fah;
    }

    //Convert mm to Feet
    public double MmtoFeet(double mm) {
        double feet = mm / 304.8;

        return feet;
    }

    //******************************************************************************
    //calculate H/w, give answer to user and ask to read off graph
    public double CurveAt(double shortLeg, double longLeg) {
        //Get lengths of legs in feet
        double H = MmtoFeet(shortLeg);
        double w = MmtoFeet(longLeg);

        return (H / w);
    }

    //Calculate and determine whether pipe is safe
    public double DiameterLength(double shortLeg, double longLeg, double hori, String size, String weight) {
        double ans = 0;//variable to be returned
        ResultSet rs = null;

        try { //get Lr and Dr from resultSet
            rs = getElbowData(size, weight);

            rs.next();

            double Dr = Double.parseDouble(rs.getString(1));
            double Lr = Double.parseDouble(rs.getString(2));

            if (hori != 0) {
                Lr = 2 * Lr;
            }

            //convert legs to feet then add legs to get Ls
            double Ls = MmtoFeet(shortLeg) + MmtoFeet(longLeg) + MmtoFeet(hori);
            //get efffective length
            double effLength = Ls + Lr;

            //get effective diameter
            ans = effLength / Dr;

        } catch (SQLException sE) {
            errorMessageNormal("From diameterLength: " + sE);
        } catch (Exception e) {
            errorMessageNormal("From DiameterLength: " + e);
        }

        return ans;//return Diameter lengths
    }

    //Get elbow diameter from database
    private ResultSet getElbowData(String size, String weight) {
        ResultSet rs = null;

        //************************************************


        /* String id1 = Double.toString(size);
         String id2 = Double.toString(weight);
         */
        //****************************************************

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String query = "SELECT Dr, Lr FROM table1 WHERE ID = '" + size + weight + "'";
            System.out.println(query);
            //create ResultSet object
            rs = stmt.executeQuery(query);//send query to database and execute

        } catch (Exception e) {
            errorMessageNormal("From getElbowDiam: " + e);
        }

        return rs;
    }

//Calculate expansion factor
    public double ExpansionFactor(int matIndex, double hot, double cold) {
        double factor = 0;//variable to be returned

        double h = getFEHC(hot, matIndex);
        double c = getFEHC(cold, matIndex);
        factor = h - c;

        return factor;//return variable
    }

    //Calculate computed stress range
    public double StressRange(double Fe, double Fs, double Fl) {
        double range = 0;

        //calculate stress range
        range = (Fe * Fs) / Fl;

        return range;
    }

    //*********************************************************************************
    //method to get metal selected in comboBox
    public String getSelectedMaterial(int index) {
        String material = null;

        try {
            switch (index) {
                case 0: {
                    infoMessageNormal("Please selected a type of material");
                }
                case 1: {
                    material = "CarbonSteelA";
                    break;
                }
                case 2: {
                    material = "CarbonSteelB";
                    break;
                }
                case 3: {
                    material = "CarbonMoly";
                    break;
                }
                case 4: {
                    material = "LowChromeMoly";
                    break;
                }
                case 5: {
                    material = "Nickel";
                    break;
                }
                case 6: {
                    material = "Monel";
                    break;
                }
                case 7: {
                    material = "Aluminum3003H112";
                    break;
                }
                case 8: {
                    material = "Aluminum5154H112";
                    break;
                }
                case 9: {
                    material = "Aluminum6061T6";
                    break;
                }
                case 10: {
                    material = "MediumChromeMoly";
                    break;
                }
                case 11: {
                    material = "AusteniticStainless";
                    break;
                }
            }

        } catch (Exception e) {
            errorMessageNormal("From GetSelectedIndex: " + e);
        }

        return material;
    }

//method to get service selected in comboBox
    public String GetService(int index) {
        String service = "";

        try {
            switch (index) {
                case 0: {
                    infoMessageNormal("Please select a service type");
                    break;
                }
                case 1: {
                    service = "power";
                    break;
                }
                case 2: {
                    service = "oil";
                    break;
                }
                case 3: {
                    service = "ExpansionFactor";
                    break;
                }
            }
        } catch (Exception e) {
            errorMessageNormal("From getService: " + e);
        }

        return service;
    }

    //method to get values opposite temperarues
    //You will have to change this method to accomodate all metals :(
    public double GetAllowableStress(int matIndex, int serviceIndex, double maxTemp) {
        double ans = 0;
        ResultSet rs = null;

        try {
            //determine which method(s) to use depending on metal selected

            // 1-4, 10 - 11:  -20 --> 1200 


            if (matIndex >= 5 & matIndex <= 9) {

                //<editor-fold defaultstate="collapsed" desc="Monel and nickel">
                if (matIndex == 5 | matIndex == 6) {
                    if (maxTemp <= 500 | maxTemp >= -300) {

                        if (serviceIndex == 2) {
                            if (maxTemp >= -300 & maxTemp <= 100) {
                                ans = interNeg300(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > -100 & maxTemp <= -20) {
                                ans = interNeg100(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > -20 & maxTemp <= 0) {
                                ans = interNeg20(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > 0 & maxTemp <= 32) {
                                ans = inter0(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > 32 & maxTemp <= 70) {
                                ans = inter32(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > 70 & maxTemp <= 100) {
                                ans = inter70(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp > 100 & maxTemp < 500) {
                                ans = inter100(maxTemp, serviceIndex, matIndex);
                            }

                            if (matIndex == 5 & maxTemp == 500) {
                                ans = 15000;
                            }

                            if (matIndex == 6 & maxTemp == 500) {
                                ans = 25150;
                            }

                        } else {
                            errorMessageNormal("Please select only oil for Anneled Monel and Nickel.");
                        }

                    }
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Al 3003 and 6061">
                if (matIndex == 7 | matIndex == 9) {
                    if (maxTemp >= -300 & maxTemp <= 400) {

                        if (maxTemp >= -300 & maxTemp <= 100) {
                            ans = interNeg300(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > -100 & maxTemp <= -20) {
                            ans = interNeg100(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > -20 & maxTemp <= 0) {
                            ans = interNeg20(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 0 & maxTemp <= 32) {
                            ans = inter0(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 32 & maxTemp <= 70) {
                            ans = inter32(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 70 & maxTemp <= 100) {
                            ans = inter70(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 100 & maxTemp <= 500) {
                            ans = inter100(maxTemp, serviceIndex, matIndex);
                        }

                    } else {
                        errorMessageNormal("Temperature entered is out of range for material selected.");
                    }
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Al 5154">
                if (matIndex == 8) {
                    if (maxTemp >= -300 & maxTemp <= 300) {
                        if (maxTemp >= -300 & maxTemp <= 100) {
                            ans = interNeg300(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > -100 & maxTemp <= -20) {
                            ans = interNeg100(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > -20 & maxTemp <= 0) {
                            ans = interNeg20(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 0 & maxTemp <= 32) {
                            ans = inter0(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 32 & maxTemp <= 70) {
                            ans = inter32(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 70 & maxTemp <= 100) {
                            ans = inter70(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 100 & maxTemp <= 500) {
                            ans = inter100(maxTemp, serviceIndex, matIndex);
                        }
                    } else {
                        errorMessageNormal("Temperature entered is out of range for material selected.");
                    }
                }
                //</editor-fold>

            } else {// -20 --> 1200

                if (matIndex == 1 | matIndex == 2) {

                    //<editor-fold defaultstate="collapsed" desc="CS A & B power">
                    if (serviceIndex == 1) {
                        if (maxTemp < 0 & maxTemp >= -20) {
                            ans = interNeg20(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 32 & maxTemp >= 0) {
                            ans = inter0(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 70 & maxTemp >= 32) {
                            ans = inter32(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 100 & maxTemp >= 70) {
                            ans = inter70(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp <= 900 & maxTemp >= 100) {
                            ans = inter100(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 900 | maxTemp < -20) {
                            errorMessageNormal("Temperature unsuitable for material selected.");
                        }

                    }
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="CS A&B oil">
                    if (serviceIndex == 2) {
                        if (maxTemp < 0 & maxTemp >= -20) {
                            ans = interNeg20(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 32 & maxTemp >= 0) {
                            ans = inter0(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 70 & maxTemp >= 32) {
                            ans = inter32(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 100 & maxTemp >= 70) {
                            ans = inter70(maxTemp, serviceIndex, matIndex);

                        }

                        if (maxTemp <= 1100 & maxTemp >= 100) {
                            ans = inter100(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp > 1100 | maxTemp < -20) {
                            errorMessageNormal("Temperature unsuitable for material selected.");
                        }

                    }
                    //</editor-fold>
                }


                if (matIndex == 10 | matIndex == 11) {
//<editor-fold defaultstate="collapsed" desc="for medium chrome moly and Stainless">
                    if (maxTemp < 0 & maxTemp >= -20) {
                        ans = interNeg20(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp < 32 & maxTemp >= 0) {
                        ans = inter0(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp < 70 & maxTemp >= 32) {
                        ans = inter32(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp < 100 & maxTemp >= 70) {
                        ans = inter70(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp <= 1200 & maxTemp >= 1000) {
                        ans = inter1000(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp <= 1000 & maxTemp >= 100) {
                        ans = inter100(maxTemp, serviceIndex, matIndex);
                    }

                    if (maxTemp < - 20 | maxTemp > 1200) {
                        errorMessageNormal("Temperatures entered are not suitable for the material selected.");
                    }
                    //</editor-fold>
                }


                //for carbon moly and low chrome moly
                if (matIndex == 3) {

                    //<editor-fold defaultstate="collapsed" desc="CM and LChM  power">
                    if (serviceIndex == 1 & maxTemp <= 1000) {
                        if (maxTemp < 0 & maxTemp >= -20) {
                            ans = interNeg20(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 32 & maxTemp >= 0) {
                            ans = inter0(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 70 & maxTemp >= 32) {
                            ans = inter32(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp < 100 & maxTemp >= 70) {
                            ans = inter70(maxTemp, serviceIndex, matIndex);
                        }

                        if (maxTemp <= 1000 & maxTemp >= 100) {
                            ans = inter100(maxTemp, serviceIndex, matIndex);
                        }

                    } else {
                        errorMessageNormal("Temperature out of range for selected material and service");
                    }
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="CM  oil">
                    if (serviceIndex == 2) {
                        if (maxTemp <= 1100) {
                            if (maxTemp < 0 & maxTemp >= -20) {
                                ans = interNeg20(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp < 32 & maxTemp >= 0) {
                                ans = inter0(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp < 70 & maxTemp >= 32) {
                                ans = inter32(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp < 100 & maxTemp >= 70) {
                                ans = inter70(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp <= 1000 & maxTemp >= 100) {
                                ans = inter100(maxTemp, serviceIndex, matIndex);
                            }

                            if (maxTemp <= 1100 & maxTemp >= 1000) {
                                ans = inter1000(maxTemp, serviceIndex, matIndex);
                            }
                        } else {
                            errorMessageNormal("Temperature entered out of range for material and service selected");
                        }
                    }
                    //</editor-fold>
                }

            }
        } catch (Exception e) {
            errorMessageNormal("From getAllowable stress: " + e);
        }

        return ans;
    }

    public double getFEHC(Double deg, int matIndex) {
        double ans = 0;
        ResultSet rs = null;

        String material = getSelectedMaterial(matIndex);

        try {
            //create statement
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            //determine which method(s) to use depending on metal selected
            // 5-9:  -300 --> 500
            // 1-4, 10 - 11:  -20 --> 1200 
            if (matIndex >= 5 & matIndex <= 9) {
                // -300 ---> 500 °F

                if (deg > 500 | deg < -300) {
                    errorMessageNormal("Temperatures entered are out of range for the material selected");
                }

                if (deg == 500) {
                    try {
                        String query = "SELECT ExpansionFactor FROM " + material + " WHERE MetalTemp = " + 500;
                        rs = stmt.executeQuery(query);//send query to database and execute
                        rs.next();
                        ans = Integer.parseInt(rs.getString(1));
                    } catch (Exception e) {
                    }
                }

                //gap is 50
                if (deg >= 100 & deg < 500) {
                    ans = inter100(deg, 3, matIndex);
                }

                //gap is 30
                if (deg < 100 & deg >= 70) {
                    ans = inter70(deg, 3, matIndex);
                }

                //gap is 38
                if (deg < 70 & deg >= 32) {
                    ans = inter32(deg, 3, matIndex);
                }

                //gap is 32
                if (deg < 32 & deg >= 0) {
                    ans = inter0(deg, 3, matIndex);
                }

                // gap is -20
                if (deg < 0 & deg >= -20) {
                    ans = interNeg20(deg, 3, matIndex);
                }

                //gap is 80
                if (deg < -20 & deg >= -100) {
                    ans = interNeg100(deg, 3, matIndex);
                }

                //gap is 100
                if (deg < -100 & deg >= -300) {
                    ans = interNeg300(deg, 3, matIndex);
                }

            } else {//for other metals, with range -20 --> 1200 °F

                if (deg < -20 | deg > 1200) {
                    errorMessageNormal("Temperatures entered are out of range for the material selected");
                }

                // gap is 20
                if (deg < 0 & deg >= -20) {
                    ans = interNeg20(deg, 3, matIndex);
                }

                if (deg < 32 & deg >= 0) {
                    ans = inter0(deg, 3, matIndex);
                }

                if (deg < 70 & deg >= 32) {
                    ans = inter32(deg, 3, matIndex);
                }

                if (deg < 100 & deg >= 70) {

                    ans = inter70(deg, 3, matIndex);
                }

                if (deg < 1000 & deg >= 100) {
                    ans = inter100(deg, 3, matIndex);
                }

                if (deg <= 1200 & deg >= 1000) {
                    ans = inter1000(deg, 3, matIndex);
                }
            }


        } catch (Exception e) {
            errorMessageNormal("From getFEHC: " + e);
        }


        return ans;
    }

    //read in String containing colour name, then create new Color depending on name
    public Color colour(String name) {
        Color colour = null;

        switch (name) {
            case "red": {
                colour = new Color(255, 51, 51);
                break;
            }
            case "green": {
                colour = new Color(0, 204, 51);
                break;
            }
            case "default": {
                colour = new Color(214, 217, 223);
                break;
            }
            default: {
                errorMessageNormal("Invalid colour name");
            }
        }
        return colour;
    }

    private double inter100(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            int num = (int) (deg / 50);

            int temp = num * 50;

            double percGap = (deg - temp) / 50;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + temp;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + (temp + 50);
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From inter100: " + e);
        }

        return ans;
    }

    private double inter1000(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            int num = (int) (deg / 50);
            int temp = num * 50;
            double percGap = (deg - temp) / 50;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + temp;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + (temp + 50);
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);
        } catch (Exception e) {
            errorMessageNormal("From inter1000: " + e);
        }
        return ans;
    }

    private double inter70(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            double percGap = (deg - 70) / 30;
            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 70;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 100;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From inter1000: " + e);
        }
        return ans;
    }

    private double inter32(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            double percGap = (deg - 32) / 38;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 32;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 70;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From inter32: " + e);
        }
        return ans;
    }

    private double inter0(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            double percGap = (deg - 0) / 32;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 0;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 32;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From inter32: " + e);

        }
        return ans;
    }

    private double interNeg20(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            double percGap = (deg - -20) / 20;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + -20;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + 0;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;
            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From interNeg20: " + e);
        }
        return ans;
    }

    private double interNeg100(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);

        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            double percGap = (deg - -100) / 80;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + -100;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + -20;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From interNeg20: " + e);
        }
        return ans;
    }

    private double interNeg300(double deg, int servIndex, int matIndex) {
        double ans = 0;
        ResultSet rs = null;
        String material = getSelectedMaterial(matIndex);
        String service = GetService(servIndex);
        try {
            Statement stmt = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            int num = (int) (deg / 100);

            int temp = num * 100;

            double percGap = (deg - temp) / 100;

            String query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + temp;
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int lowerXF = Integer.parseInt(rs.getString(1));

            query = "SELECT " + service + " FROM " + material + " WHERE MetalTemp = " + (temp + 100);
            rs = stmt.executeQuery(query);//send query to database and execute
            rs.next();
            int upperXF = Integer.parseInt(rs.getString(1));

            int xFGap = upperXF - lowerXF;

            ans = lowerXF + (percGap * xFGap);

        } catch (Exception e) {
            errorMessageNormal("From interNeg20: " + e);
        }
        return ans;
    }

    public double abscissa(double longLeg, double shortLeg) {
        double ans = MmtoFeet(shortLeg) / MmtoFeet(longLeg);

        return ans;
    }
}
