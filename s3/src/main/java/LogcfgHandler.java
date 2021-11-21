import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogcfgHandler {

    public String folderName;

    public LogcfgHandler() {
        this.folderName = UUID.randomUUID().toString();
    }

    public void UpdateLogcfgFile() throws URISyntaxException {

       File file = new File(Properties.template_path);
       try( BufferedReader fileReader = new BufferedReader(new FileReader(file));
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(Properties.logcfg_path))){

           String path = Properties.folders_path+folderName;
           while (fileReader.ready()){
               String line = fileReader.readLine();
               fileWriter.write(line.replaceAll("_change_",path));
           }
       } catch (IOException exception) {
           exception.printStackTrace();
       }
    }

    public List<Path> getPaths(){
        List<Path> filesInFolder = null;
        String path         = Properties.folders_path+folderName; // установить папку
        try {
            filesInFolder = Files.walk(Paths.get(path))//"c:/tmp/1/"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());


        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return filesInFolder;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderPath() {
        return Properties.folders_path+folderName;
    }
}
