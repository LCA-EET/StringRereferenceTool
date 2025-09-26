import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Main {
    private static File _oldDialogFile;
    private static File _newDialogFile;
    private static File _saveGameFile;
    private static File _saveGameCopy;
    private static Hashtable<Integer, String> _oldTLK;
    private static Hashtable<Integer, String> _newTLK;
    private static Hashtable<Integer, Integer> _comparisonTable;
    private static byte[] _saveFileContents;

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
        BuildComparisonTable();
        if(UpdateSaveFile()){
            System.out.println("Save file processed successfully.");
        }
        else{
            System.out.println("An error occurred during processing. The save file has been restored from the backup.");
        }
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
                        _saveGameFile = new File(fileScanner.nextLine());
                        if(_saveGameFile.exists()){
                            return true;
                        }
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
        _saveGameFile = GetFile("Provide the path to the BALDUR.gam file to update: ");
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
            int referenceID;
            String traText;
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

    private static boolean UpdateSaveFile()
    {
        ByteUtils.init();
        _saveGameCopy = new File(_saveGameFile.toPath() + ".bak");
        int append = 0;
        while(Files.exists(_saveGameCopy.toPath())){
            _saveGameCopy = new File(_saveGameFile.toPath() + ".bak" + append);
            append++;
        }
        try{
            Files.copy(_saveGameFile.toPath(), _saveGameCopy.toPath());
            System.out.println("Backup file saved to " + _saveGameCopy.toPath());
            _saveFileContents = Files.readAllBytes(_saveGameFile.toPath());
            ProcessCharacters(0x20); // Party Members
            ProcessCharacters(0x30); // NPCs
            Files.write(_saveGameFile.toPath(), _saveFileContents);
            return true;
        }
        catch(Exception ex){
            System.err.println(ex +": " + ex.getMessage());
        }
        return false;
    }
    private static void ProcessCharacters(int offset){
        int charactersOffset = ByteUtils.ExtractInt(_saveFileContents, offset);
        int numCharacters = ByteUtils.ExtractInt(_saveFileContents, offset + 0x4);
        System.out.println(charactersOffset + ", " + numCharacters);
        for(int i = 0; i < numCharacters; i++){
            int charOffset = charactersOffset + (i * 0x160);
            UpdateReference(charOffset + 0xE4); // Strongest Foe Reference
            ProcessCREStructure(ByteUtils.ExtractInt(_saveFileContents, charOffset + 0x4), i == 0);
        }
    }
    private static void ProcessCREStructure(int creOffset, boolean isMain){
        if(!isMain){ // skip the main character as these references are not applicable
            UpdateReference(creOffset + 0x8); // Name Reference
            UpdateReference(creOffset + 0xC); // Tooltip Reference
        }
        int soundOffset = creOffset + 0xA4;
        for(int s = 0; s < 100; s++){
            UpdateReference(soundOffset); // Update sounds
            soundOffset += 0x4;
        }
    }
    private static int GetNewReference(int oldReference){
        return _comparisonTable.getOrDefault(oldReference, oldReference);
    }
    private static void UpdateReference(int offset){
        int oldReference = ByteUtils.ExtractInt(_saveFileContents, offset);
        WriteIntToFileContents(GetNewReference(oldReference), offset);
    }
    private static void WriteIntToFileContents(int value, int offset){
        byte[] intBytes = ByteUtils.IntToByteArray(value);
        System.arraycopy(intBytes, 0, _saveFileContents, offset, 4);
    }
}
