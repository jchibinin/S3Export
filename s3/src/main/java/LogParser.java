import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class LogParser {

    public List<Path> filesInFolder;
    public String liftpath;

    public LogParser(List<Path> filesInFolder) {
        this.filesInFolder = filesInFolder;
    }

    public void handleFolder() throws IOException {
        liftpath = PropertiesLocal.lift_path + System.currentTimeMillis() + ".log";
        for (Path file : filesInFolder) {
            //берем только rphost
            if (file.toString().contains("rphost"))            {
                System.out.println("Begin parse " + file.toString());
                parseToOneFile(file, liftpath);
            }
        }
    }

    public void parseToOneFile(Path file, String liftpath) throws IOException {
        //парсим все в один файл
        String filename = file.getFileName().toString().split("\\.")[0];

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toString()),StandardCharsets.UTF_8));
            // BufferedWriter fileWriter = new BufferedWriter(new FileWriter(liftpath,Boolean.TRUE)
             BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(liftpath, true), StandardCharsets.UTF_8))) {

            String toWrite = "";
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                if ((line.length()<6)||(!line.substring(0, 5).replaceAll("\\d\\d:\\d\\d", "").equals(""))) {
                    toWrite = toWrite + line;
                } else {
                    toWrite.replaceAll(System.lineSeparator(), "");
                    if (!toWrite.equals("")) {
                        fileWriter.write("20"+filename + toWrite + System.lineSeparator());
                    }
                    toWrite = line;
                }
            }
            fileWriter.write("20"+filename + toWrite + System.lineSeparator());

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
