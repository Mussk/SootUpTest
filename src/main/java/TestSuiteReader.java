import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TestSuiteReader {

    public List<String> GetTestSuitePaths(String pathToTestSuiteFolder) throws Exception {

        List<String> testSuitePaths = new ArrayList<>();
        File testSuiteFolder = new File(pathToTestSuiteFolder);

        if(testSuiteFolder.exists()) {
            if(testSuiteFolder.isDirectory()) {

                Files.walkFileTree(testSuiteFolder.toPath(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if(testSuiteFolder.toPath().equals(dir.toAbsolutePath()))
                            return FileVisitResult.CONTINUE;

                        testSuitePaths.add(dir.toAbsolutePath().toString());
                        System.out.println("Found package folder: " + dir.toAbsolutePath());
                        return FileVisitResult.CONTINUE;

                    }

                });
            } else { throw  new Exception(testSuiteFolder.getPath() + " is not a folder"); }
        }else { throw new NoSuchFileException(testSuiteFolder.getAbsolutePath(), "1", "No such File");}

        return testSuitePaths;
    }

    /** @param pathToPackage = give full path string
     *  @return List<String> testClassesNames = just names of .class files without extension **/
    public List<String> GetTestClasses(String pathToPackage) throws Exception {

        List<String> testClassesNames = new ArrayList<>();
        File packageFolder = new File(pathToPackage);

        if(packageFolder.exists()) {
            if(packageFolder.isDirectory()) {

                Files.walkFileTree(packageFolder.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                         if(Files.isDirectory(file))
                             return FileVisitResult.CONTINUE;

                         testClassesNames.add(file.getFileName().toString().split(Pattern.quote("."))[0]);
                         System.out.println("Found .class file: " + file.getFileName());
                         return FileVisitResult.CONTINUE;
                    }

                }); } else { throw  new Exception(packageFolder + " is not a folder"); }
        }else { throw new NoSuchFileException(packageFolder.getAbsolutePath(), "1", "No such File");}

        return testClassesNames;

    }

}
