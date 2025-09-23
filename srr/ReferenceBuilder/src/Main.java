import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Main {
    private static File _oldDialogFile;
    private static File _newDialogFile;
    private static Hashtable<Integer, String> _oldTLK;
    private static Hashtable<Integer, String> _newTLK;
    private static Hashtable<Integer, Integer> _comparisonTable;

    public static void main(String[] args){
        boolean processCommandLine = true;
        if(args.length == 1){
            processCommandLine = !ProcessParamFile(args[0]);
        }
        if(processCommandLine){
            FetchParams();
        }
        _oldTLK = new Hashtable<>();
        _newTLK = new Hashtable<>();
        ProcessTLKFile(_oldDialogFile, _oldTLK);
        ProcessTLKFile(_newDialogFile, _newTLK);
        System.out.println(_oldTLK.size());
    }
    private static boolean ProcessParamFile(String pathToFile){
        File paramFile = new File(pathToFile);
        if(paramFile.exists()){
            try{
                Scanner fileScanner = new Scanner(paramFile);
                _oldDialogFile = new File(fileScanner.nextLine());
                if(_oldDialogFile.exists()){
                    _newDialogFile = new File(fileScanner.nextLine());
                    if(_newDialogFile.exists()){
                        return true;
                    }
                }
            }
            catch(Exception e) {
                System.err.println(e.getMessage());
            }
        }
        System.err.println("Invalid params file.");
        return false;
    }
    private static File GetFile(String prompt){
        boolean validPath = false;
        File inFile = null;
        do {
            try{
                System.out.print(prompt);
                Scanner readIn = new Scanner(System.in);
                inFile = new File(readIn.nextLine());
                if(inFile.exists()){
                    validPath = true;
                }
            }
            catch(Exception ex){
                System.err.println(ex.getMessage());
            }
        }
        while(!validPath);
        return inFile;
    }
    private static void FetchParams(){
        _oldDialogFile = GetFile("Provide the absolute path to the OLD dialog.tlk file for your EE install: ");
        _newDialogFile = GetFile("Provide the absolute path to the NEW dialog.tlk file for your EE install: ");
        BuildComparisonTable();
    }

    private static void BuildComparisonTable(){
        Hashtable<String, Integer > newTLKStringKey = new Hashtable<>();
        for(Integer key : _newTLK.keySet()){
            newTLKStringKey.put(_newTLK.get(key), key);
        }
        _comparisonTable = new Hashtable<>();
        for(Integer key: _oldTLK.keySet()){
            String value = _oldTLK.get(key);
            if(newTLKStringKey.containsKey(value)){
                _comparisonTable.put(key, newTLKStringKey.get(value));
            }
        }
    }

    private static void ProcessTLKFile(File tlk, Dictionary<Integer, String> tlkTable){
        try{
            System.out.println("Processing tlk file: " + tlk.toPath());
            String fileText = Files.readString(tlk.toPath(), StandardCharsets.UTF_16LE);
            String[] splitText = fileText.split("= ~");
            int referenceID = 0;
            String traText = "";
            for(int i = 1; i < splitText.length; i++){
                String prior = splitText[i-1];
                String current = splitText[i];
                referenceID = Integer.parseInt(prior.substring(prior.lastIndexOf("@")+1,
                        prior.lastIndexOf(" ")).trim());
                int endIndex = current.lastIndexOf("@");
                if(endIndex > -1){
                    traText = "~" + current.substring(0, endIndex);
                }
                else{
                    traText = "~" + current;
                }
                tlkTable.put(referenceID, traText);
            }
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
        }

    }

}
