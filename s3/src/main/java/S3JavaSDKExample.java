import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class S3JavaSDKExample {


    public static void main(String[] args) throws Exception {

        if (args!=null&&args.length>0){

            String propertiesFile = args[0];

            Properties prop = new Properties();

            try {
                //обращаемся к файлу и получаем данные
                FileInputStream fileInputStream = new FileInputStream(propertiesFile);
                prop.load(fileInputStream);

                PropertiesLocal.access_key_id     = prop.getProperty("access_key_id");
                PropertiesLocal.secret_access_key = prop.getProperty("secret_access_key");
                PropertiesLocal.logcfg_path       = prop.getProperty("logcfg_path");
                PropertiesLocal.folders_path      = prop.getProperty("folders_path");
                PropertiesLocal.lift_path         = prop.getProperty("lift_path");
                PropertiesLocal.template_path     = prop.getProperty("template_path");
                PropertiesLocal.bucketname        = prop.getProperty("bucketname");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else throw new Exception("No properties file detected");

        List<Path> pathToHandle = null;

        while (true) {

            LogcfgHandler logcfgHandler = new LogcfgHandler();
            logcfgHandler.UpdateLogcfgFile();

            System.out.println("New logcfg to " + logcfgHandler.folderName);

            System.out.println("Begin waiting.. ");
            TimeUnit.MINUTES.sleep(5);
            System.out.println("End waiting.. ");

            if (pathToHandle != null) {

                System.out.println("Begin parsing.. ");
                LogParser logParser = new LogParser(pathToHandle);
                logParser.handleFolder();
                System.out.println("End parsing.. ");

                System.out.println("Sending to S3.. ");
                SendToS3(Paths.get(logParser.liftpath));
                System.out.println("End sending to S3.. ");

                System.out.println("Clearing data ");
                File file = new File(logParser.liftpath);
                file.delete();

                File dir = new File(logcfgHandler.getFolderPath());
                dir.delete();
                System.out.println("End clearing data ");
            }

            System.out.println("Getting files in .. " + logcfgHandler.folderName);
            pathToHandle = logcfgHandler.getPaths();

        }

    }

    public static void SendToS3(Path fileToSend) throws Exception {

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(PropertiesLocal.access_key_id, PropertiesLocal.secret_access_key);

        String bucket_name = PropertiesLocal.bucketname;

        final String file_path = fileToSend.toString();
        String key_name = fileToSend.getFileName().toString();

        {
            System.out.format("Uploading %s to S3 bucket %s...\n", file_path, bucket_name);
            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                    .withEndpointConfiguration(
                            new AmazonS3ClientBuilder.EndpointConfiguration(
                                    "storage.yandexcloud.net", "ru-central1"
                            )
                    )
                    .build();
            try {
                s3.putObject(bucket_name, key_name, new File(file_path));
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }
        }

    }


}

