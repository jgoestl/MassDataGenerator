/*
 * The MIT License
 *
 * Copyright 2014 Julian Goestl.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.jgoestl.massdatagenerator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author j.goestl
 */
public class MassDataGenerator {
    
    private static final Pattern uuidPattern = Pattern.compile("#UUID#");
    private static final Pattern seqPattern = Pattern.compile("#SEQ#");
    private static final Pattern datePattern = Pattern.compile("#DATE#");
    private static final String DEFAULT_DATE_FORMAT = "YYY-MM-d H:m:s.S";
    
    private static Logger logger = Logger.getLogger(MassDataGenerator.class.getName());

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // CommandLine options
        Options options = new Options();
        options.addOption("i", "input", true, "The input file");
        options.addOption("o", "output", true, "The output file");
        options.addOption("c", "count", true, "The amount of generatet data");
        options.addOption("d", "dateFormat", true, "A custom date format");
        options.addOption("h", "help", false, "Show this");
        
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        // Show the help
        if(cmd.hasOption("help") || !cmd.hasOption("input") || !cmd.hasOption("output") || !cmd.hasOption("count")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("massDataGenerator", options);
            
            System.out.println("\nFollowing in your input file will be replaced:");
            System.out.println("#UUID# - A random UUID");
            System.out.println("#SEQ#  - A consecutive number (starting with 1)");
            System.out.println("#DATE# - The current date (YYYY-MM-d h:m:s.S)");
            
            System.exit(0);
        }
        
        // Get values and validate
        String inputFilePath = cmd.getOptionValue("input");
        String outputPath = cmd.getOptionValue("output");
        int numberOfData = getNumberOfData(cmd);
        validate(inputFilePath, outputPath, numberOfData);
        DateFormat dateFormat = getDateFormat(cmd);
        
        // Read, generte and write Data
        String inputString = null;
        inputString = readInputFile(inputFilePath, inputString);
        StringBuilder output = generateOutput(numberOfData, inputString, dateFormat);
        writeOutput(outputPath, output);
    }

    
    private static int getNumberOfData(CommandLine cmd) {
        int numberOfData = 0;
        try {
            numberOfData = Integer.parseInt(cmd.getOptionValue("count"));
        } catch(NumberFormatException ex) {
            System.out.println("Count must be a number");
            try {
                System.in.read();
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
            System.exit(1);
        }
        return numberOfData;
    }
    
    
    private static void validate(String inputFilePath, String outputPath, int numberOfData) {
        if(inputFilePath == null || outputPath == null) {
            System.out.println("Input- and Output-Path must not be null");
            try {
                System.in.read();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            System.exit(1);
        }
        if(numberOfData < 1) {
            System.out.println("Count must be greater than 0");
            try {
                System.in.read();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            System.exit(1);
        }
    }
    
    
    private static DateFormat getDateFormat(CommandLine cmd) {
        DateFormat dateFormat = null;
        if(cmd.hasOption("dateFormat")) {
            try {
                dateFormat = new SimpleDateFormat(cmd.getOptionValue("dateFormat"));
            } catch (IllegalArgumentException e) {
//                System.out.println("Invalid date format");
                logger.log(Level.SEVERE, e.getMessage(), e);
                System.exit(1);
            }
        } else {
            dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }
        return dateFormat;
    }
    
    
    private static String readInputFile(String inputFilePath, String inputString) {
        BufferedReader br = null;
        try {
            File input = new File(inputFilePath);
            br = new BufferedReader(new FileReader(input));
            StringBuilder inputStringBuilder = new StringBuilder();
            char[] readBuffer = new char[1024];
            while(-1 != br.read(readBuffer)) {
                inputStringBuilder.append(readBuffer);
            }
            inputString = inputStringBuilder.toString();
        } catch (FileNotFoundException ex) {
            System.out.println("Input-File \"" + inputFilePath + "\" not found");
            try {
                System.in.read();
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
            System.exit(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(br != null)
                    br.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return inputString;
    }
    
    
    private static StringBuilder generateOutput(int numberOfData, String inputString, DateFormat dateFormat) {
        StringBuilder output = new StringBuilder();
        for(int i = 1; i <= numberOfData; i++) {
            String part = inputString;
            
            Matcher uuidMatcher = uuidPattern.matcher(part);
            if(uuidMatcher.find()) {
                part = uuidMatcher.replaceAll(UUID.randomUUID().toString());
            }
            
            Matcher seqMatcher = seqPattern.matcher(part);
            if(seqMatcher.find()) {
                part = seqMatcher.replaceAll(String.valueOf(i));
            }
            
            Matcher dateMatcher = datePattern.matcher(part);
            if(dateMatcher.find()) {
                Calendar now = Calendar.getInstance();
                part = dateMatcher.replaceAll(getFormattedDate(now, dateFormat));
            }
            
            output.append(part).append("\r\n");
        }
        return output;
    }

    
    private static void writeOutput(String outputPath, StringBuilder output) {
        BufferedOutputStream bos = null;
        try {
            File f = new File(outputPath);
            bos = new BufferedOutputStream(new FileOutputStream(f));
            bos.write(output.toString().getBytes());
        } catch (FileNotFoundException ex) {
            System.out.println("Output-File \"" + outputPath + "\" not found");
            try {
                System.in.read();
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
            System.exit(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(bos != null)
                    bos.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    private static String getFormattedDate(Calendar cal, DateFormat dateFormat) {
        return dateFormat.format(cal.getTime());
    }
    
}
